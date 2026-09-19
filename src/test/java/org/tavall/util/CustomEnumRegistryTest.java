package org.tavall.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomEnumRegistryTest {

    @Test
    void canonicalValuesAreTypedAndOrdered() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("test-value");
        TestValue first = registry.register(new TestValue("first"));
        TestValue second = registry.register(new TestValue("second"));

        assertSame(first, registry.require("first"));
        assertEquals(List.of(first, second), registry.values());
        assertEquals(List.of("first", "second"), registry.ids().stream().toList());
        assertEquals("first", first.id());
        assertEquals("first", first.toString());
    }

    @Test
    void getOrRegisterCanonicalizesDescriptorDefinedValues() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-id");
        AtomicInteger factoryCalls = new AtomicInteger();

        TestValue first = registry.getOrRegister("tavall-ffa", id -> {
            factoryCalls.incrementAndGet();
            return new TestValue(id);
        });
        TestValue second = registry.getOrRegister("tavall-ffa", id -> {
            factoryCalls.incrementAndGet();
            return new TestValue(id);
        });

        assertSame(first, second);
        assertEquals(1, factoryCalls.get());
        assertEquals(List.of(first), registry.values());
    }

    @Test
    void getOrRegisterResolvesAliasesBeforeCreatingValues() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-id");
        TestValue canonical = registry.register(new TestValue("tavall-ffa"));
        registry.registerAlias("novus-ffa", canonical);

        TestValue resolved = registry.getOrRegister("novus-ffa", id -> {
            throw new AssertionError("factory must not run for an alias");
        });

        assertSame(canonical, resolved);
    }

    @Test
    void getOrRegisterRequiresFactoryIdentityToMatchRequestedId() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-id");

        assertThrows(IllegalArgumentException.class,
                () -> registry.getOrRegister("tavall-ffa", ignored -> new TestValue("different")));
        assertFalse(registry.find("tavall-ffa").isPresent());
    }

    @Test
    void frozenRegistryStillResolvesExistingValuesButRejectsNewOnes() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-id");
        TestValue canonical = registry.register(new TestValue("tavall-ffa"));
        registry.freeze();

        assertSame(canonical, registry.getOrRegister("tavall-ffa", TestValue::new));
        assertThrows(IllegalStateException.class,
                () -> registry.getOrRegister("tavall-lobby", TestValue::new));
    }

    @Test
    void aliasesResolveToCanonicalValueWithoutChangingItsIdentity() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-id");
        TestValue tavallFfa = registry.register(new TestValue("tavall-ffa"));

        registry.registerAlias("novus-ffa", tavallFfa);

        assertSame(tavallFfa, registry.require("novus-ffa"));
        assertEquals("tavall-ffa", registry.require("novus-ffa").id());
    }

    @Test
    void duplicateIdentifiersAndAliasesFailImmediately() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-id");
        TestValue value = registry.register(new TestValue("tavall-ffa"));
        registry.registerAlias("novus-ffa", value);

        assertThrows(IllegalArgumentException.class,
                () -> registry.register(new TestValue("tavall-ffa")));
        assertThrows(IllegalArgumentException.class,
                () -> registry.register(new TestValue("novus-ffa")));
        assertThrows(IllegalArgumentException.class,
                () -> registry.registerAlias("novus-ffa", value));
        assertThrows(IllegalArgumentException.class,
                () -> registry.registerAlias("tavall-ffa", value));
    }

    @Test
    void aliasTargetsMustBeTheRegisteredCanonicalObject() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-id");
        registry.register(new TestValue("tavall-ffa"));

        assertThrows(IllegalArgumentException.class,
                () -> registry.registerAlias("novus-ffa", new TestValue("tavall-ffa")));
    }

    @Test
    void freezingRegistryRejectsLateExtensions() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-profile");
        TestValue ffa = registry.register(new TestValue("ffa"));
        registry.freeze();

        assertTrue(registry.isFrozen());
        assertThrows(IllegalStateException.class,
                () -> registry.register(new TestValue("lobby")));
        assertThrows(IllegalStateException.class,
                () -> registry.registerAlias("legacy-ffa", ffa));
    }

    @Test
    void lookupIsExplicitAndDoesNotNormalizeMagicStrings() {
        CustomEnumRegistry<TestValue> registry = CustomEnumRegistry.create("module-profile");
        registry.register(new TestValue("ffa"));

        assertTrue(registry.find("ffa").isPresent());
        assertFalse(registry.find("FFA").isPresent());
        assertFalse(registry.find(null).isPresent());
        assertThrows(IllegalArgumentException.class, () -> registry.require("missing"));
    }

    @Test
    void identifiersRejectBlankOrPaddedValues() {
        assertThrows(NullPointerException.class, () -> new TestValue(null));
        assertThrows(IllegalArgumentException.class, () -> new TestValue(""));
        assertThrows(IllegalArgumentException.class, () -> new TestValue("   "));
        assertThrows(IllegalArgumentException.class, () -> new TestValue(" ffa"));
        assertThrows(IllegalArgumentException.class, () -> new TestValue("ffa "));
    }

    @Test
    void separateCustomEnumFamiliesDoNotBecomeEqualByStringAccident() {
        TestValue profile = new TestValue("ffa");
        OtherValue module = new OtherValue("ffa");

        assertNotEquals(profile, module);
    }

    private static final class TestValue extends CustomEnum<TestValue> {
        private TestValue(String id) {
            super(id);
        }
    }

    private static final class OtherValue extends CustomEnum<OtherValue> {
        private OtherValue(String id) {
            super(id);
        }
    }
}
