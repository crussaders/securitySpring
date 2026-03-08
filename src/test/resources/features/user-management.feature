# Feature: User Management
#
# This feature file describes the expected behaviour of the User management API.
# Each scenario represents one business rule or use-case that the service must satisfy.
# Written in Gherkin (Given / When / Then) so that non-technical stakeholders can
# read and validate the requirements alongside developers.

Feature: User Management

  # Background runs before every scenario in this feature.
  # Here we establish that the system has no users by default, so each
  # scenario starts from a clean, predictable state.
  Background:
    Given the user repository is empty

  # ── Retrieve all users ──────────────────────────────────────────────────────

  Scenario: Retrieve all users when the repository has users
    # Arrange: a list of existing users must be present
    Given the user repository contains a user with id 1, first name "Alice", last name "Smith" and email "alice@example.com"
    # Act: request the full list
    When I request all users
    # Assert: the returned list contains the expected user
    Then the result should contain 1 user
    And the first user's first name should be "Alice"

  Scenario: Retrieve all users returns empty list when repository is empty
    # No arrange needed – Background already emptied the repository
    When I request all users
    Then the result should be an empty list

  # ── Create a user ───────────────────────────────────────────────────────────

  Scenario: Create a new user successfully
    # Arrange: specify the data for the user to be created
    Given I have a new user with first name "Bob", last name "Jones", email "bob@example.com" and password "secret"
    # Act: call the create operation
    When I create the user
    # Assert: the returned user carries a generated id and the provided data
    Then the created user should have id 1
    And the created user's email should be "bob@example.com"

  # ── Retrieve a single user ──────────────────────────────────────────────────

  Scenario: Retrieve a user by id when the user exists
    Given the user repository contains a user with id 1, first name "Alice", last name "Smith" and email "alice@example.com"
    When I request the user with id 1
    Then the returned user should have first name "Alice"

  Scenario: Retrieve a user by id when the user does not exist
    # No user is seeded – any lookup should raise ResourceNotFoundException
    When I request the user with id 99
    Then a ResourceNotFoundException should be thrown for "User" with id 99

  # ── Update a user ───────────────────────────────────────────────────────────

  Scenario: Update an existing user's details
    Given the user repository contains a user with id 1, first name "Alice", last name "Smith" and email "alice@example.com"
    When I update user with id 1 to have first name "Alicia", last name "Brown" and email "alicia@example.com"
    Then the updated user should have first name "Alicia" and email "alicia@example.com"

  Scenario: Update a user that does not exist raises an error
    When I update user with id 99 to have first name "Ghost", last name "User" and email "ghost@example.com"
    Then a ResourceNotFoundException should be thrown for "User" with id 99

  # ── Delete a user ───────────────────────────────────────────────────────────

  Scenario: Delete a user that has no associated orders
    Given the user repository contains a user with id 1, first name "Alice", last name "Smith" and email "alice@example.com"
    And the user with id 1 has no associated orders
    When I delete the user with id 1
    Then no exception should be thrown
    And the user repository should have had deleteById called for id 1

  Scenario: Delete a user that has associated orders raises a conflict error
    Given the user repository contains a user with id 1, first name "Alice", last name "Smith" and email "alice@example.com"
    And the user with id 1 has associated orders
    When I delete the user with id 1
    Then a conflict ResponseStatusException should be thrown

  Scenario: Delete a user that does not exist raises an error
    When I delete the user with id 99
    Then a ResourceNotFoundException should be thrown for "User" with id 99
