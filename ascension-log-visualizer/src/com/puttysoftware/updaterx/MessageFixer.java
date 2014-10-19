package com.puttysoftware.updaterx;

import org.apache.commons.lang3.text.WordUtils;

public class MessageFixer {
    public static String fix(final String message, final int wrapCount) {
        String fixed = message.replaceAll("\n", " ");
        fixed = fixed.replaceAll("\r", "");
        return WordUtils.wrap(fixed, wrapCount);
    }

    public static String fix(final String[] messages, final int wrapCount) {
        final String[] fixed = new String[messages.length];
        String combined = "";
        for (int x = 0; x < fixed.length; x++) {
            fixed[x] = WordUtils.wrap(messages[x], wrapCount);
            if (x == fixed.length - 1) {
                combined += fixed[x];
            } else {
                combined += fixed[x] + "\n";
            }
        }
        return combined;
    }
}
