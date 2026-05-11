package com.trunghieu.fashioncommerce.fashion_commerce_backend.security;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.CartRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ConversationRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.DiscountRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderItemRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShippingRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.PaymentRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository; // Import ProductVariantRepository
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ReviewRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShippingAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository; // Inject ProductVariantRepository
    private final OrderRepository orderRepository;
    private final OrderShopRepository orderShopRepository;
    private final OrderShippingRepository orderShippingRepository;
    private final OrderItemRepository orderItemRepository;
    private final ConversationRepository conversationRepository;
    private final ReviewRepository reviewRepository;
    private final DiscountRepository discountRepository;
    private final PaymentRepository paymentRepository;

    public boolean isShopOwner(Long shopId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
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
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return productRepository.findById(productId)
                .map(product -> product.getShop() != null && product.getShop().getOwner() != null
                        && product.getShop().getOwner().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isShippingAddressOwner(Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return shippingAddressRepository.findById(addressId)
                .map(address -> address.getUser() != null && address.getUser().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isCartOwner(Long cartId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return cartRepository.findById(cartId)
                .map(cart -> cart.getUser() != null && cart.getUser().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isOrderOwner(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return orderRepository.findById(orderId)
                .map(order -> order.getUser() != null && order.getUser().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isOrderShopOwner(Long orderShopId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return orderShopRepository.findById(orderShopId)
                .map(orderShop -> {
                    boolean isSeller = orderShop.getShop() != null && orderShop.getShop().getOwner() != null
                            && orderShop.getShop().getOwner().getId().equals(currentUserId);
                    boolean isBuyer = orderShop.getOrder() != null && orderShop.getOrder().getUser() != null
                            && orderShop.getOrder().getUser().getId().equals(currentUserId);
                    return isSeller || isBuyer;
                })
                .orElse(false);
    }

    public boolean isOrderShippingOwner(Long orderShippingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return orderShippingRepository.findById(orderShippingId)
                .map(shipping -> {
                    boolean isSeller = shipping.getOrderShop() != null && shipping.getOrderShop().getShop() != null &&
                            shipping.getOrderShop().getShop().getOwner() != null
                            && shipping.getOrderShop().getShop().getOwner().getId().equals(currentUserId);
                    boolean isBuyer = shipping.getOrderShop() != null && shipping.getOrderShop().getOrder() != null &&
                            shipping.getOrderShop().getOrder().getUser() != null
                            && shipping.getOrderShop().getOrder().getUser().getId().equals(currentUserId);
                    return isSeller || isBuyer;
                })
                .orElse(false);
    }

    public boolean isOrderItemOwner(Long orderItemId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return orderItemRepository.findById(orderItemId)
                .map(item -> {
                    boolean isSeller = item.getOrderShop() != null && item.getOrderShop().getShop() != null &&
                            item.getOrderShop().getShop().getOwner() != null
                            && item.getOrderShop().getShop().getOwner().getId().equals(currentUserId);
                    boolean isBuyer = item.getOrderShop() != null && item.getOrderShop().getOrder() != null &&
                            item.getOrderShop().getOrder().getUser() != null
                            && item.getOrderShop().getOrder().getUser().getId().equals(currentUserId);
                    return isSeller || isBuyer;
                })
                .orElse(false);
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải là chủ sở hữu của biến thể sản phẩm
     * có ID đã cho hay không.
     * Quyền sở hữu biến thể được xác định bởi quyền sở hữu sản phẩm, và từ đó là
     * quyền sở hữu shop.
     * 
     * @param productVariantId ID của biến thể sản phẩm cần kiểm tra.
     * @return true nếu người dùng hiện tại là chủ sở hữu của shop chứa sản phẩm đó,
     *         ngược lại là false.
     */
    public boolean isProductVariantOwner(Long productVariantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return productVariantRepository.findById(productVariantId)
                .map(productVariant -> productVariant.getProduct() != null &&
                        productVariant.getProduct().getShop() != null &&
                        productVariant.getProduct().getShop().getOwner() != null &&
                        productVariant.getProduct().getShop().getOwner().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isConversationParticipant(Long conversationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return conversationRepository.findById(conversationId)
                .map(conversation -> {
                    boolean isUser = conversation.getUser() != null
                            && conversation.getUser().getId().equals(currentUserId);
                    boolean isShopOwner = conversation.getShop() != null && conversation.getShop().getOwner() != null
                            && conversation.getShop().getOwner().getId().equals(currentUserId);
                    return isUser || isShopOwner;
                })
                .orElse(false);
    }

    public boolean isReviewOwner(Long reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return reviewRepository.findById(reviewId)
                .map(review -> review.getUser() != null && review.getUser().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isDiscountShopOwner(Long discountId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return discountRepository.findById(discountId)
                .map(discount -> discount.getShop() != null && discount.getShop().getOwner() != null
                        && discount.getShop().getOwner().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isPaymentOwner(Long paymentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        return paymentRepository.findById(paymentId)
                .map(payment -> payment.getOrder() != null && payment.getOrder().getUser() != null
                        && payment.getOrder().getUser().getId().equals(currentUserId))
                .orElse(false);
    }
}
