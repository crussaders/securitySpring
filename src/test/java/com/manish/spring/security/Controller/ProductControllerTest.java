package com.manish.spring.security.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manish.spring.security.dto.ProductDTO;
import com.manish.spring.security.exception.ResourceNotFoundException;
import com.manish.spring.security.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
    }

    private ProductDTO buildProductDTO(Long id, String name) {
        return new ProductDTO(id, name, "A description", new BigDecimal("19.99"), 50, null);
    }

    @Test
    void getProducts_returnsListOfProducts() throws Exception {
        ProductDTO product = buildProductDTO(1L, "Widget");
        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Widget"));
    }

    @Test
    void getProduct_returnsProduct_whenFound() throws Exception {
        ProductDTO product = buildProductDTO(1L, "Widget");
        when(productService.getProduct(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    void getProduct_returns404_whenNotFound() throws Exception {
        when(productService.getProduct(99L)).thenThrow(new ResourceNotFoundException("Product", 99L));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_returnsCreatedProduct() throws Exception {
        ProductDTO input = buildProductDTO(null, "Gadget");
        ProductDTO saved = buildProductDTO(2L, "Gadget");
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Gadget"));
    }

    @Test
    void updateProduct_returnsUpdatedProduct() throws Exception {
        ProductDTO input = buildProductDTO(null, "Widget Pro");
        ProductDTO updated = buildProductDTO(1L, "Widget Pro");
        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Widget Pro"));
    }

    @Test
    void updateProduct_returns404_whenNotFound() throws Exception {
        ProductDTO input = buildProductDTO(null, "Widget Pro");
        when(productService.updateProduct(eq(99L), any(ProductDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product", 99L));

        mockMvc.perform(put("/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_returns200_onSuccess() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk());
    }
}
