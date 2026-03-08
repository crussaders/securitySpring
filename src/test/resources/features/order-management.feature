# Feature: Order Management
#
# Describes the expected behaviour of the Order management API.
# Orders link a User to a set of OrderItems and have a lifecycle status.
# Key business rules tested here:
#   - A newly created order always starts with status "CREATED".
#   - An order with items or a payment cannot be deleted (referential integrity guard).

Feature: Order Management

  Background:
    Given the order repository is empty

  # ── Retrieve all orders ──────────────────────────────────────────────────────

  Scenario: Retrieve all orders when orders exist
    Given the order repository contains an order with id 1 and status "CREATED"
    When I request all orders
    Then the order result should contain 1 order
    And the first order's status should be "CREATED"

  Scenario: Retrieve all orders returns empty list when repository is empty
    When I request all orders
    Then the order result should be an empty list

  # ── Create an order ──────────────────────────────────────────────────────────

  Scenario: Create a new order sets status to CREATED
    Given I have a new order with total amount 150.00
    When I create the order
    Then the created order should have id 1
    And the created order's status should be "CREATED"

  # ── Retrieve a single order ──────────────────────────────────────────────────

  Scenario: Retrieve an order by id when order exists
    Given the order repository contains an order with id 1 and status "CREATED"
    When I request the order with id 1
    Then the returned order should have status "CREATED"

  Scenario: Retrieve an order by id when order does not exist
    When I request the order with id 99
    Then a ResourceNotFoundException should be thrown for "Order" with id 99

  # ── Update order status ──────────────────────────────────────────────────────

  Scenario: Update an order's status
    Given the order repository contains an order with id 1 and status "CREATED"
    When I update the status of order with id 1 to "SHIPPED"
    Then the updated order's status should be "SHIPPED"

  Scenario: Update status of an order that does not exist raises an error
    When I update the status of order with id 99 to "SHIPPED"
    Then a ResourceNotFoundException should be thrown for "Order" with id 99

  # ── Delete an order ──────────────────────────────────────────────────────────

  Scenario: Delete an order that has no items and no payment
    Given the order repository contains an order with id 1 and status "CREATED"
    And the order with id 1 has no order items and no payment
    When I delete the order with id 1
    Then no exception should be thrown

  Scenario: Delete an order that has associated order items raises a conflict error
    Given the order repository contains an order with id 1 and status "CREATED"
    And the order with id 1 has associated order items
    When I delete the order with id 1
    Then a conflict ResponseStatusException should be thrown

  Scenario: Delete an order that has an associated payment raises a conflict error
    Given the order repository contains an order with id 1 and status "CREATED"
    And the order with id 1 has an associated payment
    When I delete the order with id 1
    Then a conflict ResponseStatusException should be thrown

  Scenario: Delete an order that does not exist raises an error
    When I delete the order with id 99
    Then a ResourceNotFoundException should be thrown for "Order" with id 99
