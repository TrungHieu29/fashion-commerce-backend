package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.security.CustomUserDetails;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#requestDto.shopId)")
    // ADMIN có thể tạo sản phẩm cho bất kỳ shop nào. Chủ shop chỉ có thể tạo sản phẩm cho shop của mình.
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto requestDto, Authentication authentication) {
        // Kiểm tra bổ sung nếu là CUSTOMER, đảm bảo ownerId trong request khớp với ID của người dùng đang đăng nhập
        if (authentication.getPrincipal() instanceof CustomUserDetails && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            // Logic kiểm tra này đã được bao gồm trong @PreAuthorize, nhưng có thể cần thêm nếu @PreAuthorize không thể truy cập #requestDto.shopId một cách đáng tin cậy
            // hoặc nếu bạn muốn ném một ngoại lệ cụ thể hơn.
            // Tuy nhiên, @securityUtils.isShopOwner(#requestDto.shopId) đã xử lý việc này.
        }
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem sản phẩm theo ID
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem tất cả sản phẩm
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem sản phẩm theo Shop ID
    public ResponseEntity<Page<ProductResponseDto>> getProductsByShopId(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProductsByShopId(shopId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem sản phẩm theo Category ID
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProductsByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brand/{brandId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem sản phẩm theo Brand ID
    public ResponseEntity<Page<ProductResponseDto>> getProductsByBrandId(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getProductsByBrandId(brandId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể tìm kiếm sản phẩm
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductOwner(#id)")
    // ADMIN có thể cập nhật bất kỳ sản phẩm nào. Chủ shop chỉ có thể cập nhật sản phẩm của mình.
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductOwner(#id)")
    // ADMIN có thể xóa bất kỳ sản phẩm nào. Chủ shop chỉ có thể xóa sản phẩm của mình.
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
