package com.rockthevote.grommet.util;

public final class Strings {
    private Strings() {
        // No instances.
    }

    public static boolean isBlank(CharSequence string) {
        return (string == null
                || string.toString().trim().equals("null")
                || string.toString().trim().length() == 0);
    }

    public static String valueOrDefault(String string, String defaultString) {
        return isBlank(string) ? defaultString : string;
    }

    public static String truncateAt(String string, int length) {
        return string.length() > length ? string.substring(0, length) : string;
    }

    public static String formatNumberAsPercentage(double number) {
        return String.format("%.0f%%", number * 100);
    }
}
