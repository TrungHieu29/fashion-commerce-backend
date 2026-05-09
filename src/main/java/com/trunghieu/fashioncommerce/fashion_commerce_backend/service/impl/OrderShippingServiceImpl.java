package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShippingRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShippingResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShipping;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderShippingMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShippingRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderShippingServiceImpl implements OrderShippingService {

    private final OrderShippingRepository orderShippingRepository;
    private final OrderShopRepository orderShopRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderShippingMapper orderShippingMapper;

    @Override
    @Transactional
    public OrderShippingResponseDto createOrderShipping(Long orderShopId, OrderShippingRequestDto requestDto) {
        OrderShop orderShop = orderShopRepository.findById(orderShopId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShop not found with id: " + orderShopId));

        if (orderShippingRepository.findByOrderShopId(orderShopId).isPresent()) {
            throw new IllegalArgumentException("Shipping info already exists for this order shop");
        }

        OrderShipping orderShipping = orderShippingMapper.toEntity(requestDto);
        orderShipping.setOrderShop(orderShop);
        orderShipping.setAddressSnapshot(orderShop.getAddressSnapshot());
        orderShipping.setShippingStatus(parseShippingStatus(requestDto.getShippingStatus()));
        orderShipping.setTrackingCode(requestDto.getTrackingCode() != null ? requestDto.getTrackingCode() : generateTrackingCode());

        OrderShipping savedShipping = orderShippingRepository.save(orderShipping);
        return orderShippingMapper.toDto(savedShipping);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderShippingResponseDto getOrderShippingById(Long id) {
        OrderShipping orderShipping = orderShippingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShipping not found with id: " + id));
        return orderShippingMapper.toDto(orderShipping);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderShippingResponseDto getOrderShippingByOrderShopId(Long orderShopId) {
        OrderShipping orderShipping = orderShippingRepository.findByOrderShopId(orderShopId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShipping not found for OrderShop id: " + orderShopId));
        return orderShippingMapper.toDto(orderShipping);
    }

    @Override
    @Transactional
    public OrderShippingResponseDto updateOrderShipping(Long id, OrderShippingRequestDto requestDto) {
        OrderShipping orderShipping = orderShippingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShipping not found with id: " + id));

        if (requestDto.getShippingStatus() != null) {
            orderShipping.setShippingStatus(parseShippingStatus(requestDto.getShippingStatus()));
            updateStatusWorkflow(orderShipping, parseShippingStatus(requestDto.getShippingStatus()));
        }
        if (requestDto.getTrackingCode() != null) {
            orderShipping.setTrackingCode(requestDto.getTrackingCode());
            // Tự động chuyển trạng thái sang PROCESSING nếu có tracking code mà đang PENDING
            if (orderShipping.getShippingStatus() == ShippingStatus.PENDING) {
                updateStatusWorkflow(orderShipping, ShippingStatus.PROCESSING);
            }
        }

        OrderShipping savedShipping = orderShippingRepository.save(orderShipping);
        return orderShippingMapper.toDto(savedShipping);
    }

    /**
     * Mockup luồng trạng thái tự động (Shopee-like)
     */
    private void updateStatusWorkflow(OrderShipping shipping, ShippingStatus newStatus) {
        shipping.setShippingStatus(newStatus);
        OrderShop orderShop = shipping.getOrderShop();
        Order order = orderShop.getOrder();

        switch (newStatus) {
            case PROCESSING:
                orderShop.setStatus(OrderStatus.PROCESSING);
                break;
            case SHIPPED:
                orderShop.setStatus(OrderStatus.SHIPPED);
                break;
            case DELIVERED:
                orderShop.setStatus(OrderStatus.DELIVERED);
                // Nếu là COD, cập nhật luôn Payment sang COMPLETED (Mockup logic)
                if (order.getPayment() != null && order.getPayment().getMethod() != null 
                        && "COD".equals(order.getPayment().getMethod().name())) {
                    order.getPayment().setStatus(PaymentStatus.COMPLETED);
                }
                break;
            case CANCELLED:
                orderShop.setStatus(OrderStatus.CANCELLED);
                replenishStock(orderShop); // Hoàn kho khi hủy
                break;
            case RETURNED:
                orderShop.setStatus(OrderStatus.RETURNED);
                replenishStock(orderShop); // Hoàn kho khi khách trả hàng
                break;
        }

        // Sau khi cập nhật OrderShop, kiểm tra để cập nhật trạng thái Order tổng
        updateParentOrderStatus(order);
    }

    /**
     * Cập nhật trạng thái Order tổng dựa trên trạng thái của tất cả OrderShop thành phần
     */
    private void updateParentOrderStatus(Order order) {
        boolean allDelivered = order.getOrderShops().stream().allMatch(s -> s.getStatus() == OrderStatus.DELIVERED);
        boolean allShipped = order.getOrderShops().stream().allMatch(s -> s.getStatus() == OrderStatus.SHIPPED || s.getStatus() == OrderStatus.DELIVERED);
        boolean anyCancelled = order.getOrderShops().stream().anyMatch(s -> s.getStatus() == OrderStatus.CANCELLED);
        boolean allCancelled = order.getOrderShops().stream().allMatch(s -> s.getStatus() == OrderStatus.CANCELLED);
        boolean anyReturned = order.getOrderShops().stream().anyMatch(s -> s.getStatus() == OrderStatus.RETURNED);

        if (allDelivered) {
            order.setStatus(OrderStatus.DELIVERED);
        } else if (anyReturned) {
            // Nếu có bất kỳ shop nào bị hoàn hàng, trạng thái order tổng có thể đánh dấu là RETURNED hoặc DELIVERED tùy nghiệp vụ
            order.setStatus(OrderStatus.RETURNED);
        } else if (allShipped) {
            order.setStatus(OrderStatus.SHIPPED);
        } else if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        // Lưu ý: Nếu chỉ 1 shop hủy, Order vẫn ở trạng thái PROCESSING hoặc SHIPPED tùy vào các shop còn lại
    }

    /**
     * Hoàn trả số lượng hàng vào kho
     */
    private void replenishStock(OrderShop orderShop) {
        if (orderShop.getOrderItems() == null) return;
        orderShop.getOrderItems().forEach(item -> {
            var variant = item.getProductVariant();
            if (variant != null) {
                variant.setStock(variant.getStock() + item.getQuantity());
                productVariantRepository.save(variant);
            }
        });
    }

    private ShippingStatus parseShippingStatus(String status) {
        if (status == null || status.isBlank()) {
            return ShippingStatus.PENDING;
        }
        try {
            return ShippingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid shipping status: " + status);
        }
    }

    private String generateTrackingCode() {
        return UUID.randomUUID().toString();
    }
}
