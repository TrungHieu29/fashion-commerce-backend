package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.PaymentRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.PaymentResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop; // Import OrderShop
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Payment;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod; // Import PaymentMethod
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus; // Import ShippingStatus
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.PaymentMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.PaymentRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderService; // Import OrderService
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    // private final OrderService orderService; // Xóa injection OrderService
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

        // Không trừ kho ở đây cho COD. Việc trừ kho sẽ xảy ra khi shop CONFIRM đơn hàng.
        // Trạng thái OrderShop vẫn là PENDING.

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
        // Phương thức này sẽ chỉ dùng cho các trường hợp cập nhật status nội bộ khác nếu cần.
        // Không tự động cập nhật OrderShop và trừ kho ở đây nữa.
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        payment.setStatus(status);
        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDto processOnlinePaymentResult(Long paymentId, PaymentStatus resultStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getMethod() == PaymentMethod.COD) {
            throw new IllegalArgumentException("Cannot process online payment result for COD payment method.");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            return paymentMapper.toDto(payment);
        }

        payment.setStatus(resultStatus);
        Payment savedPayment = paymentRepository.save(payment);

        Order order = savedPayment.getOrder();

        if (resultStatus == PaymentStatus.COMPLETED) {
            order.getOrderShops().forEach(orderShop -> {
                if (orderShop.getStatus() == OrderStatus.PENDING) {
                    orderShop.setStatus(OrderStatus.CONFIRMED);
                }
            });
        } else if (resultStatus == PaymentStatus.FAILED) {
            order.getOrderShops().forEach(orderShop -> {
                if (orderShop.getStatus() == OrderStatus.PENDING || orderShop.getStatus() == OrderStatus.CONFIRMED) {
                    orderShop.setStatus(OrderStatus.CANCELLED);
                }
                if (orderShop.getShipping() != null) {
                    orderShop.getShipping().setShippingStatus(ShippingStatus.CANCELLED);
                }
            });
        }

        orderRepository.save(order);
        return paymentMapper.toDto(savedPayment);
    }
}
