package com.manish.spring.security.cucumber.steps;

import com.manish.spring.security.Entity.Payment;
import com.manish.spring.security.Repository.PaymentRepository;
import com.manish.spring.security.cucumber.context.TestContext;
import com.manish.spring.security.service.PaymentService;
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
 * Step definitions for the "Payment Management" feature.
 *
 * <p>Key business rules exercised:
 * <ul>
 *   <li>PaymentService.createPayment always forces paymentStatus to "SUCCESS"
 *       regardless of any value supplied by the caller.</li>
 *   <li>PaymentService.getPayment / deletePayment throw RuntimeException("Payment not found")
 *       when the payment does not exist (note: not ResourceNotFoundException – the
 *       service uses a plain RuntimeException for payments).</li>
 * </ul>
 */
public class PaymentSteps {

    // ── Mocks ────────────────────────────────────────────────────────────────

    /** Mock repository – no real database connection required. */
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);

    /**
     * The real PaymentService under test.  Its single dependency (PaymentRepository)
     * is injected via reflection in the constructor.
     */
    private final PaymentService paymentService;

    /** Shared per-scenario state injected by Cucumber. */
    private final TestContext ctx;

    // ── Constructor ──────────────────────────────────────────────────────────

    public PaymentSteps(TestContext ctx) throws Exception {
        this.ctx = ctx;
        this.paymentService = new PaymentService();
        java.lang.reflect.Field field = PaymentService.class.getDeclaredField("paymentRepository");
        field.setAccessible(true);
        field.set(paymentService, paymentRepository);
    }

    // ── @Before ──────────────────────────────────────────────────────────────

    /** Resets all stubs before each scenario for test isolation. */
    @Before
    public void resetMocks() {
        reset(paymentRepository);
    }

    // ── @Given steps ─────────────────────────────────────────────────────────

    /**
     * Simulates an empty payments table.
     * Used as the Background step that runs before every scenario in this feature.
     */
    @Given("the payment repository is empty")
    public void thePaymentRepositoryIsEmpty() {
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());
    }

    /**
     * Seeds the mock repository with a single Payment.
     *
     * @param id     the payment's identifier
     * @param method the payment method (e.g. "CREDIT_CARD")
     * @param status the payment status (e.g. "SUCCESS")
     */
    @Given("the payment repository contains a payment with id {long}, method {string} and status {string}")
    public void thePaymentRepositoryContainsPayment(long id, String method, String status) {
        Payment payment = new Payment(id, null, method, status, BigDecimal.valueOf(100), null);

        // Stub findAll used by getPayments()
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        // Stub findById used by getPayment(id), updatePayment(id, …), deletePayment(id)
        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        // Stub save used by updatePayment
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /**
     * Prepares the Payment input that will be passed to createPayment().
     * Note: paymentStatus is intentionally set to a non-"SUCCESS" value here to
     * verify that the service overwrites it.
     */
    @Given("I have a new payment with method {string} and amount {double}")
    public void iHaveANewPayment(String method, double amount) {
        // Setting paymentStatus to "PENDING" – the service must overwrite this to "SUCCESS"
        Payment newPayment = new Payment(null, null, method, "PENDING", BigDecimal.valueOf(amount), null);
        ctx.setResult(newPayment);

        // Stub save to simulate DB id-assignment; the service will have already set
        // paymentStatus to "SUCCESS" on the passed-in object before calling save().
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
    }

    // ── @When steps ──────────────────────────────────────────────────────────

    /**
     * Calls PaymentService.createPayment() with the Payment prepared in @Given.
     * The service sets paymentStatus = "SUCCESS" before delegating to the repository.
     */
    @When("I create the payment")
    public void iCreateThePayment() {
        Payment input = (Payment) ctx.getResult();
        ctx.setResult(paymentService.createPayment(input));
    }

    /**
     * Calls PaymentService.getPayments() and stores the list in TestContext.
     */
    @When("I request all payments")
    public void iRequestAllPayments() {
        ctx.setResultList(paymentService.getPayments());
    }

    /**
     * Calls PaymentService.getPayment(id), capturing RuntimeException for
     * the "not found" error scenario.
     */
    @When("I request the payment with id {long}")
    public void iRequestThePaymentWithId(long id) {
        try {
            ctx.setResult(paymentService.getPayment(id));
        } catch (RuntimeException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls PaymentService.updatePayment(id, updated), capturing RuntimeException
     * for the "not found" error scenario.
     */
    @When("I update payment with id {long} to have method {string} and amount {double}")
    public void iUpdatePayment(long id, String method, double amount) {
        Payment updated = new Payment(null, null, method, null, BigDecimal.valueOf(amount), null);
        try {
            ctx.setResult(paymentService.updatePayment(id, updated));
        } catch (RuntimeException e) {
            ctx.setThrownException(e);
        }
    }

    /**
     * Calls PaymentService.deletePayment(id), capturing RuntimeException.
     */
    @When("I delete the payment with id {long}")
    public void iDeleteThePaymentWithId(long id) {
        try {
            paymentService.deletePayment(id);
        } catch (RuntimeException e) {
            ctx.setThrownException(e);
        }
    }

    // ── @Then steps ───────────────────────────────────────────────────────────

    /**
     * Asserts the id of the Payment returned by createPayment().
     */
    @Then("the created payment should have id {long}")
    public void theCreatedPaymentShouldHaveId(long id) {
        Payment payment = (Payment) ctx.getResult();
        assertThat(payment.getId()).isEqualTo(id);
    }

    /**
     * Asserts the paymentStatus of the Payment returned by createPayment().
     * This is the core "auto-set to SUCCESS" business rule verification.
     */
    @And("the created payment's status should be {string}")
    public void theCreatedPaymentStatusShouldBe(String status) {
        Payment payment = (Payment) ctx.getResult();
        assertThat(payment.getPaymentStatus()).isEqualTo(status);
    }

    /**
     * Asserts the number of payments in the list returned by getPayments().
     */
    @Then("the payment result should contain {int} payment")
    public void thePaymentResultShouldContain(int count) {
        assertThat(ctx.getResultList()).hasSize(count);
    }

    /**
     * Asserts the status of the first Payment in the result list.
     */
    @Then("the first payment's status should be {string}")
    public void theFirstPaymentStatusShouldBe(String status) {
        Payment payment = (Payment) ctx.getResultList().get(0);
        assertThat(payment.getPaymentStatus()).isEqualTo(status);
    }

    /**
     * Asserts that the payment result list is empty.
     */
    @Then("the payment result should be an empty list")
    public void thePaymentResultShouldBeAnEmptyList() {
        assertThat(ctx.getResultList()).isEmpty();
    }

    /**
     * Asserts the paymentMethod of the Payment returned by getPayment().
     */
    @Then("the returned payment should have method {string}")
    public void theReturnedPaymentMethodShouldBe(String method) {
        Payment payment = (Payment) ctx.getResult();
        assertThat(payment.getPaymentMethod()).isEqualTo(method);
    }

    /**
     * Asserts the paymentMethod of the Payment returned by updatePayment().
     */
    @Then("the updated payment should have method {string}")
    public void theUpdatedPaymentMethodShouldBe(String method) {
        Payment payment = (Payment) ctx.getResult();
        assertThat(payment.getPaymentMethod()).isEqualTo(method);
    }

    /**
     * Asserts that a plain RuntimeException was thrown with a specific message.
     * PaymentService (and RoleService) use RuntimeException for "not found" rather
     * than the custom ResourceNotFoundException used by User/Product/Order services.
     */
    @Then("a RuntimeException should be thrown with message {string}")
    public void aRuntimeExceptionShouldBeThrown(String message) {
        assertThat(ctx.getThrownException())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(message);
    }

    /**
     * Verifies that paymentRepository.deleteById was invoked with the correct id.
     */
    @And("the payment repository should have had deleteById called for id {long}")
    public void thePaymentRepositoryDeleteByIdCalledForId(long id) {
        verify(paymentRepository).deleteById(id);
    }
}
