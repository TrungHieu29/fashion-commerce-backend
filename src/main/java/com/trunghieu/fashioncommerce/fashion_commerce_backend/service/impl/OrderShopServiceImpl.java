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
