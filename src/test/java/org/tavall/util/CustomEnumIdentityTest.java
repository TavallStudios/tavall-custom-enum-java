package org.tavall.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tavall Architectural Role: Unit test for CustomEnum registration identity behavior.
 * Ownership: Tavall Custom Enum test suite; verifies canonical registration and identity hashing.
 */
final class CustomEnumIdentityTest {
    @Test
    void registeringTheSameNameReturnsTheCanonicalInstance() {
        RegisteredCustomEnum first = RegisteredCustomEnum.register("TEST");
        RegisteredCustomEnum second = RegisteredCustomEnum.register("TEST");

        assertSame(first, second);
        assertEquals(System.identityHashCode(first), first.hashCode());
    }

    private static final class RegisteredCustomEnum extends CustomEnum<RegisteredCustomEnum> {
        private RegisteredCustomEnum(String name) {
            super(name);
        }

        private static RegisteredCustomEnum register(String name) {
            return CustomEnum.register(RegisteredCustomEnum.class, name, RegisteredCustomEnum::new);
        }
    }
}
