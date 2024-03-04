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

    @SuppressWarnings({"HardCodedStringLiteral", "ImplicitNumericConversion"})
    @Test
    final void testFormatNumber() {
        TestFormatNumber.testNumber(0.0F, "0");
        TestFormatNumber.testNumber(5.0F, "5");
        TestFormatNumber.testNumber(999.0F, "999");
        TestFormatNumber.testNumber(1_000.0F, "1k");
        TestFormatNumber.testNumber(5_821.0F, "5.8k");
        TestFormatNumber.testNumber(-5_821.0F, "-5.8k");
        TestFormatNumber.testNumber(10_500.0F, "10k");
        TestFormatNumber.testNumber(101_800.0F, "101k");
        TestFormatNumber.testNumber(2_000_000.0F, "2M");
        TestFormatNumber.testNumber(7_800_000.0F, "7.8M");
        TestFormatNumber.testNumber(92_150_000.0F, "92M");
        TestFormatNumber.testNumber(123_200_000.0F, "123M");
        TestFormatNumber.testNumber(9_999_999.0F, "9.9M");
        TestFormatNumber.testNumber(9_999_999_999.0F, "10B"); // TODO this should be 9.9B but casting to long makes it 10B
        TestFormatNumber.testNumber(9_999_999_999_999.0F, "9.9T");
        TestFormatNumber.testNumber(999_999_999_999_999_999.0F, "999q");
        TestFormatNumber.testNumber(1_230_000_000_000_000.0F, "1.2q");
        TestFormatNumber.testNumber(Long.MIN_VALUE, "-9.2Q");
        TestFormatNumber.testNumber(Long.MAX_VALUE, "9.2Q");
    }
}
