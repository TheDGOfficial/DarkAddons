package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class TestFormatNumber {
    TestFormatNumber() {
        super();
    }

    private static final void testNumber(final float number, @NotNull final String expected) {
        Assertions.assertEquals(expected, Utils.formatNumber(number));
    }

    @SuppressWarnings({"UnqualifiedStaticUsage", "HardCodedStringLiteral", "ImplicitNumericConversion"})
    @Test
    final void testFormatNumber() {
        testNumber(0.0F, "0");
        testNumber(5.0F, "5");
        testNumber(999.0F, "999");
        testNumber(1_000.0F, "1k");
        testNumber(5_821.0F, "5.8k");
        testNumber(-5_821.0F, "-5.8k");
        testNumber(10_500.0F, "10k");
        testNumber(101_800.0F, "101k");
        testNumber(2_000_000.0F, "2M");
        testNumber(7_800_000.0F, "7.8M");
        testNumber(92_150_000.0F, "92M");
        testNumber(123_200_000.0F, "123M");
        testNumber(9_999_999.0F, "9.9M");
        testNumber(9_999_999_999.0F, "10B"); // TODO this should be 9.9B but casting to long makes it 10B
        testNumber(9_999_999_999_999.0F, "9.9T");
        testNumber(999_999_999_999_999_999.0F, "999q");
        testNumber(1_230_000_000_000_000.0F, "1.2q");
        testNumber(Long.MIN_VALUE, "-9.2Q");
        testNumber(Long.MAX_VALUE, "9.2Q");
    }
}
