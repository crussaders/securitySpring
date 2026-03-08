package com.manish.spring.security.cucumber.steps;

import com.manish.spring.security.Entity.Product;
import com.manish.spring.security.Repository.ProductRepository;
import com.manish.spring.security.cucumber.context.TestContext;
import com.manish.spring.security.dto.ProductDTO;
import com.manish.spring.security.exception.ResourceNotFoundException;
import com.manish.spring.security.service.ProductService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Step definitions for the "Product Management" feature.
 *
 * <p>Design note – DTOs vs entities:
 * ProductService works exclusively with {@link ProductDTO} objects for input/output,
 * mapping to and from the {@link Product} entity internally.  These step definitions
 * therefore deal in ProductDTOs rather than Product entities, mirroring the service API.
 *
 * <p>The mock ProductRepository is configured so that:
 * <ul>
 *   <li>findAll() / findById() return entities (the service converts them to DTOs).</li>
 *   <li>save() returns the saved entity (the service then wraps it in a DTO).</li>
 * </ul>
 */
public class ProductSteps {

    // ── Mocks ────────────────────────────────────────────────────────────────

    /**
     * Mock repository – replaces the real JPA repository so no database is needed.
     */
    private final ProductRepository productRepository = mock(ProductRepository.class);

    /**
     * The real ProductService under test, populated with the mock repository
     * via reflection in the constructor.
     */
    private final ProductService productService;

    /**
     * Shared state across steps within a scenario, injected by Cucumber.
     */
    private final TestContext ctx;

    // ── Constructor ──────────────────────────────────────────────────────────

    public ProductSteps(TestContext ctx) throws Exception {
        this.ctx = ctx;
        this.productService = new ProductService();
        java.lang.reflect.Field field = ProductService.class.getDeclaredField("productRepository");
        field.setAccessible(true);
        field.set(productService, productRepository);
    }

    // ── @Before ──────────────────────────────────────────────────────────────

    /**
     * Resets the mock before each scenario to prevent stub bleed-over.
     */
    @Before
    public void resetMocks() {
        reset(productRepository);
    }

    // ── @Given steps ─────────────────────────────────────────────────────────

    /**
     * Simulates an empty product catalogue by making findAll() return an empty list.
     * Used as the Background step so every scenario starts cleanly.
     */
    @Given("the product repository is empty")
    public void theProductRepositoryIsEmpty() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
    }

    /**
     * Seeds the mock repository with a single Product so look-ups will succeed.
     * Both findAll() and findById() are stubbed to return this product.
     *
     * @param id    the product's identifier
     * @param name  the product name
     * @param price the unit price
     * @param stock available stock quantity
     */
    @Given("the product repository contains a product with id {long}, name {string}, price {double} and stock {int}")
    public void theProductRepositoryContainsProduct(long id, String name, double price, int stock) {
        Product product = new Product(id, name, "A description", BigDecimal.valueOf(price),
                stock, null, null);

        // Stub the "list all" repository method
        when(productRepository.findAll()).thenReturn(List.of(product));

        // Stub the "find by id" repository method
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // Stub save to return the same entity (used by updateProduct)
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /**
     * Prepares a new ProductDTO to be created.  Stores it in TestContext so the
     * @When "I create the product" step can retrieve it.
     * Also stubs the repository's save() to simulate DB id-assignment.
     */
    @Given("I have a new product with name {string}, description {string}, price {double} and stock {int}")
    public void iHaveANewProduct(String name, String description, double price, int stock) {
        ProductDTO dto = new ProductDTO(null, name, description, BigDecimal.valueOf(price), stock, null);
        ctx.setResult(dto);

        // save() will return a Product with id=1, simulating DB auto-increment
        Product saved = new Product(1L, name, description, BigDecimal.valueOf(price), stock, null, null);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
    }

    // ── @When steps ──────────────────────────────────────────────────────────

    /**
     * Calls ProductService.getAllProducts() which returns a List<ProductDTO>.
     * Stores the list in TestContext for @Then assertions.
     */
    @When("I request all products")
    public void iRequestAllProducts() {
        ctx.setResultList(productService.getAllProducts());
    }

    /**
     * Calls ProductService.createProduct() with the DTO prepared in @Given.
     * Stores the returned ProductDTO in TestContext.
     */
    @When("I create the product")
    public void iCreateTheProduct() {
        ProductDTO dto = (ProductDTO) ctx.getResult();
        ctx.setResult(productService.createProduct(dto));
    }

    /**
     * Calls ProductService.getProduct(id).
     * Captures ResourceNotFoundException so error-path @Then steps can inspect it.
     */
    @When("I request the product with id {long}")
    public void iRequestTheProductWithId(long id) {
        try {
            ctx.setResult(productService.getProduct(id));
        } catch (ResourceNotFoundException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls ProductService.updateProduct(id, dto) with the provided new field values.
     * Captures ResourceNotFoundException for error-path assertions.
     */
    @When("I update product with id {long} to have name {string}, description {string}, price {double} and stock {int}")
    public void iUpdateProduct(long id, String name, String description, double price, int stock) {
        ProductDTO dto = new ProductDTO(null, name, description, BigDecimal.valueOf(price), stock, null);

        // stub save to return a Product with the same id
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            if (p.getId() == null) p.setId(id);
            return p;
        });

        try {
            ctx.setResult(productService.updateProduct(id, dto));
        } catch (ResourceNotFoundException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls ProductService.deleteProduct(id).
     * deleteProduct does not guard against ResourceNotFoundException (it delegates
     * directly to deleteById), so no exception capture is strictly needed here —
     * but we keep the try/catch for consistency with the feature file assertions.
     */
    @When("I delete the product with id {long}")
    public void iDeleteTheProductWithId(long id) {
        try {
            productService.deleteProduct(id);
        } catch (Exception e) {
            ctx.setThrownException(e);
        }
    }

    // ── @Then steps ───────────────────────────────────────────────────────────

    /**
     * Asserts the number of products in the list returned by getAllProducts().
     */
    @Then("the result should contain {int} product")
    public void theResultShouldContainProducts(int count) {
        assertThat(ctx.getResultList()).hasSize(count);
    }

    /**
     * Asserts that the first ProductDTO in the result list has the expected name.
     */
    @Then("the first product's name should be {string}")
    public void theFirstProductNameShouldBe(String name) {
        ProductDTO dto = (ProductDTO) ctx.getResultList().get(0);
        assertThat(dto.getName()).isEqualTo(name);
    }

    /**
     * Asserts that the product result list is empty.
     */
    @Then("the product result should be an empty list")
    public void theProductResultShouldBeAnEmptyList() {
        assertThat(ctx.getResultList()).isEmpty();
    }

    /**
     * Asserts the id of the ProductDTO returned by createProduct().
     */
    @Then("the created product should have id {long}")
    public void theCreatedProductShouldHaveId(long id) {
        ProductDTO dto = (ProductDTO) ctx.getResult();
        assertThat(dto.getId()).isEqualTo(id);
    }

    /**
     * Asserts the name of the created ProductDTO.
     */
    @And("the created product's name should be {string}")
    public void theCreatedProductNameShouldBe(String name) {
        ProductDTO dto = (ProductDTO) ctx.getResult();
        assertThat(dto.getName()).isEqualTo(name);
    }

    /**
     * Asserts the name of the ProductDTO returned by getProduct().
     */
    @Then("the returned product should have name {string}")
    public void theReturnedProductNameShouldBe(String name) {
        ProductDTO dto = (ProductDTO) ctx.getResult();
        assertThat(dto.getName()).isEqualTo(name);
    }

    /**
     * Asserts both the name and price of the ProductDTO returned by updateProduct().
     */
    @Then("the updated product should have name {string} and price {double}")
    public void theUpdatedProductShouldHave(String name, double price) {
        ProductDTO dto = (ProductDTO) ctx.getResult();
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(price));
    }

    /**
     * Verifies that productRepository.deleteById was invoked with the correct id,
     * confirming that the service actually delegated deletion to the repository.
     */
    @And("the product repository should have had deleteById called for id {long}")
    public void theProductRepositoryDeleteByIdCalledForId(long id) {
        verify(productRepository).deleteById(id);
    }
}
