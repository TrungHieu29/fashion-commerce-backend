package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ProductStatus; // Import ProductStatus
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private String productName;
    private String productDetail;
    private Double rating;
    private ProductStatus status; // Đã thay đổi từ Integer sang ProductStatus
    private BigDecimal price;
    private Long shopId;
    private String shopName; // Thêm tên shop để hiển thị
    private Long brandId;
    private String brandName; // Thêm tên brand
    private Long categoryId;
    private String categoryName; // Thêm tên category
    // Có thể thêm List<ProductImageResponseDto> images; và List<ProductVariantResponseDto> variants; sau
}
