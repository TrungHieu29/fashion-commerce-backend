package com.trunghieu.fashioncommerce.fashion_commerce_backend.security;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShippingAddressRepository; // Import ShippingAddressRepository
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ShippingAddressRepository shippingAddressRepository; // Inject ShippingAddressRepository

    public boolean isShopOwner(Long shopId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return shopRepository.findById(shopId)
                .map(shop -> shop.getOwner() != null && shop.getOwner().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isProductOwner(Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return productRepository.findById(productId)
                .map(product -> product.getShop() != null && product.getShop().getOwner() != null && product.getShop().getOwner().getId().equals(currentUserId))
                .orElse(false);
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải là chủ sở hữu của địa chỉ giao hàng có ID đã cho hay không.
     * @param addressId ID của địa chỉ giao hàng cần kiểm tra.
     * @return true nếu người dùng hiện tại là chủ sở hữu, ngược lại là false.
     */
    public boolean isShippingAddressOwner(Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return shippingAddressRepository.findById(addressId)
                .map(address -> address.getUser() != null && address.getUser().getId().equals(currentUserId))
                .orElse(false);
    }
}
