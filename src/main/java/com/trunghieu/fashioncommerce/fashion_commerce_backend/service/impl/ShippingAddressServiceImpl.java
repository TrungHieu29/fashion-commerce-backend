package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShippingAddressRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShippingAddressResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ShippingAddress;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ShippingAddressMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShippingAddressRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final ShippingAddressMapper shippingAddressMapper;
    private final UserRepository userRepository; // Cần để kiểm tra user tồn tại

    @Override
    @Transactional
    public ShippingAddressResponseDto createShippingAddress(ShippingAddressRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getUserId()));

        ShippingAddress shippingAddress = shippingAddressMapper.toEntity(requestDto);
        shippingAddress.setUser(user);

        // Nếu địa chỉ mới được đặt làm mặc định, hủy đặt mặc định cho các địa chỉ khác của user này
        if (Boolean.TRUE.equals(shippingAddress.getIsDefault())) {
            shippingAddressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        shippingAddressRepository.save(addr);
                    });
        } else {
            // Nếu không có địa chỉ nào khác và địa chỉ này không được đặt mặc định, đặt nó làm mặc định
            if (shippingAddressRepository.findByUserId(user.getId()).isEmpty()) {
                shippingAddress.setIsDefault(true);
            }
        }

        shippingAddress = shippingAddressRepository.save(shippingAddress);
        return shippingAddressMapper.toDto(shippingAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingAddressResponseDto getShippingAddressById(Long id) {
        ShippingAddress shippingAddress = shippingAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Address not found with id: " + id));
        return shippingAddressMapper.toDto(shippingAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressResponseDto> getShippingAddressesByUserId(Long userId) {
        userRepository.findById(userId) // Kiểm tra user tồn tại
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return shippingAddressRepository.findByUserId(userId).stream()
                .map(shippingAddressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressResponseDto> getAllShippingAddresses() {
        return shippingAddressRepository.findAll().stream()
                .map(shippingAddressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingAddressResponseDto updateShippingAddress(Long id, ShippingAddressRequestDto requestDto) {
        ShippingAddress existingAddress = shippingAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Address not found with id: " + id));

        // Kiểm tra user tồn tại (nếu userId trong requestDto khác với user hiện tại của địa chỉ)
        if (!existingAddress.getUser().getId().equals(requestDto.getUserId())) {
            userRepository.findById(requestDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getUserId()));
            // Nếu thay đổi user, cần xử lý lại logic isDefault cho user cũ và mới
            // Hiện tại, giả định userId không thay đổi khi update địa chỉ
        }

        existingAddress.setReceiverName(requestDto.getReceiverName());
        existingAddress.setPhone(requestDto.getPhone());
        existingAddress.setAddressLine(requestDto.getAddressLine());
        existingAddress.setCity(requestDto.getCity());
        existingAddress.setDistrict(requestDto.getDistrict());

        // Xử lý logic đặt mặc định
        if (Boolean.TRUE.equals(requestDto.getIsDefault()) && Boolean.FALSE.equals(existingAddress.getIsDefault())) {
            // Nếu địa chỉ này được đặt làm mặc định, hủy đặt mặc định cho các địa chỉ khác của user này
            shippingAddressRepository.findByUserIdAndIsDefaultTrue(existingAddress.getUser().getId())
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        shippingAddressRepository.save(addr);
                    });
            existingAddress.setIsDefault(true);
        } else if (Boolean.FALSE.equals(requestDto.getIsDefault()) && Boolean.TRUE.equals(existingAddress.getIsDefault())) {
            // Nếu địa chỉ này bị hủy đặt mặc định, và không còn địa chỉ nào khác, cần xử lý
            // Hiện tại, không cho phép hủy đặt mặc định nếu đây là địa chỉ duy nhất
            if (shippingAddressRepository.findByUserId(existingAddress.getUser().getId()).size() == 1) {
                throw new IllegalArgumentException("Cannot unset default for the only shipping address.");
            }
            existingAddress.setIsDefault(false);
        } else {
            existingAddress.setIsDefault(requestDto.getIsDefault());
        }


        existingAddress = shippingAddressRepository.save(existingAddress);
        return shippingAddressMapper.toDto(existingAddress);
    }

    @Override
    @Transactional
    public void deleteShippingAddress(Long id) {
        ShippingAddress existingAddress = shippingAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Address not found with id: " + id));

        // Không cho phép xóa địa chỉ mặc định nếu đây là địa chỉ duy nhất
        if (Boolean.TRUE.equals(existingAddress.getIsDefault()) && shippingAddressRepository.findByUserId(existingAddress.getUser().getId()).size() == 1) {
            throw new IllegalArgumentException("Cannot delete the only default shipping address.");
        }

        shippingAddressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ShippingAddressResponseDto setDefaultShippingAddress(Long id, Long userId) {
        ShippingAddress addressToSetDefault = shippingAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Address not found with id: " + id));

        if (!addressToSetDefault.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Shipping Address with id " + id + " does not belong to user with id " + userId);
        }

        // Hủy đặt mặc định cho các địa chỉ khác của user này
        shippingAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                .forEach(addr -> {
                    addr.setIsDefault(false);
                    shippingAddressRepository.save(addr);
                });

        // Đặt địa chỉ này làm mặc định
        addressToSetDefault.setIsDefault(true);
        addressToSetDefault = shippingAddressRepository.save(addressToSetDefault);
        return shippingAddressMapper.toDto(addressToSetDefault);
    }
}
