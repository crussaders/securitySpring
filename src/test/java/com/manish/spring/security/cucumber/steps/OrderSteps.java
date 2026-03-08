package com.manish.spring.security.cucumber.steps;

import com.manish.spring.security.Entity.Order;
import com.manish.spring.security.Entity.OrderItem;
import com.manish.spring.security.Entity.Payment;
import com.manish.spring.security.Repository.OrderRepository;
import com.manish.spring.security.cucumber.context.TestContext;
import com.manish.spring.security.exception.ResourceNotFoundException;
import com.manish.spring.security.service.OrderService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Step definitions for the "Order Management" feature.
 *
 * <p>Key business rules exercised:
 * <ul>
 *   <li>OrderService.createOrder always forces status to "CREATED" regardless of input.</li>
 *   <li>Deleting an order with associated OrderItems throws HTTP 409 CONFLICT.</li>
 *   <li>Deleting an order with an associated Payment throws HTTP 409 CONFLICT.</li>
 *   <li>Accessing or deleting a non-existent order throws ResourceNotFoundException.</li>
 * </ul>
 *
 * <p>Note: OrderService.deleteOrder is @Transactional and loads lazy collections
 * (getOrderItems, getPayment), so we set those fields directly on the Order entity.
 */
public class OrderSteps {

    // ── Mocks ────────────────────────────────────────────────────────────────

    /**
     * Mock repository backing the OrderService.
     * We program findAll(), findById(), save(), and delete() as needed per scenario.
     */
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    /**
     * The real OrderService under test.  Its single dependency (OrderRepository)
     * is injected via reflection in the constructor.
     */
    private final OrderService orderService;

    /** Shared per-scenario state injected by Cucumber. */
    private final TestContext ctx;

    // ── Constructor ──────────────────────────────────────────────────────────

    public OrderSteps(TestContext ctx) throws Exception {
        this.ctx = ctx;
        this.orderService = new OrderService();
        java.lang.reflect.Field field = OrderService.class.getDeclaredField("orderRepository");
        field.setAccessible(true);
        field.set(orderService, orderRepository);
    }

    // ── @Before ──────────────────────────────────────────────────────────────

    /** Resets all stubs before each scenario to ensure test isolation. */
    @Before
    public void resetMocks() {
        reset(orderRepository);
    }

    // ── @Given steps ─────────────────────────────────────────────────────────

    /**
     * Simulates an empty orders table by making findAll() return an empty list.
     * This is the Background step that runs before every scenario.
     */
    @Given("the order repository is empty")
    public void theOrderRepositoryIsEmpty() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
    }

    /**
     * Seeds the mock repository with a single Order.
     * By default the order has no OrderItems and no Payment so it can be deleted
     * freely unless a subsequent @Given step adds them.
     *
     * @param id     the order's identifier
     * @param status the initial order status
     */
    @Given("the order repository contains an order with id {long} and status {string}")
    public void theOrderRepositoryContainsOrder(long id, String status) {
        Order order = new Order(id, null, BigDecimal.valueOf(100), status, null,
                Collections.emptyList(), null);

        // findAll is used by getOrders()
        when(orderRepository.findAll()).thenReturn(List.of(order));

        // findById is used by getOrder(id), updateOrderStatus(id, …), deleteOrder(id)
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        // save is used by createOrder and updateOrderStatus
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /**
     * Prepares a new Order entity to be passed to createOrder().
     * The status field is intentionally left as null here because the service
     * overwrites it with "CREATED" – that is exactly the behaviour we are testing.
     */
    @Given("I have a new order with total amount {double}")
    public void iHaveANewOrderWithTotalAmount(double amount) {
        Order newOrder = new Order(null, null, BigDecimal.valueOf(amount), null, null,
                Collections.emptyList(), null);
        ctx.setResult(newOrder);

        // Stub save to assign id=1 and preserve the status set by the service
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) o.setId(1L);
            return o;
        });
    }

    /**
     * Configures the seeded order to have no OrderItems and no Payment,
     * making it eligible for deletion without a conflict error.
     */
    @And("the order with id {long} has no order items and no payment")
    public void theOrderHasNoItemsAndNoPayment(long id) {
        // The order was already seeded with empty items and null payment – no extra stubbing needed.
        // This step is explicitly written to make the scenario intent clear to readers.
    }

    /**
     * Configures the seeded order to have at least one OrderItem, simulating
     * the FK-integrity condition that should block deletion.
     */
    @And("the order with id {long} has associated order items")
    public void theOrderHasOrderItems(long id) {
        // Re-stub findById with an Order that has a non-empty orderItems list
        Order order = new Order(id, null, BigDecimal.valueOf(100), "CREATED", null,
                List.of(new OrderItem()), null);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    }

    /**
     * Configures the seeded order to have an associated Payment, simulating
     * the other FK-integrity condition that should block deletion.
     */
    @And("the order with id {long} has an associated payment")
    public void theOrderHasPayment(long id) {
        // Re-stub findById with an Order that has a non-null payment
        Order order = new Order(id, null, BigDecimal.valueOf(100), "CREATED", null,
                Collections.emptyList(), new Payment());
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
    }

    // ── @When steps ──────────────────────────────────────────────────────────

    /**
     * Calls OrderService.getOrders() and stores the list in TestContext.
     */
    @When("I request all orders")
    public void iRequestAllOrders() {
        ctx.setResultList(orderService.getOrders());
    }

    /**
     * Calls OrderService.createOrder() with the Order prepared in @Given.
     * The service will overwrite the status field to "CREATED".
     */
    @When("I create the order")
    public void iCreateTheOrder() {
        Order input = (Order) ctx.getResult();
        ctx.setResult(orderService.createOrder(input));
    }

    /**
     * Calls OrderService.getOrder(id), capturing ResourceNotFoundException.
     */
    @When("I request the order with id {long}")
    public void iRequestTheOrderWithId(long id) {
        try {
            ctx.setResult(orderService.getOrder(id));
        } catch (ResourceNotFoundException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls OrderService.updateOrderStatus(id, status), capturing exceptions.
     */
    @When("I update the status of order with id {long} to {string}")
    public void iUpdateOrderStatus(long id, String status) {
        try {
            ctx.setResult(orderService.updateOrderStatus(id, status));
        } catch (ResourceNotFoundException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls OrderService.deleteOrder(id), capturing both ResourceNotFoundException
     * and ResponseStatusException (CONFLICT) thrown by the FK-integrity guards.
     */
    @When("I delete the order with id {long}")
    public void iDeleteTheOrderWithId(long id) {
        try {
            orderService.deleteOrder(id);
        } catch (ResourceNotFoundException | ResponseStatusException e) {
            ctx.setThrownException((Exception) e);
        }
    }

    // ── @Then steps ───────────────────────────────────────────────────────────

    /**
     * Asserts the number of orders in the list returned by getOrders().
     */
    @Then("the order result should contain {int} order")
    public void theOrderResultShouldContain(int count) {
        assertThat(ctx.getResultList()).hasSize(count);
    }

    /**
     * Asserts the status of the first Order in the result list.
     */
    @Then("the first order's status should be {string}")
    public void theFirstOrderStatusShouldBe(String status) {
        Order order = (Order) ctx.getResultList().get(0);
        assertThat(order.getStatus()).isEqualTo(status);
    }

    /**
     * Asserts that the order result list is empty.
     */
    @Then("the order result should be an empty list")
    public void theOrderResultShouldBeAnEmptyList() {
        assertThat(ctx.getResultList()).isEmpty();
    }

    /**
     * Asserts the id of the Order returned by createOrder().
     */
    @Then("the created order should have id {long}")
    public void theCreatedOrderShouldHaveId(long id) {
        Order order = (Order) ctx.getResult();
        assertThat(order.getId()).isEqualTo(id);
    }

    /**
     * Asserts the status of the Order returned by createOrder().
     * The key assertion: regardless of what status was supplied, it must be "CREATED".
     */
    @And("the created order's status should be {string}")
    public void theCreatedOrderStatusShouldBe(String status) {
        Order order = (Order) ctx.getResult();
        assertThat(order.getStatus()).isEqualTo(status);
    }

    /**
     * Asserts the status of the Order returned by getOrder().
     */
    @Then("the returned order should have status {string}")
    public void theReturnedOrderStatusShouldBe(String status) {
        Order order = (Order) ctx.getResult();
        assertThat(order.getStatus()).isEqualTo(status);
    }

    /**
     * Asserts the status of the Order returned by updateOrderStatus().
     */
    @Then("the updated order's status should be {string}")
    public void theUpdatedOrderStatusShouldBe(String status) {
        Order order = (Order) ctx.getResult();
        assertThat(order.getStatus()).isEqualTo(status);
    }
}
