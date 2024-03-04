package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

final class TestRemoveControlCodes {
    @SuppressWarnings({"TypeMayBeWeakened", "UnsecureRandomNumberGeneration"})
    @NotNull
    private static final Random RANDOM = new Random();

    TestRemoveControlCodes() {
        super();
    }

    @Test
    final void testRemoveControlCodes() {
        final var allColorChars = "0123456789abcdefklmnorz";

        final var allColorCharArray = allColorChars.toCharArray();
        final var allColorCharArrayLength = allColorCharArray.length;

        Assertions.assertEquals(allColorChars, Utils.removeControlCodes(allColorChars));

        for (final var c : allColorCharArray) {
            Assertions.assertEquals("", Utils.removeControlCodes("§" + c));
            Assertions.assertEquals("", Utils.removeControlCodes("§" + Character.toUpperCase(c)));
        }

        for (var i = 0; 100 > i; ++i) {
            TestRemoveControlCodes.randomizedTest(allColorCharArray, allColorCharArrayLength);
        }
    }

    private static final void randomizedTest(@NotNull final char[] allColorCharArray, final int allColorCharArrayLength) {
        final var textBuilder = new StringBuilder("test".length() * (allColorCharArrayLength << 1));

        for (var i = 0; i < allColorCharArrayLength << 1; ++i) {
            textBuilder.append("test");

            if (i != (allColorCharArrayLength << 1) - 1) {
                textBuilder.append(' ');
            }
        }

        final var text = textBuilder.toString();
        final var textColored = new StringBuilder(text.length() << 1);

        final var split = text.split(" ");
        final var splitLength = split.length;

        for (var i = 0; i < splitLength; i++) {
            final var word = split[i];
            final var randomColorChar = allColorCharArray[TestRemoveControlCodes.RANDOM.nextInt(allColorCharArrayLength)];

            textColored.append('§').append(TestRemoveControlCodes.RANDOM.nextBoolean() ? Character.toUpperCase(randomColorChar) : randomColorChar).append(word);

            if (i != splitLength - 1) {
                textColored.append(' ');
            }
        }

        Assertions.assertEquals(text, Utils.removeControlCodes(textColored.toString()));
    }
}
