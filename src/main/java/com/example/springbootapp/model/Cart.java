package com.example.springbootapp.model;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "cart", fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    @PostLoad
    @PostConstruct
    public void init() {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
    }

    public List<CartItem> getItems() {
        // Ensure items list is never null
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<CartItem> items) { 
        this.items = items != null ? items : new ArrayList<>(); 
    }
}
