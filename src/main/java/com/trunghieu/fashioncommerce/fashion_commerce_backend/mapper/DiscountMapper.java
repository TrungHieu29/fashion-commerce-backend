package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.DiscountRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.DiscountResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product; // Import Product
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {DiscountStatus.class, DiscountType.class})
public interface DiscountMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "products", ignore = true) // Products sẽ được set trong service
    Discount toEntity(DiscountRequestDto dto);

    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "discountTarget", target = "discountTarget")
    @Mapping(source = "entity.products", target = "productIds", qualifiedByName = "mapProductsToIds") // Ánh xạ products sang productIds
    DiscountResponseDto toDto(Discount entity);

    @Named("mapProductsToIds")
    default Set<Long> mapProductsToIds(Set<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
    }
}
