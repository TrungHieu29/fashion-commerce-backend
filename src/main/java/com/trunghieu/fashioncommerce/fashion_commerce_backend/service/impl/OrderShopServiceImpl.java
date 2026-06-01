package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Payment; // Import Payment
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod; // Import PaymentMethod
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus; // Import PaymentStatus
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus; // Import ShippingStatus
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderItemMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderShopMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.DiscountRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderService; // Inject OrderService
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
    private final OrderService orderService; // Inject OrderService

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

    @Override
    @Transactional
    public OrderShopResponseDto confirmDelivery(Long orderShopId) {
        OrderShop orderShop = orderShopRepository.findById(orderShopId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShop not found with id: " + orderShopId));

        if (orderShop.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("OrderShop must be in DELIVERED status to be completed.");
        }

        orderShop.setStatus(OrderStatus.COMPLETED);

        // Cập nhật PaymentStatus của Order chính nếu là COD và đang PENDING
        Order order = orderShop.getOrder();
        Payment payment = order.getPayment();
        if (payment != null && payment.getMethod() == PaymentMethod.COD && payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order); // Lưu Order để cập nhật Payment
        }

        OrderShop savedOrderShop = orderShopRepository.save(orderShop);

        return orderShopMapper.toDto(savedOrderShop);
    }

    @Override
    @Transactional
    public OrderShopResponseDto requestReturn(Long orderShopId) {
        OrderShop orderShop = orderShopRepository.findById(orderShopId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShop not found with id: " + orderShopId));

        if (orderShop.getStatus() != OrderStatus.DELIVERED && orderShop.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("OrderShop must be in DELIVERED or COMPLETED status to request a return.");
        }

        orderShop.setStatus(OrderStatus.RETURN_REQUESTED);
        OrderShop savedOrderShop = orderShopRepository.save(orderShop);

        return orderShopMapper.toDto(savedOrderShop);
    }

    @Override
    @Transactional
    public OrderShopResponseDto cancelOrderShop(Long orderShopId) {
        OrderShop orderShop = orderShopRepository.findById(orderShopId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShop not found with id: " + orderShopId));

        if (orderShop.getStatus() != OrderStatus.PENDING && orderShop.getStatus() != OrderStatus.CONFIRMED && orderShop.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalArgumentException("OrderShop can only be cancelled if in PENDING, CONFIRMED or PROCESSING status.");
        }

        orderShop.setStatus(OrderStatus.CANCELLED);

        // Cập nhật OrderShipping liên quan
        if (orderShop.getShipping() != null) {
            orderShop.getShipping().setShippingStatus(ShippingStatus.CANCELLED);
        }

        orderService.replenishStock(orderShop); // Hoàn kho

        // Xử lý hoàn tiền nếu đã thanh toán online
        Order order = orderShop.getOrder();
        Payment payment = order.getPayment();
        if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            payment.setStatus(PaymentStatus.REFUND_INITIATED);
            // Gọi PaymentService để xử lý hoàn tiền thực tế
            // paymentService.initiateRefund(payment.getId(), orderShop.getFinalPrice()); // Giả định có phương thức này
            orderRepository.save(order); // Lưu Order để cập nhật Payment
        }

        OrderShop savedOrderShop = orderShopRepository.save(orderShop);
        return orderShopMapper.toDto(savedOrderShop);
    }

    @Override
    @Transactional
    public OrderShopResponseDto confirmOrder(Long orderShopId) {
        OrderShop orderShop = orderShopRepository.findById(orderShopId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderShop not found with id: " + orderShopId));

        if (orderShop.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("OrderShop must be in PENDING status to be confirmed.");
        }

        orderShop.setStatus(OrderStatus.CONFIRMED);
        orderService.deductStock(orderShop); // Trừ kho khi xác nhận đơn hàng

        OrderShop savedOrderShop = orderShopRepository.save(orderShop);
        return orderShopMapper.toDto(savedOrderShop);
    }
}
