# Feature: Payment Management
#
# Describes the expected behaviour of the Payment management API.
# Key business rule: when a payment is created, the service automatically
# sets paymentStatus to "SUCCESS" regardless of any value supplied by the caller.

Feature: Payment Management

  Background:
    Given the payment repository is empty

  # ── Create a payment ─────────────────────────────────────────────────────────

  Scenario: Create a payment sets status to SUCCESS automatically
    Given I have a new payment with method "CREDIT_CARD" and amount 250.00
    When I create the payment
    Then the created payment should have id 1
    And the created payment's status should be "SUCCESS"

  # ── Retrieve all payments ────────────────────────────────────────────────────

  Scenario: Retrieve all payments when payments exist
    Given the payment repository contains a payment with id 1, method "CREDIT_CARD" and status "SUCCESS"
    When I request all payments
    Then the payment result should contain 1 payment
    And the first payment's status should be "SUCCESS"

  Scenario: Retrieve all payments returns empty list when repository is empty
    When I request all payments
    Then the payment result should be an empty list

  # ── Retrieve a single payment ────────────────────────────────────────────────

  Scenario: Retrieve a payment by id when payment exists
    Given the payment repository contains a payment with id 1, method "CREDIT_CARD" and status "SUCCESS"
    When I request the payment with id 1
    Then the returned payment should have method "CREDIT_CARD"

  Scenario: Retrieve a payment by id when payment does not exist
    When I request the payment with id 99
    Then a RuntimeException should be thrown with message "Payment not found"

  # ── Update a payment ─────────────────────────────────────────────────────────

  Scenario: Update an existing payment's details
    Given the payment repository contains a payment with id 1, method "CREDIT_CARD" and status "SUCCESS"
    When I update payment with id 1 to have method "BANK_TRANSFER" and amount 300.00
    Then the updated payment should have method "BANK_TRANSFER"

  Scenario: Update a payment that does not exist raises an error
    When I update payment with id 99 to have method "BANK_TRANSFER" and amount 300.00
    Then a RuntimeException should be thrown with message "Payment not found"

  # ── Delete a payment ─────────────────────────────────────────────────────────

  Scenario: Delete an existing payment successfully
    Given the payment repository contains a payment with id 1, method "CREDIT_CARD" and status "SUCCESS"
    When I delete the payment with id 1
    Then no exception should be thrown
    And the payment repository should have had deleteById called for id 1
