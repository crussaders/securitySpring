package com.manish.spring.security.cucumber.context;

/**
 * TestContext acts as a shared "world" object in Cucumber tests.
 *
 * <p>In Cucumber, each step definition class is instantiated fresh for every scenario.
 * To share state (e.g. the result returned from a service call, or an exception that was
 * thrown) between a @When step and a @Then step, we need a single mutable holder that
 * every step definition class can reference within the same scenario.
 *
 * <p>Cucumber automatically shares a single instance of this class across all step
 * definition classes that declare it as a constructor parameter (constructor injection).
 * This is the recommended, thread-safe approach because each scenario gets its own fresh
 * TestContext instance, preventing state leakage between scenarios.
 */
public class TestContext {

    // ── Generic result holder ──────────────────────────────────────────────────

    /**
     * Stores the object returned by the most recent service/controller call.
     * Declared as Object so it can hold any entity type (User, Product, Order…).
     * @Then steps cast it to the expected type for assertions.
     */
    private Object result;

    /**
     * Stores the list returned by the most recent "get all" service call.
     * Using a raw List allows the same field to hold any entity list.
     */
    private java.util.List<?> resultList;

    // ── Exception holder ───────────────────────────────────────────────────────

    /**
     * Stores the exception thrown by the service during the @When step.
     * @Then steps that validate error scenarios inspect this field instead
     * of asserting on a return value.
     */
    private Exception thrownException;

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public java.util.List<?> getResultList() {
        return resultList;
    }

    public void setResultList(java.util.List<?> resultList) {
        this.resultList = resultList;
    }

    public Exception getThrownException() {
        return thrownException;
    }

    public void setThrownException(Exception thrownException) {
        this.thrownException = thrownException;
    }
}
