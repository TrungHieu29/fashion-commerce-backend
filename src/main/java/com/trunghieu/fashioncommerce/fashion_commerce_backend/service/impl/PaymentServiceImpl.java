package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.PaymentRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.PaymentResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Payment;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.PaymentMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.PaymentRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(Long orderId, PaymentRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (paymentRepository.findByOrder_Id(orderId).isPresent()) {
            throw new IllegalArgumentException("Payment already exists for order id: " + orderId);
        }

        Payment payment = paymentMapper.toEntity(requestDto);
        payment.setOrder(order);
        payment.setAmount(order.getFinalPrice()); // Ép số tiền thanh toán bằng đúng Final Price của Order

        Payment savedPayment = paymentRepository.save(payment);

        // Nếu là COD, tự động chuyển đơn hàng sang PROCESSING để Shop bắt đầu xử lý
        if (requestDto.getMethod() == com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod.COD) {
            order.setStatus(OrderStatus.PROCESSING);
            if (order.getOrderShops() != null) {
                order.getOrderShops().forEach(shop -> shop.setStatus(OrderStatus.PROCESSING));
                deductStock(order); // Trừ kho ngay khi tạo đơn COD
            }
            orderRepository.save(order);
        }

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional
    public PaymentResponseDto updatePaymentStatus(Long paymentId, com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        payment.setStatus(status);
        Payment savedPayment = paymentRepository.save(payment);

        // Tự động cập nhật Order và OrderShop dựa trên kết quả thanh toán Online
        Order order = savedPayment.getOrder();
        if (status == com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus.COMPLETED) {
            order.setStatus(OrderStatus.PROCESSING);
            order.getOrderShops().forEach(shop -> shop.setStatus(OrderStatus.PROCESSING));
            deductStock(order); // Trừ kho khi thanh toán online thành công
        } else if (status == com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
            order.getOrderShops().forEach(shop -> shop.setStatus(OrderStatus.CANCELLED));
        }
        orderRepository.save(order);

        return paymentMapper.toDto(savedPayment);
    }

    private void deductStock(Order order) {
        order.getOrderShops().forEach(shop -> {
            shop.getOrderItems().forEach(item -> {
                var variant = item.getProductVariant();
                if (variant.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException("Sản phẩm " + variant.getProduct().getProductName() + " không đủ tồn kho");
                }
                variant.setStock(variant.getStock() - item.getQuantity());
                productVariantRepository.save(variant);
            });
        });
    }
}
