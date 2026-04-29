package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "product_brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long id;

    private String name;
    private String description;

    // Xóa Set<Product> products; -> Truy vấn qua ProductRepository
}