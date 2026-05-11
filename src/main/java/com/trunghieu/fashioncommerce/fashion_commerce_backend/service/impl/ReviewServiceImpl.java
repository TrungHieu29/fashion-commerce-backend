package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ReviewRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ReviewResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Review;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ReviewMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderItemRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ReviewRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getUserId()));
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product not found with id: " + requestDto.getProductId()));
        OrderItem orderItem = orderItemRepository.findById(requestDto.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order item not found with id: " + requestDto.getOrderItemId()));

        // Kiểm tra bảo mật: OrderItem phải thuộc về User đang đánh giá
        if (!orderItem.getOrderShop().getOrder().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không thể đánh giá sản phẩm từ đơn hàng của người khác.");
        }

        // Kiểm tra tính nhất quán: ProductId trong request phải khớp với sản phẩm trong
        // OrderItem
        if (!orderItem.getProductVariant().getProduct().getId().equals(product.getId())) {
            throw new IllegalArgumentException("Sản phẩm đánh giá không khớp với sản phẩm trong đơn hàng.");
        }

        // Kiểm tra điều kiện: Chỉ cho phép đánh giá khi đơn hàng đã được giao thành
        // công
        if (orderItem.getOrderShop().getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Bạn chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao thành công.");
        }

        Review review = reviewMapper.toEntity(requestDto);
        review.setUser(user);
        review.setProduct(product);
        review.setOrderItem(orderItem);

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByProductId(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Long id, ReviewRequestDto requestDto) {
        Review existing = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        existing.setComment(requestDto.getComment());
        existing.setRating(requestDto.getRating());
        return reviewMapper.toDto(reviewRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
}
