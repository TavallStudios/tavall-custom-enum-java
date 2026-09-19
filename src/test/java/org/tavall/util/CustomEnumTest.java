package org.tavall.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomEnumTest {

    @Test
    void registeredValuesBehaveLikeEnumConstants() {
        assertSame(ThreadType.MAIN, CustomEnum.valueOf(ThreadType.class, "MAIN"));
        assertSame(ThreadType.AI, CustomEnum.valueOf(ThreadType.class, "AI"));
        assertEquals(List.of(ThreadType.MAIN, ThreadType.AI, ThreadType.WORKER),
                CustomEnum.values(ThreadType.class));

        assertEquals("MAIN", ThreadType.MAIN.name());
        assertEquals(0, ThreadType.MAIN.ordinal());
        assertEquals(1, ThreadType.AI.ordinal());
        assertEquals(2, ThreadType.WORKER.ordinal());
        assertEquals("AI", ThreadType.AI.toString());
    }

    @Test
    void valuesLookupInitializesTheConcreteType() {
        assertEquals(List.of(LazyType.FIRST, LazyType.SECOND), CustomEnum.values(LazyType.class));
        assertSame(LazyType.SECOND, CustomEnum.valueOf(LazyType.class, "SECOND"));
    }

    @Test
    void duplicateNamesWithinOneTypeFailImmediately() {
        assertThrows(IllegalArgumentException.class, () -> DuplicateType.registerDuplicate("ONE"));
    }

    @Test
    void differentTypesMayUseTheSameNameWithoutBecomingEqual() {
        assertEquals("AI", ThreadType.AI.name());
        assertEquals("AI", DatabaseType.AI.name());
        assertNotEquals(ThreadType.AI, DatabaseType.AI);
    }

    @Test
    void lookupIsExact() {
        assertThrows(IllegalArgumentException.class,
                () -> CustomEnum.valueOf(ThreadType.class, "ai"));
        assertThrows(IllegalArgumentException.class,
                () -> CustomEnum.valueOf(ThreadType.class, "missing"));
    }

    @Test
    void namesRejectNullBlankAndPaddedValues() {
        assertThrows(NullPointerException.class, () -> InvalidType.create(null));
        assertThrows(IllegalArgumentException.class, () -> InvalidType.create(""));
        assertThrows(IllegalArgumentException.class, () -> InvalidType.create("   "));
        assertThrows(IllegalArgumentException.class, () -> InvalidType.create(" AI"));
        assertThrows(IllegalArgumentException.class, () -> InvalidType.create("AI "));
    }

    @Test
    void valuesSnapshotIsImmutable() {
        List<ThreadType> values = CustomEnum.values(ThreadType.class);
        assertThrows(UnsupportedOperationException.class, () -> values.add(ThreadType.AI));
    }
}

final class ThreadType extends CustomEnum<ThreadType> {

    static final ThreadType MAIN = register("MAIN");
    static final ThreadType AI = register("AI");
    static final ThreadType WORKER = register("WORKER");

    private ThreadType(String name) {
        super(name);
    }

    private static ThreadType register(String name) {
        return CustomEnum.register(new ThreadType(name));
    }
}

final class DatabaseType extends CustomEnum<DatabaseType> {

    static final DatabaseType AI = register("AI");

    private DatabaseType(String name) {
        super(name);
    }

    private static DatabaseType register(String name) {
        return CustomEnum.register(new DatabaseType(name));
    }
}

final class LazyType extends CustomEnum<LazyType> {

    static final LazyType FIRST = register("FIRST");
    static final LazyType SECOND = register("SECOND");

    private LazyType(String name) {
        super(name);
    }

    private static LazyType register(String name) {
        return CustomEnum.register(new LazyType(name));
    }
}

final class DuplicateType extends CustomEnum<DuplicateType> {

    static final DuplicateType ONE = register("ONE");

    private DuplicateType(String name) {
        super(name);
    }

    static DuplicateType registerDuplicate(String name) {
        return CustomEnum.register(new DuplicateType(name));
    }

    private static DuplicateType register(String name) {
        return CustomEnum.register(new DuplicateType(name));
    }
}

final class InvalidType extends CustomEnum<InvalidType> {

    private InvalidType(String name) {
        super(name);
    }

    static InvalidType create(String name) {
        return new InvalidType(name);
    }
}
