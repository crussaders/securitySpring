package com.manish.spring.security.cucumber.steps;

import com.manish.spring.security.Entity.Order;
import com.manish.spring.security.Entity.Role;
import com.manish.spring.security.Entity.User;
import com.manish.spring.security.Repository.OrderRepository;
import com.manish.spring.security.Repository.UserRepository;
import com.manish.spring.security.cucumber.context.TestContext;
import com.manish.spring.security.exception.ResourceNotFoundException;
import com.manish.spring.security.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Step definitions for the "User Management" feature.
 *
 * <p>Why no Spring context?
 * These tests mirror the existing unit-test style (MockitoExtension), keeping the
 * test suite fast and independent of a running database.  The service under test is
 * created directly using Mockito mocks for its dependencies.
 *
 * <p>Constructor injection:
 * Cucumber creates one instance of this class per scenario and passes the shared
 * TestContext in, so state written in @Given/@When steps can be read in @Then steps.
 */
public class UserSteps {

    // ── Dependencies created with Mockito ────────────────────────────────────

    /**
     * Mock of UserRepository – replaces the real JPA repository so no database
     * is needed.  We program its behaviour with Mockito's when(...).thenReturn(...)
     * inside each @Given step.
     */
    private final UserRepository userRepository = mock(UserRepository.class);

    /**
     * Mock of OrderRepository – UserService needs this to check whether a user
     * has associated orders before deleting them.
     */
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    /**
     * Mock of PasswordEncoder – UserService encodes passwords; we mock the encoder
     * so tests do not depend on BCrypt and return a predictable encoded value.
     */
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    /**
     * The real service under test, constructed manually so we can inject the mocks
     * above via reflection (Mockito's @InjectMocks equivalent at runtime).
     */
    private final UserService userService;

    /**
     * Shared state holder injected by Cucumber.  @Given steps write to it,
     * @Then steps read from it.
     */
    private final TestContext ctx;

    // ── Constructor injection ────────────────────────────────────────────────

    /**
     * Cucumber calls this constructor for every scenario, passing in the single
     * TestContext instance for that scenario.  We build the UserService here so
     * the mocks are fresh and unprogrammed at the start of every scenario.
     */
    public UserSteps(TestContext ctx) throws Exception {
        this.ctx = ctx;

        // Build a real UserService and inject the mocks using reflection.
        // We cannot use @InjectMocks outside JUnit 5 lifecycle, so we
        // manually set private fields via org.mockito.internal.util.reflection.
        this.userService = new UserService();
        injectField("userRepository", userRepository);
        injectField("orderRepository", orderRepository);
        injectField("passwordEncoder", passwordEncoder);

        // Make the password encoder mock return a predictable encoded value
        // for any plain-text password supplied during tests.
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    }

    /**
     * Injects a mock into the private field of the UserService.
     * This is the standard approach when constructing services manually outside
     * the JUnit Mockito extension lifecycle.
     */
    private void injectField(String fieldName, Object mock) throws Exception {
        java.lang.reflect.Field field = UserService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(userService, mock);
    }

    // ── @Before hook ─────────────────────────────────────────────────────────

    /**
     * @Before runs before each scenario step execution in this step definition class.
     * We reset the mocks here so that stubbing set up in one scenario does not
     * accidentally leak into the next scenario.
     */
    @Before
    public void resetMocks() {
        reset(userRepository, orderRepository, passwordEncoder);
        // Re-apply the global password encoder stub after the reset.
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    }

    // ── @Given steps ─────────────────────────────────────────────────────────

    /**
     * Establishes the "empty repository" precondition by making findAll() return
     * an empty list.  This is the Background step shared by every scenario.
     */
    @Given("the user repository is empty")
    public void theUserRepositoryIsEmpty() {
        // Stub findAll to return no users – simulates a clean database.
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
    }

    /**
     * Seeds the mock repository with a single User so that subsequent @When steps
     * that look up users will find this one.
     */
    @Given("the user repository contains a user with id {long}, first name {string}, last name {string} and email {string}")
    public void theUserRepositoryContainsUser(long id, String firstName, String lastName, String email) {
        User user = new User(id, firstName, lastName, email, "hashed_pass", null);

        // Stub findAll – used by getUsers()
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Stub findById – used by getUser(id), updateUser(id, …) and deleteUser(id)
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Stub save – used by updateUser(id, …); return a copy with the same id
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) u.setId(id);
            return u;
        });
    }

    /**
     * Prepares the new-user input that will be passed to createUser().
     * We store it in the TestContext so the @When step can retrieve it.
     */
    @Given("I have a new user with first name {string}, last name {string}, email {string} and password {string}")
    public void iHaveANewUser(String firstName, String lastName, String email, String password) {
        // Build a User with no id (id is null because it hasn't been persisted yet)
        User newUser = new User(null, firstName, lastName, email, password, null);
        ctx.setResult(newUser);

        // Stub save to simulate the database assigning id=1 upon insert
        User saved = new User(1L, firstName, lastName, email, "encoded_" + password, null);
        when(userRepository.save(any(User.class))).thenReturn(saved);
    }

    /**
     * Configures the order repository mock to return an empty list for the given user,
     * meaning the user can be safely deleted without violating referential integrity.
     */
    @Given("the user with id {long} has no associated orders")
    public void theUserHasNoOrders(long userId) {
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
    }

    /**
     * Configures the order repository mock to return a non-empty list, simulating
     * the presence of FK-constraint violations that should block deletion.
     */
    @Given("the user with id {long} has associated orders")
    public void theUserHasOrders(long userId) {
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(new Order()));
    }

    // ── @When steps ──────────────────────────────────────────────────────────

    /**
     * Invokes UserService.getUsers() and stores the returned list in TestContext.
     * Any exception would propagate and fail the scenario.
     */
    @When("I request all users")
    public void iRequestAllUsers() {
        ctx.setResultList(userService.getUsers());
    }

    /**
     * Invokes UserService.createUser() with the User prepared in the @Given step.
     * The result (User with generated id) is stored for @Then assertions.
     */
    @When("I create the user")
    public void iCreateTheUser() {
        User input = (User) ctx.getResult();
        ctx.setResult(userService.createUser(input));
    }

    /**
     * Invokes UserService.getUser(id).
     * Wraps the call in a try/catch so that a ResourceNotFoundException can be
     * inspected by the "should be thrown" @Then step rather than failing immediately.
     */
    @When("I request the user with id {long}")
    public void iRequestTheUserWithId(long id) {
        try {
            ctx.setResult(userService.getUser(id));
        } catch (ResourceNotFoundException e) {
            // Store the exception so @Then steps can assert on it
            ctx.setThrownException(e);
        }
    }

    /**
     * Invokes UserService.updateUser(id, updatedUser) with the given field values.
     * Wraps the call to capture ResourceNotFoundException for error-path assertions.
     */
    @When("I update user with id {long} to have first name {string}, last name {string} and email {string}")
    public void iUpdateUser(long id, String firstName, String lastName, String email) {
        User updatedUser = new User(null, firstName, lastName, email, null, null);

        // Stub save to return the updated entity with the same id
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) u.setId(id);
            return u;
        });

        try {
            ctx.setResult(userService.updateUser(id, updatedUser));
        } catch (ResourceNotFoundException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Invokes UserService.deleteUser(id).
     * Wraps the call so both ResourceNotFoundException and ResponseStatusException
     * (conflict) are captured for @Then assertions.
     */
    @When("I delete the user with id {long}")
    public void iDeleteTheUserWithId(long id) {
        try {
            userService.deleteUser(id);
        } catch (ResourceNotFoundException | ResponseStatusException e) {
            ctx.setThrownException((Exception) e);
        }
    }

    // ── @Then steps ───────────────────────────────────────────────────────────

    /**
     * Verifies that the result list from "get all" has the expected number of users.
     */
    @Then("the result should contain {int} user")
    public void theResultShouldContainUsers(int count) {
        assertThat(ctx.getResultList()).hasSize(count);
    }

    /**
     * Verifies that the first element in the result list has the expected first name.
     */
    @Then("the first user's first name should be {string}")
    public void theFirstUserFirstNameShouldBe(String firstName) {
        User user = (User) ctx.getResultList().get(0);
        assertThat(user.getFirstName()).isEqualTo(firstName);
    }

    /**
     * Verifies that the result list is empty (no users found).
     */
    @Then("the result should be an empty list")
    public void theResultShouldBeAnEmptyList() {
        assertThat(ctx.getResultList()).isEmpty();
    }

    /**
     * Verifies that the created user has the expected id.
     */
    @Then("the created user should have id {long}")
    public void theCreatedUserShouldHaveId(long id) {
        User user = (User) ctx.getResult();
        assertThat(user.getId()).isEqualTo(id);
    }

    /**
     * Verifies that the created user has the expected email.
     */
    @And("the created user's email should be {string}")
    public void theCreatedUserEmailShouldBe(String email) {
        User user = (User) ctx.getResult();
        assertThat(user.getEmail()).isEqualTo(email);
    }

    /**
     * Verifies that the user returned by getUser() has the expected first name.
     */
    @Then("the returned user should have first name {string}")
    public void theReturnedUserFirstNameShouldBe(String firstName) {
        User user = (User) ctx.getResult();
        assertThat(user.getFirstName()).isEqualTo(firstName);
    }

    /**
     * Verifies that the updated user has both the expected first name and email.
     */
    @Then("the updated user should have first name {string} and email {string}")
    public void theUpdatedUserShouldHave(String firstName, String email) {
        User user = (User) ctx.getResult();
        assertThat(user.getFirstName()).isEqualTo(firstName);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    /**
     * Verifies that no exception was thrown during the @When step.
     * Used for "happy path" delete scenarios.
     */
    @Then("no exception should be thrown")
    public void noExceptionShouldBeThrown() {
        assertThat(ctx.getThrownException()).isNull();
    }

    /**
     * Verifies that the user repository's deleteById method was called with the
     * expected id, confirming the service actually delegated to the repository.
     */
    @And("the user repository should have had deleteById called for id {long}")
    public void theUserRepositoryDeleteByIdCalledForId(long id) {
        verify(userRepository).deleteById(id);
    }

    /**
     * Verifies that the thrown exception is a ResourceNotFoundException whose
     * message contains both the resource name and the id.
     * Matches scenarios like: "a ResourceNotFoundException should be thrown for "User" with id 99".
     */
    @Then("a ResourceNotFoundException should be thrown for {string} with id {long}")
    public void aResourceNotFoundExceptionShouldBeThrown(String resourceName, long id) {
        assertThat(ctx.getThrownException())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(resourceName)
                .hasMessageContaining(String.valueOf(id));
    }

    /**
     * Verifies that the thrown exception is a ResponseStatusException with HTTP 409
     * CONFLICT status, indicating a delete was blocked by a FK-integrity rule.
     */
    @Then("a conflict ResponseStatusException should be thrown")
    public void aConflictResponseStatusExceptionShouldBeThrown() {
        assertThat(ctx.getThrownException())
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT));
    }
}
