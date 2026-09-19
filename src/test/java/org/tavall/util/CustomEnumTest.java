package org.tavall.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomEnumTest {

    @Test
    void valuesMayBeRegisteredOutsideTheEnumFamily() {
        assertSame(DatabaseThreadTypes.DATABASE,
                CustomEnum.valueOf(ThreadType.class, "DATABASE"));
        assertSame(AiThreadTypes.AI,
                CustomEnum.valueOf(ThreadType.class, "AI"));
    }

    @Test
    void repeatedRegistrationReturnsTheCanonicalValue() {
        CanonicalType first = CanonicalType.register("SHARED");
        CanonicalType second = CanonicalType.register("SHARED");

        assertSame(first, second);
        assertEquals(1, CanonicalType.constructions());
        assertSame(first, CustomEnum.valueOf(CanonicalType.class, "SHARED"));
    }

    @Test
    void separateContributorsConvergeOnTheSameValue() {
        assertSame(DatabaseThreadTypes.DATABASE, PersistenceThreadTypes.DATABASE);
    }

    @Test
    void valuesReflectRegistrationOrder() {
        ValueOrderType first = ValueOrderType.register("FIRST");
        ValueOrderType second = ValueOrderType.register("SECOND");
        ValueOrderType third = ValueOrderType.register("THIRD");

        assertEquals(List.of(first, second, third), CustomEnum.values(ValueOrderType.class));
    }

    @Test
    void differentTypesMayUseTheSameNameWithoutBecomingEqual() {
        ThreadType threadType = ThreadType.register("DATABASE");
        DatabaseType databaseType = DatabaseType.register("DATABASE");

        assertEquals("DATABASE", threadType.name());
        assertEquals("DATABASE", databaseType.name());
        assertNotEquals(threadType, databaseType);
    }

    @Test
    void lookupIsExact() {
        ThreadType ai = ThreadType.register("AI");

        assertSame(ai, CustomEnum.valueOf(ThreadType.class, "AI"));
        assertThrows(IllegalArgumentException.class,
                () -> CustomEnum.valueOf(ThreadType.class, "ai"));
        assertThrows(IllegalArgumentException.class,
                () -> CustomEnum.valueOf(ThreadType.class, "missing"));
    }

    @Test
    void namesRejectNullBlankAndPaddedValues() {
        assertThrows(NullPointerException.class, () -> ThreadType.register(null));
        assertThrows(IllegalArgumentException.class, () -> ThreadType.register(""));
        assertThrows(IllegalArgumentException.class, () -> ThreadType.register("   "));
        assertThrows(IllegalArgumentException.class, () -> ThreadType.register(" AI"));
        assertThrows(IllegalArgumentException.class, () -> ThreadType.register("AI "));
    }

    @Test
    void valuesSnapshotIsImmutable() {
        ImmutableType value = ImmutableType.register("VALUE");
        List<ImmutableType> values = CustomEnum.values(ImmutableType.class);

        assertEquals(List.of(value), values);
        assertThrows(UnsupportedOperationException.class, () -> values.add(value));
    }

    @Test
    void registeredValuesExposeTheirNameAndUseIdentityEquality() {
        ThreadType first = ThreadType.register("WORKER");
        ThreadType second = ThreadType.register("WORKER");

        assertEquals("WORKER", first.name());
        assertEquals("WORKER", first.toString());
        assertSame(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}

final class ThreadType extends CustomEnum<ThreadType> {

    private ThreadType(String name) {
        super(name);
    }

    static ThreadType register(String name) {
        return CustomEnum.register(ThreadType.class, name, ThreadType::new);
    }
}

final class DatabaseThreadTypes {

    static final ThreadType DATABASE = ThreadType.register("DATABASE");

    private DatabaseThreadTypes() {
    }
}

final class PersistenceThreadTypes {

    static final ThreadType DATABASE = ThreadType.register("DATABASE");

    private PersistenceThreadTypes() {
    }
}

final class AiThreadTypes {

    static final ThreadType AI = ThreadType.register("AI");

    private AiThreadTypes() {
    }
}

final class DatabaseType extends CustomEnum<DatabaseType> {

    private DatabaseType(String name) {
        super(name);
    }

    static DatabaseType register(String name) {
        return CustomEnum.register(DatabaseType.class, name, DatabaseType::new);
    }
}

final class CanonicalType extends CustomEnum<CanonicalType> {

    private static int constructions;

    private CanonicalType(String name) {
        super(name);
        constructions++;
    }

    static CanonicalType register(String name) {
        return CustomEnum.register(CanonicalType.class, name, CanonicalType::new);
    }

    static int constructions() {
        return constructions;
    }
}

final class ValueOrderType extends CustomEnum<ValueOrderType> {

    private ValueOrderType(String name) {
        super(name);
    }

    static ValueOrderType register(String name) {
        return CustomEnum.register(ValueOrderType.class, name, ValueOrderType::new);
    }
}

final class ImmutableType extends CustomEnum<ImmutableType> {

    private ImmutableType(String name) {
        super(name);
    }

    static ImmutableType register(String name) {
        return CustomEnum.register(ImmutableType.class, name, ImmutableType::new);
    }
}
