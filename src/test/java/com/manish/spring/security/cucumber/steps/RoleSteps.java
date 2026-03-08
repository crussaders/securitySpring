package com.manish.spring.security.cucumber.steps;

import com.manish.spring.security.Entity.Role;
import com.manish.spring.security.Entity.User;
import com.manish.spring.security.Repository.RoleRepository;
import com.manish.spring.security.cucumber.context.TestContext;
import com.manish.spring.security.service.RoleService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Step definitions for the "Role Management" feature.
 *
 * <p>Key business rules exercised:
 * <ul>
 *   <li>A role that has no assigned users can be deleted freely.</li>
 *   <li>A role that is still assigned to at least one user cannot be deleted;
 *       the service throws a ResponseStatusException with HTTP 409 CONFLICT.</li>
 *   <li>Accessing or deleting a non-existent role throws a plain RuntimeException
 *       with the message "Role not found" (RoleService does not use
 *       ResourceNotFoundException).</li>
 * </ul>
 *
 * <p>Note: RoleService.deleteRole is @Transactional and accesses the lazy
 * role.getUsers() collection.  We set the users list directly on the Role entity
 * so Hibernate lazy-loading is not involved in these unit tests.
 */
public class RoleSteps {

    // ── Mocks ────────────────────────────────────────────────────────────────

    /**
     * Mock repository that replaces the real JPA RoleRepository.
     * All interactions with the DB go through this mock.
     */
    private final RoleRepository roleRepository = mock(RoleRepository.class);

    /**
     * The real RoleService under test.  Its single dependency (RoleRepository)
     * is injected via reflection in the constructor.
     */
    private final RoleService roleService;

    /** Shared per-scenario state injected by Cucumber. */
    private final TestContext ctx;

    // ── Constructor ──────────────────────────────────────────────────────────

    public RoleSteps(TestContext ctx) throws Exception {
        this.ctx = ctx;
        this.roleService = new RoleService();
        java.lang.reflect.Field field = RoleService.class.getDeclaredField("roleRepository");
        field.setAccessible(true);
        field.set(roleService, roleRepository);
    }

    // ── @Before ──────────────────────────────────────────────────────────────

    /** Resets all stubs before each scenario for test isolation. */
    @Before
    public void resetMocks() {
        reset(roleRepository);
    }

    // ── @Given steps ─────────────────────────────────────────────────────────

    /**
     * Simulates an empty roles table.
     * Used as the Background step that runs before every scenario in this feature.
     */
    @Given("the role repository is empty")
    public void theRoleRepositoryIsEmpty() {
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
    }

    /**
     * Seeds the mock repository with a single Role, with no users assigned by default.
     *
     * @param id       the role's identifier
     * @param roleName the role name (e.g. "ADMIN")
     */
    @Given("the role repository contains a role with id {long} and name {string}")
    public void theRoleRepositoryContainsRole(long id, String roleName) {
        // By default we create the role with an empty users list so it can be deleted
        Role role = new Role(id, roleName, Collections.emptyList());

        // Stub findAll used by getAllRoles()
        when(roleRepository.findAll()).thenReturn(List.of(role));

        // Stub findById used by getRole(id), updateRole(id, …) and deleteRole(id)
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        // Stub save used by updateRole
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /**
     * Prepares a new Role to be created.  Stores it in TestContext so the
     * @When "I create the role" step can retrieve it.
     */
    @Given("I have a new role with name {string}")
    public void iHaveANewRoleWithName(String roleName) {
        Role newRole = new Role(null, roleName, Collections.emptyList());
        ctx.setResult(newRole);

        // Stub save to assign id=1, simulating DB auto-increment on insert
        Role saved = new Role(1L, roleName, Collections.emptyList());
        when(roleRepository.save(any(Role.class))).thenReturn(saved);
    }

    /**
     * Configures the seeded role to have no assigned users, making deletion safe.
     * The empty users list is already set in the seed step, so this step is
     * purely documentary – it makes the scenario intent explicit to readers.
     */
    @And("the role with id {long} has no assigned users")
    public void theRoleHasNoAssignedUsers(long id) {
        // No additional stubbing needed; the seed step already uses an empty users list.
    }

    /**
     * Re-stubs findById to return a Role that has at least one assigned User.
     * This triggers the FK-integrity guard inside RoleService.deleteRole().
     */
    @And("the role with id {long} has assigned users")
    public void theRoleHasAssignedUsers(long id) {
        // Create a User assigned to this role; we only need a non-empty list
        User user = new User(10L, "SomeUser", "LastName", "user@example.com", "pass", null);
        Role roleWithUsers = new Role(id, "ADMIN", List.of(user));
        when(roleRepository.findById(id)).thenReturn(Optional.of(roleWithUsers));
    }

    // ── @When steps ──────────────────────────────────────────────────────────

    /**
     * Calls RoleService.getAllRoles() and stores the list in TestContext.
     */
    @When("I request all roles")
    public void iRequestAllRoles() {
        ctx.setResultList(roleService.getAllRoles());
    }

    /**
     * Calls RoleService.createRole() with the Role prepared in @Given.
     */
    @When("I create the role")
    public void iCreateTheRole() {
        Role input = (Role) ctx.getResult();
        ctx.setResult(roleService.createRole(input));
    }

    /**
     * Calls RoleService.getRole(id), capturing RuntimeException for the
     * "not found" error scenario.
     */
    @When("I request the role with id {long}")
    public void iRequestTheRoleWithId(long id) {
        try {
            ctx.setResult(roleService.getRole(id));
        } catch (RuntimeException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls RoleService.updateRole(id, role), capturing RuntimeException.
     */
    @When("I update the role with id {long} to have name {string}")
    public void iUpdateTheRoleWithId(long id, String roleName) {
        Role updatedRole = new Role(null, roleName, Collections.emptyList());
        try {
            ctx.setResult(roleService.updateRole(id, updatedRole));
        } catch (RuntimeException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls RoleService.deleteRole(id), capturing both RuntimeException
     * (not found) and ResponseStatusException (CONFLICT / users still assigned).
     */
    @When("I delete the role with id {long}")
    public void iDeleteTheRoleWithId(long id) {
        try {
            roleService.deleteRole(id);
        } catch (RuntimeException e) {
            // Captures both plain RuntimeException (role not found) and
            // ResponseStatusException (role has users), since the latter extends RuntimeException
            ctx.setThrownException(e);
        }
    }

    // ── @Then steps ───────────────────────────────────────────────────────────

    /**
     * Asserts the number of roles in the list returned by getAllRoles().
     */
    @Then("the role result should contain {int} role")
    public void theRoleResultShouldContain(int count) {
        assertThat(ctx.getResultList()).hasSize(count);
    }

    /**
     * Asserts the roleName of the first Role in the result list.
     */
    @Then("the first role's name should be {string}")
    public void theFirstRoleNameShouldBe(String roleName) {
        Role role = (Role) ctx.getResultList().get(0);
        assertThat(role.getRoleName()).isEqualTo(roleName);
    }

    /**
     * Asserts that the role result list is empty.
     */
    @Then("the role result should be an empty list")
    public void theRoleResultShouldBeAnEmptyList() {
        assertThat(ctx.getResultList()).isEmpty();
    }

    /**
     * Asserts the id of the Role returned by createRole().
     */
    @Then("the created role should have id {long}")
    public void theCreatedRoleShouldHaveId(long id) {
        Role role = (Role) ctx.getResult();
        assertThat(role.getId()).isEqualTo(id);
    }

    /**
     * Asserts the roleName of the Role returned by createRole().
     */
    @And("the created role's name should be {string}")
    public void theCreatedRoleNameShouldBe(String roleName) {
        Role role = (Role) ctx.getResult();
        assertThat(role.getRoleName()).isEqualTo(roleName);
    }

    /**
     * Asserts the roleName of the Role returned by getRole().
     */
    @Then("the returned role should have name {string}")
    public void theReturnedRoleNameShouldBe(String roleName) {
        Role role = (Role) ctx.getResult();
        assertThat(role.getRoleName()).isEqualTo(roleName);
    }

    /**
     * Asserts the roleName of the Role returned by updateRole().
     */
    @Then("the updated role should have name {string}")
    public void theUpdatedRoleNameShouldBe(String roleName) {
        Role role = (Role) ctx.getResult();
        assertThat(role.getRoleName()).isEqualTo(roleName);
    }
}
