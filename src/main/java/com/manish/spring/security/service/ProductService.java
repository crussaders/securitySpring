package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Product;
import com.manish.spring.security.Repository.ProductRepository;
import com.manish.spring.security.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDTO::from)
                .toList();
    }

    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductDTO.from(product);
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product saved = productRepository.save(productDTO.toEntity());
        return ProductDTO.from(saved);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existing.setName(productDTO.getName());
        existing.setDescription(productDTO.getDescription());
        existing.setPrice(productDTO.getPrice());
        existing.setStock(productDTO.getStock());

        return ProductDTO.from(productRepository.save(existing));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
