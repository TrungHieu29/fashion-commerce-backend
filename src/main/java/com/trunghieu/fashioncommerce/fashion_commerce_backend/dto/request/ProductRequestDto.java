package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
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
public class ProductRequestDto {
    @NotBlank(message = "Product name cannot be blank")
    private String productName;

    private String productDetail;

    @NotNull(message = "Status cannot be null")
    private ProductStatus status;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Shop ID cannot be null")
    private Long shopId;

    @NotNull(message = "Brand ID cannot be null")
    private Long brandId;

    @NotEmpty(message = "At least one category must be selected")
    private Set<Long> categoryIds;
}