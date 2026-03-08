# Feature: Role Management
#
# Describes the expected behaviour of the Role management API.
# Roles are security roles that can be assigned to users.
# Key business rule: a role that is still assigned to at least one user
# cannot be deleted (foreign-key integrity guard).

Feature: Role Management

  Background:
    Given the role repository is empty

  # ── Retrieve all roles ───────────────────────────────────────────────────────

  Scenario: Retrieve all roles when roles exist
    Given the role repository contains a role with id 1 and name "ADMIN"
    When I request all roles
    Then the role result should contain 1 role
    And the first role's name should be "ADMIN"

  Scenario: Retrieve all roles returns empty list when repository is empty
    When I request all roles
    Then the role result should be an empty list

  # ── Create a role ────────────────────────────────────────────────────────────

  Scenario: Create a new role successfully
    Given I have a new role with name "MANAGER"
    When I create the role
    Then the created role should have id 1
    And the created role's name should be "MANAGER"

  # ── Retrieve a single role ───────────────────────────────────────────────────

  Scenario: Retrieve a role by id when role exists
    Given the role repository contains a role with id 1 and name "ADMIN"
    When I request the role with id 1
    Then the returned role should have name "ADMIN"

  Scenario: Retrieve a role by id when role does not exist
    When I request the role with id 99
    Then a RuntimeException should be thrown with message "Role not found"

  # ── Update a role ────────────────────────────────────────────────────────────

  Scenario: Update an existing role's name
    Given the role repository contains a role with id 1 and name "ADMIN"
    When I update the role with id 1 to have name "SUPER_ADMIN"
    Then the updated role should have name "SUPER_ADMIN"

  Scenario: Update a role that does not exist raises an error
    When I update the role with id 99 to have name "GHOST"
    Then a RuntimeException should be thrown with message "Role not found"

  # ── Delete a role ────────────────────────────────────────────────────────────

  Scenario: Delete a role that has no assigned users
    Given the role repository contains a role with id 1 and name "ADMIN"
    And the role with id 1 has no assigned users
    When I delete the role with id 1
    Then no exception should be thrown

  Scenario: Delete a role that has assigned users raises a conflict error
    Given the role repository contains a role with id 1 and name "ADMIN"
    And the role with id 1 has assigned users
    When I delete the role with id 1
    Then a conflict ResponseStatusException should be thrown

  Scenario: Delete a role that does not exist raises an error
    When I delete the role with id 99
    Then a RuntimeException should be thrown with message "Role not found"
