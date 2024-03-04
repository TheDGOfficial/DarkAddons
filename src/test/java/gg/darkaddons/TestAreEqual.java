package gg.darkaddons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class TestAreEqual {
    TestAreEqual() {
        super();
    }

    @Test
    final void testAreEqual() {
        final var obj = new Object();
        Assertions.assertTrue(Utils.areEqual(obj, obj));
        Assertions.assertTrue(Utils.areEqual("a", "a"));
        //noinspection StringOperationCanBeSimplified
        Assertions.assertTrue(Utils.areEqual("b", new String("b")));

        Assertions.assertFalse(Utils.areEqual(obj, new Object()));
        Assertions.assertFalse(Utils.areEqual("c", "d"));
        //noinspection StringOperationCanBeSimplified
        Assertions.assertFalse(Utils.areEqual("e", new String("f")));
    }
}
