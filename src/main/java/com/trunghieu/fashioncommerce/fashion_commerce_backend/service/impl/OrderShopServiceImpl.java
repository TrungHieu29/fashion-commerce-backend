package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderItemMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderShopMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.DiscountRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderShopServiceImpl implements OrderShopService {

    private final OrderShopRepository orderShopRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final DiscountRepository discountRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderShopMapper orderShopMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public OrderShopResponseDto createOrderShop(Long orderId, OrderShopRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        Shop shop = shopRepository.findById(requestDto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + requestDto.getShopId()));

        OrderShop orderShop = orderShopMapper.toEntity(requestDto);
        orderShop.setOrder(order);
        orderShop.setShop(shop);
        orderShop.setStatus(OrderStatus.PENDING);

        if (requestDto.getDiscountId() != null) {
            Discount discount = discountRepository.findById(requestDto.getDiscountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + requestDto.getDiscountId()));
            if (discount.getShop() == null || !discount.getShop().getId().equals(shop.getId())) {
                throw new IllegalArgumentException("Discount does not belong to the selected shop");
            }
            orderShop.setDiscount(discount);
        }

        Set<OrderItem> orderItems = requestDto.getOrderItems().stream()
                .map(this::mapToOrderItem)
                .collect(Collectors.toSet());

        orderItems.forEach(item -> item.setOrderShop(orderShop));
        orderShop.setOrderItems(orderItems);

        BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderShop.setTotalPrice(totalPrice);

        BigDecimal finalPrice = totalPrice;
        if (orderShop.getDiscount() != null) {
            Discount discount = orderShop.getDiscount();
            if (discount.getDiscountType() == DiscountType.PERCENT) {
                finalPrice = totalPrice.subtract(totalPrice.multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100)));
            } else {
                finalPrice = totalPrice.subtract(discount.getDiscountValue());
            }
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
        }
        orderShop.setFinalPrice(finalPrice);

        OrderShop savedOrderShop = orderShopRepository.save(orderShop);
        return orderShopMapper.toDto(savedOrderShop);
    }

    private OrderItem mapToOrderItem(OrderItemRequestDto requestDto) {
        ProductVariant productVariant = productVariantRepository.findById(requestDto.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + requestDto.getProductVariantId()));

        if (productVariant.getProduct() == null) {
            throw new ResourceNotFoundException("Product not found for variant id: " + requestDto.getProductVariantId());
        }

        OrderItem orderItem = orderItemMapper.toEntity(requestDto);
        orderItem.setProductVariant(productVariant);
        orderItem.setPrice(productVariant.getProduct().getPrice());
        return orderItem;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderShopResponseDto getOrderShopById(Long id) {
        OrderShop orderShop = orderShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShop not found with id: " + id));
        return orderShopMapper.toDto(orderShop);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderShopResponseDto> getOrderShopsByOrderId(Long orderId, Pageable pageable) {
        return orderShopRepository.findByOrderId(orderId, pageable)
                .map(orderShopMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderShopResponseDto> getOrderShopsByShopId(Long shopId, Pageable pageable) {
        return orderShopRepository.findByShopId(shopId, pageable)
                .map(orderShopMapper::toDto);
    }
}
