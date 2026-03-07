package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Product;
import com.manish.spring.security.Repository.ProductRepository;
import com.manish.spring.security.dto.ProductDTO;
import com.manish.spring.security.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // ─── getAllProducts ───────────────────────────────────────────────────────

    @Test
    void getAllProducts_returnsMappedDTOsForAllProducts() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Product p1 = new Product(1L, "Widget", "A widget", BigDecimal.TEN, 5, now, null);
        Product p2 = new Product(2L, "Gadget", "A gadget", BigDecimal.ONE, 10, now, null);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Widget");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Gadget");
        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_returnsEmptyListWhenNoProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertThat(result).isEmpty();
        verify(productRepository).findAll();
    }

    // ─── getProduct ──────────────────────────────────────────────────────────

    @Test
    void getProduct_returnsDTOWhenFound() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product(1L, "Widget", "A widget", BigDecimal.TEN, 5, now, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        ProductDTO result = productService.getProduct(1L);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Widget");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        verify(productRepository).findById(1L);
    }

    @Test
    void getProduct_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("99");
        verify(productRepository).findById(99L);
    }

    // ─── createProduct ───────────────────────────────────────────────────────

    @Test
    void createProduct_savesEntityAndReturnsMappedDTO() {
        // Arrange
        ProductDTO dto = new ProductDTO(null, "Widget", "A widget", BigDecimal.TEN, 5, null);
        Product savedProduct = new Product(1L, "Widget", "A widget", BigDecimal.TEN, 5, LocalDateTime.now(), null);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDTO result = productService.createProduct(dto);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Widget");
        assertThat(result.getStock()).isEqualTo(5);
        verify(productRepository).save(any(Product.class));
    }

    // ─── updateProduct ───────────────────────────────────────────────────────

    @Test
    void updateProduct_updatesFieldsAndReturnsMappedDTO() {
        // Arrange
        LocalDateTime created = LocalDateTime.now();
        Product existing = new Product(1L, "Old Name", "Old desc", BigDecimal.ONE, 1, created, null);
        ProductDTO updateDTO = new ProductDTO(null, "New Name", "New desc", BigDecimal.TEN, 99, null);
        Product savedProduct = new Product(1L, "New Name", "New desc", BigDecimal.TEN, 99, created, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(savedProduct);

        // Act
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Assert
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(result.getStock()).isEqualTo(99);
        verify(productRepository).findById(1L);
        verify(productRepository).save(existing);
    }

    @Test
    void updateProduct_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(99L, new ProductDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
        verify(productRepository, never()).save(any());
    }

    // ─── deleteProduct ───────────────────────────────────────────────────────

    @Test
    void deleteProduct_deletesProductById() {
        // Arrange
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).deleteById(1L);
    }
}
