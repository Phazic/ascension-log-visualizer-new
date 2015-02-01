/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.text;

/**
 * <p>
 * Operations on Strings that contain words.
 * </p>
 *
 * <p>
 * This class tries to handle <code>null</code> input gracefully. An exception
 * will not be thrown for a <code>null</code> input. Each method documents its
 * behavior in more detail.
 * </p>
 *
 * @since 2.0
 * @version $Id: WordUtils.java 1436770 2013-01-22 07:09:45Z ggregory $
 */
public class WordUtils {
    /**
     * <p>
     * The {@code line.separator} System Property. Line separator (
     * <code>&quot;\n&quot;</code> on UNIX).
     * </p>
     * <p>
     * Defaults to {@code null} if the runtime does not have security access to
     * read this property or the property does not exist.
     * </p>
     * <p>
     * This value is initialized when the class is loaded. If
     * {@link System#setProperty(String,String)} or
     * {@link System#setProperties(java.util.Properties)} is called after this
     * class is loaded, the value will be out of sync with that System property.
     * </p>
     *
     * @since Java 1.1
     */
    public static final String LINE_SEPARATOR = WordUtils
            .getSystemProperty("line.separator");

    /**
     * <p>
     * <code>WordUtils</code> instances should NOT be constructed in standard
     * programming. Instead, the class should be used as
     * <code>WordUtils.wrap("foo bar", 20);</code>.
     * </p>
     *
     * <p>
     * This constructor is public to permit tools that require a JavaBean
     * instance to operate.
     * </p>
     */
    public WordUtils() {
        super();
    }

    // Wrapping
    // --------------------------------------------------------------------------
    /**
     * <p>
     * Wraps a single line of text, identifying words by <code>' '</code>.
     * </p>
     *
     * <p>
     * New lines will be separated by the system property line separator. Very
     * long words, such as URLs will <i>not</i> be wrapped.
     * </p>
     *
     * <p>
     * Leading spaces on a new line are stripped. Trailing spaces are not
     * stripped.
     * </p>
     *
     * <pre>
     * WordUtils.wrap(null, *) = null
     * WordUtils.wrap("", *) = ""
     * </pre>
     *
     * @param str
     *            the String to be word wrapped, may be null
     * @param wrapLength
     *            the column to wrap the words at, less than 1 is treated as 1
     * @return a line with newlines inserted, <code>null</code> if null input
     */
    public static String wrap(final String str, final int wrapLength) {
        return WordUtils.wrap(str, wrapLength, null, false);
    }

    /**
     * <p>
     * Wraps a single line of text, identifying words by <code>' '</code>.
     * </p>
     *
     * <p>
     * Leading spaces on a new line are stripped. Trailing spaces are not
     * stripped.
     * </p>
     *
     * <pre>
     * WordUtils.wrap(null, *, *, *) = null
     * WordUtils.wrap("", *, *, *) = ""
     * </pre>
     *
     * @param str
     *            the String to be word wrapped, may be null
     * @param wrapLength
     *            the column to wrap the words at, less than 1 is treated as 1
     * @param newLineStr
     *            the string to insert for a new line, <code>null</code> uses
     *            the system property line separator
     * @param wrapLongWords
     *            true if long words (such as URLs) should be wrapped
     * @return a line with newlines inserted, <code>null</code> if null input
     */
    public static String wrap(final String str, final int wrapLength,
            final String newLineStr, final boolean wrapLongWords) {
        String actualNewLineStr = newLineStr;
        int actualWrapLength = wrapLength;
        if (str == null) {
            return null;
        }
        if (actualNewLineStr == null) {
            actualNewLineStr = WordUtils.LINE_SEPARATOR;
        }
        if (actualWrapLength < 1) {
            actualWrapLength = 1;
        }
        final int inputLineLength = str.length();
        int offset = 0;
        final StringBuilder wrappedLine = new StringBuilder(
                inputLineLength + 32);
        while ((inputLineLength - offset) > actualWrapLength) {
            if (str.charAt(offset) == ' ') {
                offset++;
                continue;
            }
            int spaceToWrapAt = str.lastIndexOf(' ', actualWrapLength + offset);
            if (spaceToWrapAt >= offset) {
                // normal case
                wrappedLine.append(str.substring(offset, spaceToWrapAt));
                wrappedLine.append(actualNewLineStr);
                offset = spaceToWrapAt + 1;
            } else {
                // really long word or URL
                if (wrapLongWords) {
                    // wrap really long word one line at a time
                    wrappedLine.append(str.substring(offset, actualWrapLength
                            + offset));
                    wrappedLine.append(actualNewLineStr);
                    offset += actualWrapLength;
                } else {
                    // do not wrap really long word, just extend beyond limit
                    spaceToWrapAt = str.indexOf(' ', actualWrapLength + offset);
                    if (spaceToWrapAt >= 0) {
                        wrappedLine
                                .append(str.substring(offset, spaceToWrapAt));
                        wrappedLine.append(actualNewLineStr);
                        offset = spaceToWrapAt + 1;
                    } else {
                        wrappedLine.append(str.substring(offset));
                        offset = inputLineLength;
                    }
                }
            }
        }
        // Whatever is left in line is short enough to just pass through
        wrappedLine.append(str.substring(offset));
        return wrappedLine.toString();
    }

    /**
     * <p>
     * Gets a System property, defaulting to {@code null} if the property cannot
     * be read.
     * </p>
     * <p>
     * If a {@code SecurityException} is caught, the return value is
     * {@code null} and a message is written to {@code System.err}.
     * </p>
     *
     * @param property
     *            the system property name
     * @return the system property value or {@code null} if a security problem
     *         occurs
     */
    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            System.err
                    .println("Caught a SecurityException reading the system property '"
                            + property
                            + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }
}
