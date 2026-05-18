package org.example.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    @Test
    void idCanBeSetAndRead() {
        TestEntity entity = new TestEntity();

        entity.setId(42);

        assertEquals(42, entity.getId());
    }

    private static final class TestEntity extends BaseEntity {
    }
}
