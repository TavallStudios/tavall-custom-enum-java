# Tavall Custom Enum Java

A small, dependency-light Java utility for defining strongly typed enum-like values with ordinary classes.

`CustomEnum` is intentionally low level. A custom enum declares its values once in its own class, then consumers use those typed constants exactly as they would use a normal Java enum.

## Usage

Define the enum type and register its values once:

```java
import org.tavall.util.CustomEnum;

public final class ThreadType extends CustomEnum<ThreadType> {

    public static final ThreadType MAIN = register("MAIN");
    public static final ThreadType AI = register("AI");
    public static final ThreadType WORKER = register("WORKER");

    private ThreadType(String name) {
        super(name);
    }

    private static ThreadType register(String name) {
        return CustomEnum.register(new ThreadType(name));
    }
}
```

Use it elsewhere just like an enum:

```java
ThreadType type = ThreadType.AI;

void createThread(ThreadType type) {
    // ...
}

createThread(ThreadType.WORKER);
```

Java's normal type system keeps different custom-enum families separate:

```java
DatabaseType databaseType = DatabaseType.POSTGRES;

createThread(databaseType); // does not compile
```

### Enum-style operations

Each registered value exposes its declared name and declaration-order ordinal:

```java
ThreadType.AI.name();    // "AI"
ThreadType.AI.ordinal(); // 1
ThreadType.AI.toString();// "AI"
```

Values can be looked up or enumerated through `CustomEnum`:

```java
ThreadType ai = CustomEnum.valueOf(ThreadType.class, "AI");
List<ThreadType> values = CustomEnum.values(ThreadType.class);
```

`valueOf(...)` is exact and throws for unknown names. `values(...)` returns an immutable snapshot in declaration order.

Like Java enums, registered custom-enum values use identity equality. A duplicate name within the same custom-enum type is rejected during registration.

## Coordinates

```text
org.tavall:tavall-custom-enum-java
```

## Build

The project targets Java 25. CI installs Gradle 9.1 and executes:

```text
gradle --no-daemon clean check
```
