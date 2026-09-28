package com.ihorvoloshyn.autotests.reporting;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AllureSupportTest {

    @Test
    void executesRunnableStep() {
        AtomicBoolean executed = new AtomicBoolean();
        AllureSupport.step("test step", () -> executed.set(true));
        assertTrue(executed.get());
    }

    @Test
    void returnsSupplierStepResult() {
        assertEquals("result", AllureSupport.step("test step", () -> "result"));
    }

    @Test
    void rejectsInvalidStepArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> AllureSupport.step("", () -> {}));
        assertThrows(IllegalArgumentException.class,
                () -> AllureSupport.step("step", (Runnable) null));
        assertThrows(IllegalArgumentException.class,
                () -> AllureSupport.step("step", (java.util.function.Supplier<String>) null));
    }

    @Test
    void parameterAndAttachmentsAcceptNullValues() {
        assertDoesNotThrow(() -> AllureSupport.parameter("value", null));
        assertDoesNotThrow(() -> AllureSupport.attachText("text", null));
        assertDoesNotThrow(() -> AllureSupport.attachJson("json", null));
    }
}
