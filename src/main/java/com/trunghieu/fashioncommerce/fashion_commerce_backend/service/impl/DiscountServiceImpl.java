package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.DiscountRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.DiscountResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.DiscountMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.DiscountRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final DiscountMapper discountMapper;

    @Override
    @Transactional
    public DiscountResponseDto createDiscount(DiscountRequestDto requestDto) {
        Shop shop = shopRepository.findById(requestDto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + requestDto.getShopId()));

        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Discount discount = discountMapper.toEntity(requestDto);
        discount.setShop(shop);
        discount.setDiscountType(DiscountType.valueOf(requestDto.getDiscountType().toUpperCase()));
        discount.setStatus(requestDto.getStatus());
        discount.setProducts(resolveProducts(requestDto.getProductIds()));

        return discountMapper.toDto(discountRepository.save(discount));
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponseDto getDiscountById(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        return discountMapper.toDto(discount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscountResponseDto> getDiscountsByShopId(Long shopId, Pageable pageable) {
        return discountRepository.findByShopId(shopId, pageable)
                .map(discountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponseDto> getActiveDiscountsByShopId(Long shopId) {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findByShopIdAndStatusAndStartDateBeforeAndEndDateAfter(shopId, DiscountStatus.ACTIVE, now, now)
                .stream()
                .map(discountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiscountResponseDto updateDiscount(Long id, DiscountRequestDto requestDto) {
        Discount existing = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        existing.setDiscountType(DiscountType.valueOf(requestDto.getDiscountType().toUpperCase()));
        existing.setDiscountValue(requestDto.getDiscountValue());
        existing.setStartDate(requestDto.getStartDate());
        existing.setEndDate(requestDto.getEndDate());
        existing.setStatus(requestDto.getStatus());
        existing.setMinOrderValue(requestDto.getMinOrderValue());
        existing.setProducts(resolveProducts(requestDto.getProductIds()));

        return discountMapper.toDto(discountRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        if (!discountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Discount not found with id: " + id);
        }
        discountRepository.deleteById(id);
    }

    private Set<Product> resolveProducts(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("One or more products not found with provided ids");
        }
        return new HashSet<>(products);
    }
}
