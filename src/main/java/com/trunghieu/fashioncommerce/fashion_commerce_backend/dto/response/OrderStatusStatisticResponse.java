package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusStatisticResponse {

    private String status;

    private Long count;
}