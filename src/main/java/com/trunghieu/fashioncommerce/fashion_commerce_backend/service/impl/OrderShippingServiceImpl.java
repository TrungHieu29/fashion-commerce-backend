package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShippingRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShippingResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShipping;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Payment;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderShippingMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderRepository; // Import OrderRepository
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShippingRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderService; // Import OrderService
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderShippingServiceImpl implements OrderShippingService {

    private final OrderShippingRepository orderShippingRepository;
    private final OrderShopRepository orderShopRepository;
    private final OrderService orderService; // Inject OrderService
    private final OrderShippingMapper orderShippingMapper;
    private final OrderRepository orderRepository; // Inject OrderRepository

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
        orderShipping.setTrackingCode(
                requestDto.getTrackingCode() != null ? requestDto.getTrackingCode() : generateTrackingCode());

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OrderShipping not found for OrderShop id: " + orderShopId));
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
            // Tự động chuyển trạng thái sang PROCESSING nếu có tracking code mà đang
            // PENDING
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
        Order order = orderShop.getOrder(); // Lấy Order chính

        switch (newStatus) {
            case PROCESSING:
                // OrderShop.status vẫn là CONFIRMED (nếu đã xác nhận) hoặc PENDING (nếu chưa)
                // Không tự động chuyển OrderShop sang PROCESSING ở đây nữa
                break;
            case SHIPPED:
                orderShop.setStatus(OrderStatus.SHIPPED);
                break;
            case DELIVERED:
                orderShop.setStatus(OrderStatus.DELIVERED);
                // Nếu là COD, cập nhật luôn Payment sang COMPLETED (Mockup logic)
                if (order.getPayment() != null && order.getPayment().getMethod() == PaymentMethod.COD && order.getPayment().getStatus() == PaymentStatus.PENDING) {
                    order.getPayment().setStatus(PaymentStatus.COMPLETED);
                    orderRepository.save(order); // Lưu Order để cập nhật Payment
                }
                break;
            case CANCELLED:
                orderShop.setStatus(OrderStatus.CANCELLED);
                orderService.replenishStock(orderShop); // Hoàn kho khi hủy
                // Xử lý hoàn tiền nếu đã thanh toán online
                Payment payment = order.getPayment();
                if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
                    payment.setStatus(PaymentStatus.REFUND_INITIATED);
                    orderRepository.save(order); // Lưu Order để cập nhật Payment
                }
                break;
            case RETURNED:
                orderShop.setStatus(OrderStatus.RETURNED);
                orderService.replenishStock(orderShop); // Hoàn kho khi khách trả hàng
                // Xử lý hoàn tiền nếu đã thanh toán online hoặc COD
                Payment returnPayment = order.getPayment();
                if (returnPayment != null && returnPayment.getStatus() == PaymentStatus.COMPLETED) {
                    returnPayment.setStatus(PaymentStatus.REFUND_INITIATED);
                    orderRepository.save(order); // Lưu Order để cập nhật Payment
                }
                break;
        }
        orderShopRepository.save(orderShop); // Lưu OrderShop sau khi cập nhật trạng thái
    }

    private ShippingStatus parseShippingStatus(String status) {
        if (status == null || status == null || status.isBlank()) {
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
