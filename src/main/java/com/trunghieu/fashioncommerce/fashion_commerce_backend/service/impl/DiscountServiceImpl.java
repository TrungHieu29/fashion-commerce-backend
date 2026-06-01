package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.DiscountRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.DiscountResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountTarget;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        validateDiscountRules(requestDto);

        Shop shop = shopRepository.findById(requestDto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + requestDto.getShopId()));

        Discount discount = discountMapper.toEntity(requestDto);
        discount.setShop(shop);
        discount.setDiscountTarget(DiscountTarget.valueOf(requestDto.getDiscountTarget().toUpperCase()));
        discount.setDiscountType(DiscountType.valueOf(requestDto.getDiscountType().toUpperCase()));
        discount.setStatus(requestDto.getStatus());
        discount.setProducts(resolveProducts(requestDto.getProductIds()));

        return discountMapper.toDto(discountRepository.save(discount));
    }

    private void validateDiscountRules(DiscountRequestDto dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (dto.getDiscountValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0");
        }

        DiscountTarget target = DiscountTarget.valueOf(dto.getDiscountTarget().toUpperCase());
        switch (target) {
            case PRODUCT:
                if (dto.getProductIds() == null || dto.getProductIds().isEmpty())
                    throw new IllegalArgumentException("PRODUCT target requires productIds");
                break;
            case SHOP:
            case ORDER:
                if (dto.getProductIds() != null && !dto.getProductIds().isEmpty())
                    throw new IllegalArgumentException(target + " target must not have productIds");
                if (target == DiscountTarget.ORDER) {
                    if (dto.getCode() == null || dto.getCode().isBlank())
                        throw new IllegalArgumentException("ORDER target requires code");
                    if (dto.getMinOrderValue() == null)
                        throw new IllegalArgumentException("ORDER target requires minOrderValue");
                }
                break;
        }
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
        return discountRepository
                .findByShopIdAndStatusAndStartDateBeforeAndEndDateAfter(shopId, DiscountStatus.ACTIVE, now, now)
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
        existing.setDiscountTarget(DiscountTarget.valueOf(requestDto.getDiscountTarget().toUpperCase()));
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

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBestDiscount(Long shopId, Long productId, BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Discount> activeDiscounts = discountRepository
                .findByShopIdAndStatusAndStartDateBeforeAndEndDateAfter(shopId, DiscountStatus.ACTIVE, now, now);

        if (activeDiscounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxDiscountAmount = BigDecimal.ZERO;

        for (Discount d : activeDiscounts) {
            boolean canApply = false;
            if (d.getDiscountTarget() == DiscountTarget.SHOP) {
                canApply = true;
            } else if (d.getDiscountTarget() == DiscountTarget.PRODUCT) {
                // Quan trọng: Kiểm tra sự tồn tại của productId trong danh sách áp dụng
                canApply = d.getProducts().stream().anyMatch(p -> p.getId().equals(productId));
            }

            if (canApply) {
                BigDecimal currentAmount = calculateDiscountAmount(d, originalPrice);
                // So sánh lấy giá trị giảm lớn nhất
                if (currentAmount.compareTo(maxDiscountAmount) > 0) {
                    maxDiscountAmount = currentAmount;
                }
            }
        }

        return maxDiscountAmount;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal applyOrderVoucher(Long shopId, String voucherCode, BigDecimal subtotal, LocalDateTime currentDateTime) {
        if (voucherCode == null || voucherCode.isBlank() || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return discountRepository
                .findByShopIdAndCodeAndStatus(shopId, voucherCode, DiscountStatus.ACTIVE)
                .filter(d -> d.getDiscountTarget() == DiscountTarget.ORDER)
                .filter(d -> d.getStartDate().isBefore(currentDateTime) && d.getEndDate().isAfter(currentDateTime)) // Sử dụng currentDateTime
                .filter(d -> d.getMinOrderValue() == null || subtotal.compareTo(d.getMinOrderValue()) >= 0)
                .map(d -> calculateDiscountAmount(d, subtotal))
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateDiscountAmount(Discount discount, BigDecimal price) {
        if (discount.getDiscountType() == DiscountType.PERCENT) {
            // Công thức: (Giá * %Giảm) / 100
            // Quan trọng: Phải có RoundingMode.HALF_UP để tránh lỗi ArithmeticException và
            // sai số
            return price.multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        // Nếu là FIXED, trả về giá trị giảm trực tiếp
        return discount.getDiscountValue();
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
