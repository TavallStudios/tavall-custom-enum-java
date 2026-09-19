package org.tavall.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Base type for strongly typed, dynamically extensible enum-like values.
 *
 * <p>Concrete subclasses define an enum family and bind construction once. Values may then be
 * registered from any consumer class. Re-registering the same name for the same family returns
 * the canonical existing value.</p>
 *
 * @param <T> concrete custom-enum type
 */
public abstract class CustomEnum<T extends CustomEnum<T>> {

    private static final Map<Class<?>, EnumValues<?>> VALUES = new ConcurrentHashMap<>();

    private final String name;

    protected CustomEnum(String name) {
        this.name = requireName(name);
    }

    /** Returns the registered name of this value. */
    public final String name() {
        return name;
    }

    /**
     * Returns the canonical value for {@code name}, creating and registering it when absent.
     *
     * <p>This method is intended to be exposed by each concrete family through its one
     * type-specific registration method.</p>
     */
    protected static <T extends CustomEnum<T>> T register(
            Class<T> type,
            String name,
            Function<String, T> factory) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(factory, "factory");
        String checkedName = requireName(name);
        return valuesFor(type).getOrRegister(checkedName, factory);
    }

    /** Returns the registered value named {@code name} for {@code type}. */
    public static <T extends CustomEnum<T>> T valueOf(Class<T> type, String name) {
        Objects.requireNonNull(type, "type");
        return valuesFor(type).valueOf(requireName(name));
    }

    /** Returns an immutable snapshot of the values currently registered for {@code type}. */
    public static <T extends CustomEnum<T>> List<T> values(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return valuesFor(type).values();
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
}
