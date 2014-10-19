/* Copyright (c) 2008-2010, developers of the Ascension Log Visualizer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.googlecode.logVisualizer.parser.lineParsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.turnAction.Pull;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the pull notation in preparsed ascension logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code #> Turn [*turnNumber*] pulled *amount* *itemName*}
 */
public final class PullLineParser extends AbstractLineParser {
    private static final Pattern NOT_PULL_STRING = Pattern
            .compile("^.*\\]\\s*pulled\\s*|,\\s*");
    private static final Pattern NOT_PULL_NAME = Pattern.compile("^\\d+\\s*");
    private final Matcher pullMatcher = UsefulPatterns.PULL
            .matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        // Parse the turn number
        try (Scanner scanner = new Scanner(line)) {
            scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
            final int turnNumber = scanner.nextInt();
            scanner.close();
            // Get current day number
            final int dayNumber = logData.getLastDayChange().getDayNumber();
            // Parse out all single pull strings (some older versions of the AFH
            // parser had pulls in one single line)
            try (Scanner scanner2 = new Scanner(line)) {
                scanner2.useDelimiter(PullLineParser.NOT_PULL_STRING);
                final List<String> pulls = new ArrayList<>();
                while (scanner2.hasNext()) {
                    pulls.add(scanner2.next());
                }
                scanner2.close();
                for (@SuppressWarnings("unused")
                final String s : pulls) {
                    // Parse number of items pulled
                    try (Scanner scanner3 = new Scanner(line)) {
                        scanner3.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                        final int numberOfItems = scanner3.nextInt();
                        scanner3.close();
                        // Parse item name
                        try (Scanner scanner4 = new Scanner(line)) {
                            scanner4.useDelimiter(PullLineParser.NOT_PULL_NAME);
                            final String itemName = scanner4.next();
                            scanner4.close();
                            // Add pull
                            logData.addPull(new Pull(itemName, numberOfItems,
                                    turnNumber, dayNumber));
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return this.pullMatcher.reset(line).matches();
    }
}