package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ShopMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ShopResponseDto createShop(ShopRequestDto requestDto) {
        // Check if shop name already exists
        if (shopRepository.existsByShopName(requestDto.getShopName())) {
            throw new IllegalArgumentException("Shop name already exists: " + requestDto.getShopName());
        }

        // Check if owner exists
        User owner = userRepository.findById(requestDto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getOwnerId()));

        Shop shop = shopMapper.toEntity(requestDto);
        shop.setOwner(owner);

        Shop savedShop = shopRepository.save(shop);
        return shopMapper.toDto(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponseDto getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));
        return shopMapper.toDto(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDto> getAllShops() {
        return shopRepository.findAll().stream()
                .map(shopMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShopResponseDto updateShop(Long id, ShopRequestDto requestDto) {
        Shop existingShop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));

        // Check if shop name is being changed and if it already exists
        if (!existingShop.getShopName().equals(requestDto.getShopName()) &&
                shopRepository.existsByShopName(requestDto.getShopName())) {
            throw new IllegalArgumentException("Shop name already exists: " + requestDto.getShopName());
        }

        // Update fields
        existingShop.setShopName(requestDto.getShopName());
        existingShop.setLogo(requestDto.getLogo());
        existingShop.setPhone(requestDto.getPhone());
        existingShop.setAddress(requestDto.getAddress());
        existingShop.setEmail(requestDto.getEmail());

        // Update owner if provided
        if (requestDto.getOwnerId() != null) {
            User owner = userRepository.findById(requestDto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getOwnerId()));
            existingShop.setOwner(owner);
        }

        Shop updatedShop = shopRepository.save(existingShop);
        return shopMapper.toDto(updatedShop);
    }

    @Override
    @Transactional
    public void deleteShop(Long id) {
        if (!shopRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shop not found with id: " + id);
        }
        shopRepository.deleteById(id);
    }
}