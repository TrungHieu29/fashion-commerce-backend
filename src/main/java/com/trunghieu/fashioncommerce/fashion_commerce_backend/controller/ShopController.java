package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<ShopResponseDto> createShop(@Valid @RequestBody ShopRequestDto requestDto) {
        ShopResponseDto createdShop = shopService.createShop(requestDto);
        return new ResponseEntity<>(createdShop, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable Long id) {
        ShopResponseDto shop = shopService.getShopById(id);
        return ResponseEntity.ok(shop);
    }

    @GetMapping
    public ResponseEntity<List<ShopResponseDto>> getAllShops() {
        List<ShopResponseDto> shops = shopService.getAllShops();
        return ResponseEntity.ok(shops);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopResponseDto> updateShop(
            @PathVariable Long id,
            @Valid @RequestBody ShopRequestDto requestDto) {
        ShopResponseDto updatedShop = shopService.updateShop(id, requestDto);
        return ResponseEntity.ok(updatedShop);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}