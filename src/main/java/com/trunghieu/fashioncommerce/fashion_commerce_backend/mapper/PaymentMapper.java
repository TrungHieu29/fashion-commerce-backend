package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.PaymentRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.PaymentResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Payment;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", imports = {PaymentStatus.class, PaymentMethod.class}) // Đã thêm imports cho cả PaymentStatus và PaymentMethod
public interface PaymentMapper {



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true) // Order sẽ được set trong service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", expression = "java(PaymentStatus.PENDING)") // Mặc định status là PENDING
    Payment toEntity(PaymentRequestDto paymentRequestDto);

    PaymentResponseDto toDto(Payment payment);
}
