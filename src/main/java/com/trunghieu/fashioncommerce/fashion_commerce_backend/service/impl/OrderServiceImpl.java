package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.DiscountResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.*;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.*;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DiscountService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderShopRepository orderShopRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final DiscountService discountService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getUserId()));

        // Lấy Cart của user
        Cart cart = cartRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user with id: " + requestDto.getUserId()));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty, cannot create order");
        }

        // Gom nhóm CartItems theo Shop
        Map<Long, List<CartItem>> itemsByShop = cart.getCartItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProductVariant().getProduct().getShop().getId()));

        // 1. Xác định địa chỉ giao hàng và tạo snapshot
        String addressSnapshot = resolveAddressSnapshot(user, requestDto);

        // Tạo Order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .addressSnapshot(addressSnapshot)
                .build();

        Set<OrderShop> orderShops = new HashSet<>();
        BigDecimal totalOrderPrice = BigDecimal.ZERO;

        // Tạo OrderShop cho mỗi shop
        for (Map.Entry<Long, List<CartItem>> entry : itemsByShop.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItem> shopCartItems = entry.getValue();

            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

            OrderShop orderShop = OrderShop.builder()
                    .order(order)
                    .shop(shop)
                    .addressSnapshot(addressSnapshot)
                    .status(OrderStatus.PENDING)
                    .build();

            Set<OrderItem> orderItems = shopCartItems.stream()
                    .map(cartItem -> createOrderItemFromCartItem(cartItem, orderShop))
                    .collect(Collectors.toSet());

            orderShop.setOrderItems(orderItems);

            // Tính tổng tiền cho OrderShop
            BigDecimal shopTotalPrice = orderItems.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            orderShop.setTotalPrice(shopTotalPrice);

            // Áp dụng discount nếu có
            Discount applicableDiscount = findApplicableDiscount(shopId, shopTotalPrice);
            if (applicableDiscount != null) {
                orderShop.setDiscount(applicableDiscount);
                BigDecimal discountAmount = calculateDiscountAmount(applicableDiscount, shopTotalPrice);
                orderShop.setFinalPrice(shopTotalPrice.subtract(discountAmount));
            } else {
                orderShop.setFinalPrice(shopTotalPrice);
            }

            totalOrderPrice = totalOrderPrice.add(orderShop.getFinalPrice());

            // 2. Tạo OrderShipping cho mỗi shop (Lưu snapshot địa chỉ ngay lúc này)
            OrderShipping shipping = OrderShipping.builder()
                    .orderShop(orderShop)
                    .addressSnapshot(addressSnapshot)
                    .shippingStatus(ShippingStatus.PENDING)
                    .build();
            orderShop.setShipping(shipping);

            orderShops.add(orderShop);
        }

        order.setOrderShops(orderShops);
        order.setTotalPrice(totalOrderPrice);
        order.setFinalPrice(totalOrderPrice); // Chưa áp dụng discount tổng

        Order savedOrder = orderRepository.save(order);

        // Xóa CartItems sau khi tạo order thành công
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return orderMapper.toDto(savedOrder);
    }

    private String resolveAddressSnapshot(User user, OrderRequestDto requestDto) {
        // Trường hợp 1: Khách chọn một địa chỉ ID cụ thể đã có sẵn
        if (requestDto.getAddressId() != null) {
            ShippingAddress addr = shippingAddressRepository.findById(requestDto.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + requestDto.getAddressId()));
            // Kiểm tra bảo mật: địa chỉ phải thuộc về user đang đặt hàng
            if (!addr.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Shipping address does not belong to this user");
            }
            return formatAddressSnapshot(addr.getReceiverName(), addr.getPhone(), addr.getAddressLine(), addr.getDistrict(), addr.getCity());
        }

        // Trường hợp 2: Khách nhập thông tin địa chỉ mới trực tiếp tại form checkout
        if (isAddressInfoProvided(requestDto)) {
            // Nếu user chưa từng có địa chỉ nào trong hệ thống, lưu cái này làm địa chỉ mặc định luôn
            if (shippingAddressRepository.findByUserId(user.getId()).isEmpty()) {
                ShippingAddress newAddr = ShippingAddress.builder()
                        .user(user)
                        .receiverName(requestDto.getReceiverName())
                        .phone(requestDto.getPhone())
                        .addressLine(requestDto.getAddressLine())
                        .city(requestDto.getCity())
                        .district(requestDto.getDistrict())
                        .isDefault(true)
                        .build();
                shippingAddressRepository.save(newAddr);
            }
            return formatAddressSnapshot(requestDto.getReceiverName(), requestDto.getPhone(), requestDto.getAddressLine(), requestDto.getDistrict(), requestDto.getCity());
        }

        // Trường hợp 3: Khách không truyền gì cả, lấy địa chỉ mặc định trong profile
        return shippingAddressRepository.findByUserId(user.getId()).stream()
                .filter(ShippingAddress::getIsDefault)
                .findFirst()
                .map(addr -> formatAddressSnapshot(addr.getReceiverName(), addr.getPhone(), addr.getAddressLine(), addr.getDistrict(), addr.getCity()))
                .orElseThrow(() -> new IllegalArgumentException("No shipping address found. Please provide address information."));
    }

    private boolean isAddressInfoProvided(OrderRequestDto dto) {
        return dto.getReceiverName() != null && !dto.getReceiverName().isBlank() &&
               dto.getPhone() != null && !dto.getPhone().isBlank() &&
               dto.getAddressLine() != null && !dto.getAddressLine().isBlank() &&
               dto.getCity() != null && !dto.getCity().isBlank() &&
               dto.getDistrict() != null && !dto.getDistrict().isBlank();
    }

    private String formatAddressSnapshot(String name, String phone, String line, String district, String city) {
        return String.format("%s | %s | %s, %s, %s", name, phone, line, district, city);
    }

    private Discount findApplicableDiscount(Long shopId, BigDecimal orderValue) {
        List<DiscountResponseDto> activeDiscounts = discountService.getActiveDiscountsByShopId(shopId);
        return activeDiscounts.stream()
                .filter(d -> d.getMinOrderValue() == null || orderValue.compareTo(d.getMinOrderValue()) >= 0)
                .findFirst() // Chọn discount đầu tiên phù hợp, có thể sort theo ưu tiên sau
                .map(dto -> {
                    Discount discount = new Discount();
                    discount.setId(dto.getId());
                    discount.setDiscountType(DiscountType.valueOf(dto.getDiscountType()));
                    discount.setDiscountValue(dto.getDiscountValue());
                    return discount;
                })
                .orElse(null);
    }

    private BigDecimal calculateDiscountAmount(Discount discount, BigDecimal totalPrice) {
        if (discount.getDiscountType() == DiscountType.PERCENT) {
            return totalPrice.multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else {
            return discount.getDiscountValue();
        }
    }

    private OrderItem createOrderItemFromCartItem(CartItem cartItem, OrderShop orderShop) {
        ProductVariant variant = cartItem.getProductVariant();
        Product product = variant.getProduct();

        // Lấy hình ảnh đầu tiên của sản phẩm
        String productImage = productImageRepository.findByProductId(product.getId()).stream()
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return OrderItem.builder()
                .orderShop(orderShop)
                .productVariant(variant)
                .quantity(cartItem.getQuantity())
                .price(product.getPrice()) // Freeze giá tại thời điểm mua
                .productName(product.getProductName()) // Freeze tên
                .productImage(productImage) // Freeze hình
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(status);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }
}
