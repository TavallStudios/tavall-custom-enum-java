package org.tavall.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CustomEnumArchitectureTest {

    // === Architecture Fixtures (Accepting & Rejecting) ===

    // ACCEPTING FIXTURE: Canonical CustomEnum subclass with private constructor and register factory
    static final class CompliantCustomEnum extends CustomEnum<CompliantCustomEnum> {
        private CompliantCustomEnum(String name) {
            super(name);
        }

        public static CompliantCustomEnum register(String name) {
            return CustomEnum.register(CompliantCustomEnum.class, name, CompliantCustomEnum::new);
        }
    }

    // REJECTING FIXTURE 1: Exposes public constructor, bypassing canonical registration cache
    static final class LeakyConstructorEnum extends CustomEnum<LeakyConstructorEnum> {
        public LeakyConstructorEnum(String name) {
            super(name);
        }
    }

    // REJECTING FIXTURE 2: Overrides equals or hashCode, violating identity semantics
    static final class BrokenIdentityEnum extends CustomEnum<BrokenIdentityEnum> {
        private BrokenIdentityEnum(String name) {
            super(name);
        }

        // Violating method (simulated check below)
    }

    // === Architecture Rules ===

    static boolean hasNoPublicConstructors(Class<? extends CustomEnum<?>> clazz) {
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (Modifier.isPublic(c.getModifiers())) {
                return false;
            }
        }
        return true;
    }

    static boolean preservesIdentityEquals(Class<? extends CustomEnum<?>> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals("equals") && Arrays.equals(m.getParameterTypes(), new Class<?>[]{Object.class})) {
                return false;
            }
            if (m.getName().equals("hashCode") && m.getParameterTypes().length == 0) {
                return false;
            }
        }
        return true;
    }

    @Test
    void testArchitectureRulesSelfValidateWithFixtures() {
        // Accepting fixture must pass both rules
        assertTrue(hasNoPublicConstructors(CompliantCustomEnum.class), "Compliant enum must have non-public constructors");
        assertTrue(preservesIdentityEquals(CompliantCustomEnum.class), "Compliant enum must not override identity equals");

        // Rejecting fixture 1 must fail constructor rule
        assertFalse(hasNoPublicConstructors(LeakyConstructorEnum.class), "Leaky constructor must be rejected");

        // Verify base class equals and hashCode are marked final
        try {
            Method equalsMethod = CustomEnum.class.getMethod("equals", Object.class);
            assertTrue(Modifier.isFinal(equalsMethod.getModifiers()), "CustomEnum.equals must be final to prevent overriding");

            Method hashCodeMethod = CustomEnum.class.getMethod("hashCode");
            assertTrue(Modifier.isFinal(hashCodeMethod.getModifiers()), "CustomEnum.hashCode must be final to prevent overriding");
        } catch (NoSuchMethodException e) {
            fail("CustomEnum must declare equals and hashCode");
        }
    }

    @Test
    void testIdentitySemanticsContract() {
        CompliantCustomEnum val1 = CompliantCustomEnum.register("TEST");
        CompliantCustomEnum val2 = CompliantCustomEnum.register("TEST");

        assertSame(val1, val2, "CustomEnum instances with identical names must be strictly identical (==)");
        assertEquals(System.identityHashCode(val1), val1.hashCode(), "hashCode must strictly match System.identityHashCode");
    }
}
