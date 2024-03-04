package gg.darkaddons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class TestHashMapOf {
    TestHashMapOf() {
        super();
    }

    @Test
    final void testHashMapOf() {
        final var stringStringHashMap = Utils.hashMapOf("hello", "world");

        Assertions.assertEquals(1, stringStringHashMap.size());

        Assertions.assertEquals("hello", stringStringHashMap.keySet().iterator().next());
        Assertions.assertEquals("world", stringStringHashMap.values().iterator().next());
    }
}
