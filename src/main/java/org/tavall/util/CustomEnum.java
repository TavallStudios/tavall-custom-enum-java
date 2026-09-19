package org.tavall.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base type for strongly typed, enum-like values declared with ordinary Java classes.
 *
 * <p>Each concrete subclass declares its values once by calling {@link #register(CustomEnum)}.
 * Registered values are unique by name within their concrete type, preserve declaration order,
 * expose enum-style names and ordinals, and use identity equality just like Java enums.</p>
 *
 * @param <T> concrete custom-enum type
 */
public abstract class CustomEnum<T extends CustomEnum<T>> {

    private static final Map<Class<?>, EnumValues<?>> VALUES = new ConcurrentHashMap<>();

    private final String name;
    private int ordinal = -1;

    protected CustomEnum(String name) {
        this.name = requireName(name);
    }

    /** Returns the declared name of this value. */
    public final String name() {
        return name;
    }

    /** Returns the declaration-order ordinal of this value. */
    public final int ordinal() {
        if (ordinal < 0) {
            throw new IllegalStateException("Custom enum value is not registered: " + name);
        }
        return ordinal;
    }

    /**
     * Registers one value for its concrete custom-enum type and returns that same instance.
     * Duplicate names within the same type are rejected immediately.
     */
    protected static <T extends CustomEnum<T>> T register(T value) {
        Objects.requireNonNull(value, "value");

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();

        return valuesFor(type).register(value);
    }

    /** Returns the registered value named {@code name} for {@code type}. */
    public static <T extends CustomEnum<T>> T valueOf(Class<T> type, String name) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(name, "name");
        ensureInitialized(type);
        return valuesFor(type).valueOf(name);
    }

    /** Returns the registered values for {@code type} in declaration order. */
    public static <T extends CustomEnum<T>> List<T> values(Class<T> type) {
        Objects.requireNonNull(type, "type");
        ensureInitialized(type);
        return valuesFor(type).values();
    }

    final void assignOrdinal(int ordinal) {
        if (this.ordinal >= 0) {
            throw new IllegalStateException("Custom enum value is already registered: " + name);
        }
        this.ordinal = ordinal;
    }

    @Override
    public final String toString() {
        return name;
    }

    @Override
    public final boolean equals(Object other) {
        return this == other;
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    static String requireName(String value) {
        Objects.requireNonNull(value, "name");
        if (value.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (!value.equals(value.trim())) {
            throw new IllegalArgumentException("name must not have leading or trailing whitespace");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomEnum<T>> EnumValues<T> valuesFor(Class<T> type) {
        return (EnumValues<T>) VALUES.computeIfAbsent(type, ignored -> new EnumValues<>(type));
    }

    private static void ensureInitialized(Class<?> type) {
        try {
            Class.forName(type.getName(), true, type.getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to initialize custom enum type: " + type.getName(), exception);
        }
    }
}
