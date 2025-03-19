package app.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Objects;

@UtilityClass
public class CommonUtils {

    public static boolean areAllNull(Object... values) {
        return Arrays.stream(values).allMatch(Objects::isNull);
    }
}
