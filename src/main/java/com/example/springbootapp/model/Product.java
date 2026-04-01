package com.example.springbootapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String name;

    @Column(length = 2000)
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private double price;

    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private int stockQuantity;

    private String imageUrl;
}
