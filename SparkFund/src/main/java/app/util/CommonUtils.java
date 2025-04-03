package app.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

@UtilityClass
public class CommonUtils {

    public static boolean areAllNull(Object... values) {
        return Arrays.stream(values).allMatch(Objects::isNull);
    }

    public static boolean isZeroAmount(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.isEmpty();
    }
}
