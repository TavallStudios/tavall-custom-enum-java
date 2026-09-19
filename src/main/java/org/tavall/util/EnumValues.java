package org.tavall.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Internal value storage for one concrete {@link CustomEnum} type. */
final class EnumValues<T extends CustomEnum<T>> {

    private final Class<T> type;
    private final Map<String, T> byName = new LinkedHashMap<>();
    private final List<T> ordered = new ArrayList<>();

    EnumValues(Class<T> type) {
        this.type = type;
    }

    synchronized T register(T value) {
        String name = value.name();
        if (byName.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Duplicate " + type.getSimpleName() + " value: " + name);
        }

        value.assignOrdinal(ordered.size());
        byName.put(name, value);
        ordered.add(value);
        return value;
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
        return List.copyOf(ordered);
    }
}
