package org.tavall.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Internal canonical value storage for one concrete {@link CustomEnum} family. */
final class EnumValues<T extends CustomEnum<T>> {

    private final Class<T> type;
    private final Map<String, T> byName = new LinkedHashMap<>();

    EnumValues(Class<T> type) {
        this.type = type;
    }

    synchronized T getOrRegister(String name, Function<String, T> factory) {
        T existing = byName.get(name);
        if (existing != null) {
            return existing;
        }

        T created = Objects.requireNonNull(factory.apply(name), "factory returned null");
        if (!type.isInstance(created)) {
            throw new IllegalArgumentException(
                    "Factory did not create a " + type.getSimpleName() + " value");
        }
        if (!name.equals(created.name())) {
            throw new IllegalArgumentException(
                    "Factory created " + type.getSimpleName() + " value named "
                            + created.name() + " for requested name " + name);
        }

        byName.put(name, created);
        return created;
    }

    synchronized T valueOf(String name) {
        T value = byName.get(name);
        if (value == null) {
            throw new IllegalArgumentException(
                    "No " + type.getSimpleName() + " value named " + name);
        }
        return value;
    }

    synchronized List<T> values() {
        return List.copyOf(byName.values());
    }
}
