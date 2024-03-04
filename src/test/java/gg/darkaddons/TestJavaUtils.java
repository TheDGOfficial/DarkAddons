package gg.darkaddons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

final class TestJavaUtils {
    TestJavaUtils() {
        super();
    }

    @Test
    final void testCreateGenericArray() {
        final DarkAddons[] darkAddonsGenericArray = JavaUtils.createGenericArray();

        Assertions.assertEquals(0, darkAddonsGenericArray.length);
        Assertions.assertSame(DarkAddons.class, darkAddonsGenericArray.getClass().getComponentType());

        SubCommand.registerAll();
        final var helpCmd = SubCommand.match("help");

        final var subCommandsGenericArray = JavaUtils.createGenericArray(null, helpCmd);

        Assertions.assertEquals(2, subCommandsGenericArray.length);
        Assertions.assertSame(SubCommand.class, subCommandsGenericArray.getClass().getComponentType());

        Assertions.assertSame(null, subCommandsGenericArray[0]);
        Assertions.assertSame(helpCmd, subCommandsGenericArray[1]);

        Assertions.assertSame(Object.class, JavaUtils.createGenericArray().getClass().getComponentType());
    }

    @Test
    final void testSneakyThrow() {
        Assertions.assertThrows(IOException.class, () -> JavaUtils.sneakyThrow(new IOException()));
        Assertions.assertThrows(UncheckedIOException.class, () -> JavaUtils.sneakyThrow(new UncheckedIOException(new IOException())));

        Assertions.assertThrows(Exception.class, () -> JavaUtils.sneakyThrow(new Exception()));
        Assertions.assertThrows(RuntimeException.class, () -> JavaUtils.sneakyThrow(new RuntimeException()));

        Assertions.assertThrows(Throwable.class, () -> JavaUtils.sneakyThrow(new Throwable()));
    }

    @Test
    final void testGetReifiedType() {
        Assertions.assertSame(DarkAddons.class, JavaUtils.<DarkAddons>getReifiedType());
        Assertions.assertSame(SubCommand.class, JavaUtils.<SubCommand>getReifiedType());

        Assertions.assertSame(Object.class, JavaUtils.getReifiedType());
    }
}
