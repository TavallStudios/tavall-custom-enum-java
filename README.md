# Tavall Custom Enum Java

A small, dependency-light Java utility for defining strongly typed, dynamically extensible enum-like values with ordinary classes.

`CustomEnum` is intentionally low level. A concrete custom-enum class defines one type-safe enum family and binds construction once. Values can then be registered from any consumer class without modifying the enum-family class itself.

## Usage

Define the enum family in the consuming project:

```java
import org.tavall.util.CustomEnum;

public final class ThreadType extends CustomEnum<ThreadType> {

    private ThreadType(String name) {
        super(name);
    }

    public static ThreadType register(String name) {
        return CustomEnum.register(
                ThreadType.class,
                name,
                ThreadType::new
        );
    }
}
```

`ThreadType` is only an example consumer class. It is not part of this library.

Values do not need to be declared inside `ThreadType`. Any class or module can contribute them:

```java
public final class DatabaseThreadTypes {

    public static final ThreadType DATABASE =
            ThreadType.register("DATABASE");

    private DatabaseThreadTypes() {
    }
}
```

```java
public final class AiThreadTypes {

    public static final ThreadType AI =
            ThreadType.register("AI");

    private AiThreadTypes() {
    }
}
```

Consumers retain ordinary Java type safety:

```java
void createThread(ThreadType type) {
    // ...
}

createThread(DatabaseThreadTypes.DATABASE);
createThread(AiThreadTypes.AI);
```

A different custom-enum family still cannot be passed accidentally:

```java
DatabaseType databaseType = DatabaseTypes.POSTGRES;

createThread(databaseType); // does not compile
```

## Canonical registration

Registration is map-backed and canonical within each custom-enum family.

```java
ThreadType first = ThreadType.register("DATABASE");
ThreadType second = ThreadType.register("DATABASE");

first == second; // true
```

This means separate modules may independently register the same named value and converge on the same typed object rather than creating duplicates or requiring a central enum declaration to be edited.

Different custom-enum families have independent namespaces, so the same name may safely exist in both:

```java
ThreadType threadType = ThreadType.register("DATABASE");
DatabaseType databaseType = DatabaseType.register("DATABASE");

threadType.equals(databaseType); // false
```

## Lookup and enumeration

Generic lookup and enumeration live on `CustomEnum`; concrete families do not need facade methods just to repeat them:

```java
ThreadType database =
        CustomEnum.valueOf(ThreadType.class, "DATABASE");

List<ThreadType> threadTypes =
        CustomEnum.values(ThreadType.class);
```

`valueOf(...)` is exact and throws for an unknown name. `values(...)` returns an immutable snapshot in registration order.

Each value also exposes its registered name:

```java
DatabaseThreadTypes.DATABASE.name();
DatabaseThreadTypes.DATABASE.toString();
```

Registered values use identity equality. There is intentionally no enum-style ordinal because values may be contributed dynamically from independent classes and modules, making initialization order unsuitable as a stable identifier.

## Structure

The library itself contains only the generic support code:

```text
src/main/java/org/tavall/util/
├── CustomEnum.java
└── EnumValues.java
```

Concrete families such as `ThreadType` belong in consumer projects.

## Coordinates

```text
org.tavall:tavall-custom-enum-java
```

## Build

The project targets Java 25. CI installs Gradle 9.1 and executes:

```text
gradle --no-daemon clean check
```
