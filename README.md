# Tavall Java Utils

Low-level, dependency-light Java utility primitives shared across Tavall projects.

The repository is part of the Tavall Java Tools family but remains independently consumable and versioned. Utilities belong here only when they are broadly useful Java primitives with no stronger domain owner such as concurrency, reflection, DI, caching, logging, or persistence.

## CustomEnum

`CustomEnum` provides strongly typed, extensible domain identifiers for places where a Java `enum` is too closed but raw strings are too weak.

A custom-enum family declares one concrete type and its well-known values:

```java
public final class ModuleProfile extends CustomEnum<ModuleProfile> {
    private static final CustomEnumRegistry<ModuleProfile> VALUES =
            CustomEnumRegistry.create("module-profile");

    public static final ModuleProfile KINGDOM = define("kingdom");
    public static final ModuleProfile FFA = define("ffa");
    public static final ModuleProfile LOBBY = define("lobby");

    private ModuleProfile(String id) {
        super(id);
    }

    public static ModuleProfile define(String id) {
        return VALUES.register(new ModuleProfile(id));
    }

    public static ModuleProfile fromId(String id) {
        return VALUES.require(id);
    }
}
```

Consumers then carry `ModuleProfile` instead of remembering magic strings:

```java
ModuleProfile id();
```

Extension modules may register additional values during bootstrap through the family-owned `define(...)` method. Once discovery is complete, the family can freeze its registry so late registrations fail rather than silently changing runtime behavior.

### Migration aliases

Aliases let old serialized/configured identifiers resolve to the new canonical value without preserving the old identity in production code:

```java
ModuleId tavallFfa = ModuleId.define("tavall-ffa");
ModuleId.registry().registerAlias("novus-ffa", tavallFfa);
```

A lookup of `novus-ffa` returns the canonical `tavall-ffa` object. The canonical object's `id()` never changes.

### Deliberate constraints

- A value is strongly typed by its concrete custom-enum family.
- Registries reject duplicate canonical IDs and aliases immediately.
- Aliases must point at the exact canonical object already owned by that registry.
- IDs are exact. The library does not silently lowercase, trim, or otherwise normalize wire/config values.
- There are no ordinal semantics because extension registration order is not a stable domain contract.
- Parsing strings belongs at configuration, serialization, command, or network boundaries. Internal APIs should pass the typed value.
- `CustomEnum` is not DI and does not discover classes or modules. A domain owns its registry and extension policy.

## Coordinates

```text
org.tavall:tavall-java-utils
```

## Build

The project targets Java 25. CI installs Gradle 9.1 and executes:

```text
gradle --no-daemon clean check
```
