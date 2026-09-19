package org.tavall.util;

import java.util.Objects;

/**
 * Base type for extensible, enum-like domain identifiers.
 *
 * <p>A {@code CustomEnum} value is immutable and strongly typed by its concrete subclass.
 * Values are normally declared and canonicalized through a {@link CustomEnumRegistry}. Unlike
 * a Java {@code enum}, extension modules may register additional values during bootstrap.</p>
 *
 * @param <T> concrete custom-enum family
 */
public abstract class CustomEnum<T extends CustomEnum<T>> {

    private final String id;

    protected CustomEnum(String id) {
        this.id = requireIdentifier(id, "id");
    }

    /** Returns the stable serialized identifier for this value. */
    public final String id() {
        return id;
    }

    @Override
    public final String toString() {
        return id;
    }

    @Override
    public final boolean equals(Object other) {
        return this == other
                || (other != null
                && getClass() == other.getClass()
                && id.equals(((CustomEnum<?>) other).id));
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), id);
    }

    static String requireIdentifier(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (!value.equals(value.trim())) {
            throw new IllegalArgumentException(name + " must not have leading or trailing whitespace");
        }
        return value;
    }
}
