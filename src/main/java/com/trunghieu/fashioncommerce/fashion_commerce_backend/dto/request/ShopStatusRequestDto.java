package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShopStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopStatusRequestDto {
    private ShopStatus status;
}