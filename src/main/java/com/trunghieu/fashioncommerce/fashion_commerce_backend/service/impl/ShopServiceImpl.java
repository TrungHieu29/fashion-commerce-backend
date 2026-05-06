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
    private final UserRepository userRepository; // Cần để gán owner

    @Override
    @Transactional
    public ShopResponseDto createShop(ShopRequestDto requestDto) {
        // Kiểm tra xem shopName đã tồn tại chưa
        if (shopRepository.existsByShopName(requestDto.getShopName())) {
            throw new IllegalArgumentException("Shop with name '" + requestDto.getShopName() + "' already exists.");
        }
        // Kiểm tra xem ownerId có tồn tại không
        User owner = userRepository.findById(requestDto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User (owner) not found with id: " + requestDto.getOwnerId()));

        // Kiểm tra xem user này đã có shop chưa (mối quan hệ OneToOne)
        if (shopRepository.findByOwnerId(requestDto.getOwnerId()).isPresent()) {
            throw new IllegalArgumentException("User with id " + requestDto.getOwnerId() + " already owns a shop.");
        }

        Shop shop = shopMapper.toEntity(requestDto);
        shop.setOwner(owner); // Gán owner
        shop = shopRepository.save(shop);
        return shopMapper.toDto(shop);
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
    public ShopResponseDto getShopByOwnerId(Long ownerId) {
        Shop shop = shopRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for owner with id: " + ownerId));
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

        // Kiểm tra tên trùng lặp, ngoại trừ chính shop đang cập nhật
        if (shopRepository.existsByShopName(requestDto.getShopName()) &&
            !existingShop.getShopName().equals(requestDto.getShopName())) {
            throw new IllegalArgumentException("Shop with name '" + requestDto.getShopName() + "' already exists.");
        }

        existingShop.setShopName(requestDto.getShopName());
        existingShop.setLogo(requestDto.getLogo());
        existingShop.setPhone(requestDto.getPhone());
        existingShop.setAddress(requestDto.getAddress());
        existingShop.setEmail(requestDto.getEmail());
        // Không cho phép cập nhật ownerId qua đây, owner chỉ được set khi tạo shop

        existingShop = shopRepository.save(existingShop);
        return shopMapper.toDto(existingShop);
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
