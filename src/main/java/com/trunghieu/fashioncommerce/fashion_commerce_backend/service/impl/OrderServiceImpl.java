package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.*;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountTarget;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod; // Import PaymentMethod
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus; // Import PaymentStatus
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.NotificationType;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.*;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DiscountService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.NotificationService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.PaymentService; // Import PaymentService
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Import Specification
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate; // Import Predicate
import jakarta.persistence.criteria.Subquery; // Import Subquery
import jakarta.persistence.criteria.Join; // Import Join

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        private final OrderShopRepository orderShopRepository; // Inject OrderShopRepository
        private final OrderItemRepository orderItemRepository;
        private final ShippingAddressRepository shippingAddressRepository;
        private final DiscountRepository discountRepository; // Vẫn giữ để lấy Discount entity nếu cần
        private final DiscountService discountService;
        private final OrderMapper orderMapper;
        private final ProductVariantRepository productVariantRepository; // Inject ProductVariantRepository
        private final PaymentService paymentService; // Inject PaymentService
        private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + requestDto.getUserId()));

        Cart cart = cartRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user with id: " + requestDto.getUserId()));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty, cannot create order");
        }

        List<CartItem> checkoutItems = new ArrayList<>(cart.getCartItems());

        if (requestDto.getCartItemIds() != null && !requestDto.getCartItemIds().isEmpty()) {
            Set<Long> selectedIds = new HashSet<>(requestDto.getCartItemIds());
            checkoutItems = checkoutItems.stream()
                    .filter(item -> selectedIds.contains(item.getId()))
                    .toList();
        }

        if (checkoutItems.isEmpty()) {
            throw new IllegalArgumentException("No selected cart items to checkout");
        }

        Map<Long, List<CartItem>> itemsByShop = checkoutItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProductVariant().getProduct().getShop().getId()));

        String addressSnapshot = resolveAddressSnapshot(user, requestDto);

        Order order = Order.builder()
                .user(user)
                .addressSnapshot(addressSnapshot)
                .build();

        Payment payment = Payment.builder()
                .method(requestDto.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .order(order)
                .build();
        order.setPayment(payment);

        Set<OrderShop> orderShops = new HashSet<>();
        BigDecimal totalBeforeVoucher = BigDecimal.ZERO;
        BigDecimal totalAfterVoucher = BigDecimal.ZERO;
        LocalDateTime orderCreationTime = LocalDateTime.now();

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

            Set<OrderItem> orderItems = shopCartItems.stream().map(cartItem -> {
                ProductVariant variant = cartItem.getProductVariant();

                if (variant.getStock() < cartItem.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Sản phẩm " + variant.getProduct().getProductName()
                                    + " chỉ còn " + variant.getStock() + " sản phẩm trong kho");
                }

                OrderItem item = createOrderItemFromCartItem(cartItem, orderShop);

                BigDecimal discount = discountService.calculateBestDiscount(
                        shopId,
                        item.getProductVariant().getProduct().getId(),
                        item.getPrice());

                item.setPrice(item.getPrice().subtract(discount));
                return item;
            }).collect(Collectors.toSet());

            orderShop.setOrderItems(orderItems);

            BigDecimal subtotal = orderItems.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            orderShop.setTotalPrice(subtotal);

            BigDecimal shopFinalPrice = subtotal;

            String voucherCode = null;
            if (requestDto.getShopVoucherCodes() != null) {
                voucherCode = requestDto.getShopVoucherCodes().get(shopId);
            }
            if ((voucherCode == null || voucherCode.isBlank()) && requestDto.getVoucherCode() != null) {
                voucherCode = requestDto.getVoucherCode();
            }

            if (voucherCode != null && !voucherCode.isBlank()) {
                BigDecimal voucherDiscountAmount = discountService.applyOrderVoucher(
                        shopId,
                        voucherCode,
                        subtotal,
                        orderCreationTime
                );

                if (voucherDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Discount appliedVoucher = discountRepository
                            .findByShopIdAndCodeAndStatus(
                                    shopId,
                                    voucherCode,
                                    com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus.ACTIVE)
                            .filter(d -> d.getDiscountTarget() == DiscountTarget.ORDER)
                            .filter(d -> d.getStartDate().isBefore(orderCreationTime)
                                    && d.getEndDate().isAfter(orderCreationTime))
                            .filter(d -> d.getMinOrderValue() == null
                                    || subtotal.compareTo(d.getMinOrderValue()) >= 0)
                            .orElse(null);

                    if (appliedVoucher != null) {
                        orderShop.setDiscount(appliedVoucher);
                        shopFinalPrice = subtotal.subtract(voucherDiscountAmount);
                    }
                }
            }

            orderShop.setFinalPrice(shopFinalPrice);

            totalBeforeVoucher = totalBeforeVoucher.add(orderShop.getTotalPrice());
            totalAfterVoucher = totalAfterVoucher.add(orderShop.getFinalPrice());

            OrderShipping shipping = OrderShipping.builder()
                    .orderShop(orderShop)
                    .addressSnapshot(addressSnapshot)
                    .shippingStatus(ShippingStatus.PENDING)
                    .build();
            orderShop.setShipping(shipping);

            orderShops.add(orderShop);
        }

        order.setOrderShops(orderShops);
        order.setTotalPrice(totalBeforeVoucher);
        order.setFinalPrice(totalAfterVoucher);
        payment.setAmount(totalAfterVoucher);

        Order savedOrder = orderRepository.save(order);

        savedOrder.getOrderShops().forEach(orderShop -> notificationService.createOrderNotification(
                orderShop,
                NotificationType.ORDER_CREATED,
                "Dat hang thanh cong",
                "Don hang #" + savedOrder.getId() + " tai shop "
                        + orderShop.getShop().getShopName()
                        + " da duoc tao va dang cho xac nhan."
        ));

        // Chỉ xóa những CartItem đã checkout, giữ lại các sản phẩm không được tích.
        Set<Long> checkedOutIds = checkoutItems.stream()
                .map(CartItem::getId)
                .collect(Collectors.toSet());
        cart.getCartItems().removeIf(item -> checkedOutIds.contains(item.getId()));
        cartRepository.save(cart);

        return orderMapper.toDto(savedOrder);
    }

        private String resolveAddressSnapshot(User user, OrderRequestDto requestDto) {
                // Trường hợp 1: Khách chọn một địa chỉ ID cụ thể đã có sẵn
                if (requestDto.getAddressId() != null) {
                        ShippingAddress addr = shippingAddressRepository.findById(requestDto.getAddressId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Shipping address not found with id: "
                                                                        + requestDto.getAddressId()));
                        // Kiểm tra bảo mật: địa chỉ phải thuộc về user đang đặt hàng
                        if (!addr.getUser().getId().equals(user.getId())) {
                                throw new IllegalArgumentException("Shipping address does not belong to this user");
                        }
                        return formatAddressSnapshot(addr.getReceiverName(), addr.getPhone(), addr.getAddressLine(),
                                        addr.getDistrict(), addr.getCity());
                }

                // Trường hợp 2: Khách nhập thông tin địa chỉ mới trực tiếp tại form checkout
                if (isAddressInfoProvided(requestDto)) {
                        // Nếu user chưa từng có địa chỉ nào trong hệ thống, lưu cái này làm địa chỉ mặc
                        // định luôn
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
                        return formatAddressSnapshot(requestDto.getReceiverName(), requestDto.getPhone(),
                                        requestDto.getAddressLine(), requestDto.getDistrict(), requestDto.getCity());
                }

                // Trường hợp 3: Khách không truyền gì cả, lấy địa chỉ mặc định trong profile
                return shippingAddressRepository.findByUserId(user.getId()).stream()
                                .filter(ShippingAddress::getIsDefault)
                                .findFirst()
                                .map(addr -> formatAddressSnapshot(addr.getReceiverName(), addr.getPhone(),
                                                addr.getAddressLine(),
                                                addr.getDistrict(), addr.getCity()))
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No shipping address found. Please provide address information."));
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

        private OrderItem createOrderItemFromCartItem(CartItem cartItem, OrderShop orderShop) {
                ProductVariant variant = cartItem.getProductVariant();
                Product product = variant.getProduct();

                // SỬA ĐỔI: Lấy hình ảnh theo đúng màu sắc của Variant
                String productImage = productImageRepository
                                .findByProductIdAndColor(product.getId(), variant.getColor())
                                .or(() -> productImageRepository.findByProductId(product.getId()).stream().findFirst()) // Fallback
                                                                                                                        // lấy
                                                                                                                        // đại
                                                                                                                        // 1
                                                                                                                        // cái
                                                                                                                        // nếu
                                                                                                                        // màu
                                                                                                                        // đó
                                                                                                                        // chưa
                                                                                                                        // có
                                                                                                                        // ảnh
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
        public Page<OrderResponseDto> getOrdersByUserId(Long userId, List<OrderStatus> shopStatuses, Pageable pageable) {
            Specification<Order> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

                if (shopStatuses != null && !shopStatuses.isEmpty()) {
                    // Tạo subquery để tìm OrderShop có trạng thái phù hợp
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<OrderShop> orderShopRoot = subquery.from(OrderShop.class);
                    subquery.select(orderShopRoot.get("order").get("id"))
                            .where(orderShopRoot.get("status").in(shopStatuses));
                    predicates.add(criteriaBuilder.in(root.get("id")).value(subquery));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            // Lấy Page<Order> đã lọc và sắp xếp từ database
            Page<Order> ordersPage = orderRepository.findAll(spec, pageable);

            // Xử lý hậu kỳ: Lọc OrderShop con trong từng Order
            List<OrderResponseDto> pageContent = ordersPage.getContent().stream()
                    .map(order -> {
                        OrderResponseDto dto = orderMapper.toDto(order);
                        if (shopStatuses != null && !shopStatuses.isEmpty()) {
                            // Lọc OrderShop con trong Order đã ánh xạ
                            Set<OrderShopResponseDto> filteredOrderShops = dto.getOrderShops().stream()
                                    .filter(orderShopDto -> shopStatuses.contains(orderShopDto.getStatus()))
                                    .collect(Collectors.toSet());
                            dto.setOrderShops(filteredOrderShops);
                        }
                        return dto;
                    })
                    .filter(dto -> !dto.getOrderShops().isEmpty()) // Loại bỏ Order nếu không còn OrderShop nào sau khi lọc
                    .collect(Collectors.toList());

            // Tạo PageImpl mới với tổng số phần tử đã lọc và sắp xếp
            // ordersPage.getTotalElements() đã là tổng số Order phù hợp với tiêu chí lọc chính
            return new PageImpl<>(pageContent, pageable, ordersPage.getTotalElements());
        }

        @Override
        @Transactional
        public void deleteOrder(Long orderId) {
                if (!orderRepository.existsById(orderId)) {
                        throw new ResourceNotFoundException("Order not found with id: " + orderId);
                }
                orderRepository.deleteById(orderId);
        }

        @Override
        public void replenishStock(OrderShop orderShop) { // Đổi thành public để OrderShopServiceImpl có thể gọi
            if (orderShop.getOrderItems() == null)
                return;
            // Chỉ hoàn kho nếu stock đã bị trừ
            if (orderShop.isStockDeducted()) {
                orderShop.getOrderItems().forEach(item -> {
                    var variant = item.getProductVariant();
                    if (variant != null) {
                        variant.setStock(variant.getStock() + item.getQuantity());
                        productVariantRepository.save(variant);
                    }
                });
                orderShop.setStockDeducted(false); // Đặt lại cờ sau khi hoàn kho
                orderShopRepository.save(orderShop); // Lưu OrderShop để cập nhật cờ
            }
        }

        @Override
        public void deductStock(OrderShop orderShop) {
            if (orderShop.getOrderItems() == null)
                return;
            // Chỉ trừ kho nếu stock chưa bị trừ
            if (!orderShop.isStockDeducted()) {
                orderShop.getOrderItems().forEach(item -> {
                    var variant = item.getProductVariant();
                    if (variant != null) {
                        if (variant.getStock() < item.getQuantity()) {
                            throw new IllegalArgumentException("Sản phẩm " + variant.getProduct().getProductName() + " không đủ tồn kho");
                        }
                        variant.setStock(variant.getStock() - item.getQuantity());
                        productVariantRepository.save(variant);
                    }
                });
                orderShop.setStockDeducted(true); // Đặt cờ sau khi trừ kho
                orderShopRepository.save(orderShop); // Lưu OrderShop để cập nhật cờ
            }
        }
}
