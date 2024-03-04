package gg.darkaddons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class TestFillArray {
    TestFillArray() {
        super();
    }

    @Test
    final void testFillArray() {
        try {
            final var floatArray = new float[100];
            Utils.fillFloatArray(floatArray, Float.MIN_VALUE);

            for (final var f : floatArray) {
                Assertions.assertEquals(Float.MIN_VALUE, f);
            }

            final var intArray = new int[100];
            Utils.fillIntArray(intArray, Integer.MAX_VALUE);

            for (final var i : intArray) {
                Assertions.assertEquals(Integer.MAX_VALUE, i);
            }
        } catch (final OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            throw outOfMemoryError;
        }
    }
}
