package org.tavall.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Canonical registry for one {@link CustomEnum} family.
 *
 * <p>Registries preserve definition order, reject duplicate identifiers, support migration
 * aliases, and may be frozen after module bootstrap to reject late mutations.</p>
 *
 * @param <T> concrete custom-enum family
 */
public final class CustomEnumRegistry<T extends CustomEnum<T>> {

    private final String typeName;
    private final Map<String, T> values = new LinkedHashMap<>();
    private final Map<String, T> aliases = new LinkedHashMap<>();
    private boolean frozen;

    private CustomEnumRegistry(String typeName) {
        this.typeName = CustomEnum.requireIdentifier(typeName, "typeName");
    }

    public static <T extends CustomEnum<T>> CustomEnumRegistry<T> create(String typeName) {
        return new CustomEnumRegistry<>(typeName);
    }

    /** Registers and returns a canonical value. */
    public synchronized T register(T value) {
        ensureMutable();
        Objects.requireNonNull(value, "value");

        String id = value.id();
        if (values.containsKey(id) || aliases.containsKey(id)) {
            throw duplicate(id);
        }

        values.put(id, value);
        return value;
    }

    /**
     * Registers a legacy or compatibility identifier for an already canonicalized value.
     * The alias is accepted on lookup but never changes {@link CustomEnum#id()}.
     */
    public synchronized T registerAlias(String alias, T value) {
        ensureMutable();
        String checkedAlias = CustomEnum.requireIdentifier(alias, "alias");
        Objects.requireNonNull(value, "value");

        T canonical = values.get(value.id());
        if (canonical != value) {
            throw new IllegalArgumentException(
                    "Alias target must be the canonical registered " + typeName + " value: " + value.id());
        }
        if (values.containsKey(checkedAlias) || aliases.containsKey(checkedAlias)) {
            throw duplicate(checkedAlias);
        }

        aliases.put(checkedAlias, value);
        return value;
    }

    public synchronized Optional<T> find(String id) {
        if (id == null) {
            return Optional.empty();
        }
        T value = values.get(id);
        if (value == null) {
            value = aliases.get(id);
        }
        return Optional.ofNullable(value);
    }

    public synchronized T require(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException(
                "Unknown " + typeName + " identifier: " + id));
    }

    /** Returns canonical values in deterministic registration order. */
    public synchronized List<T> values() {
        return Collections.unmodifiableList(new ArrayList<>(values.values()));
    }

    /** Returns canonical identifiers in deterministic registration order. */
    public synchronized Set<String> ids() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(values.keySet()));
    }

    /** Returns migration aliases in deterministic registration order. */
    public synchronized Map<String, T> aliases() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(aliases));
    }

    /** Permanently rejects later registrations. */
    public synchronized void freeze() {
        frozen = true;
    }

    public synchronized boolean isFrozen() {
        return frozen;
    }

    public String typeName() {
        return typeName;
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException(typeName + " registry is frozen");
        }
    }

    private IllegalArgumentException duplicate(String id) {
        return new IllegalArgumentException("Duplicate " + typeName + " identifier or alias: " + id);
    }
}
