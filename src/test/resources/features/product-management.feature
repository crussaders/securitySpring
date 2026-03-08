# Feature: Product Management
#
# Describes the expected behaviour of the Product management API.
# Products are the catalogue items that can be added to orders.
# ProductService uses a DTO (ProductDTO) for all input/output to avoid
# exposing the JPA entity directly.

Feature: Product Management

  Background:
    Given the product repository is empty

  # ── Retrieve all products ────────────────────────────────────────────────────

  Scenario: Retrieve all products when catalogue has products
    Given the product repository contains a product with id 1, name "Widget", price 9.99 and stock 100
    When I request all products
    Then the result should contain 1 product
    And the first product's name should be "Widget"

  Scenario: Retrieve all products returns empty list when catalogue is empty
    When I request all products
    Then the product result should be an empty list

  # ── Create a product ─────────────────────────────────────────────────────────

  Scenario: Create a new product successfully
    Given I have a new product with name "Gadget", description "A cool gadget", price 29.99 and stock 50
    When I create the product
    Then the created product should have id 1
    And the created product's name should be "Gadget"

  # ── Retrieve a single product ────────────────────────────────────────────────

  Scenario: Retrieve a product by id when product exists
    Given the product repository contains a product with id 1, name "Widget", price 9.99 and stock 100
    When I request the product with id 1
    Then the returned product should have name "Widget"

  Scenario: Retrieve a product by id when product does not exist
    When I request the product with id 99
    Then a ResourceNotFoundException should be thrown for "Product" with id 99

  # ── Update a product ─────────────────────────────────────────────────────────

  Scenario: Update an existing product's details
    Given the product repository contains a product with id 1, name "Widget", price 9.99 and stock 100
    When I update product with id 1 to have name "Widget Pro", description "Upgraded", price 19.99 and stock 80
    Then the updated product should have name "Widget Pro" and price 19.99

  Scenario: Update a product that does not exist raises an error
    When I update product with id 99 to have name "Ghost", description "None", price 0.00 and stock 0
    Then a ResourceNotFoundException should be thrown for "Product" with id 99

  # ── Delete a product ─────────────────────────────────────────────────────────

  Scenario: Delete an existing product successfully
    Given the product repository contains a product with id 1, name "Widget", price 9.99 and stock 100
    When I delete the product with id 1
    Then no exception should be thrown
    And the product repository should have had deleteById called for id 1
