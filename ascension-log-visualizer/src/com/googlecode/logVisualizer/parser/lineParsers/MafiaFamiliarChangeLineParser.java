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

import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;

/**
 * A parser for the familiar change notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code familiar _familiarClassName_ (_weight_ lbs)}
 */
public final class MafiaFamiliarChangeLineParser extends AbstractLineParser {
    private static final Pattern FAMILIAR_CHANGE_CAPTURE_PATTERN = Pattern
            .compile("familiar ([\\w\\p{Punct}\\s]+) \\((\\d+) lbs\\)");
    private static final String FAMILIAR_CHANGE_START_STRING = "familiar ";
    private static final String NO_FAMILIAR_STRING = "none";
    private static final String LOCK_STRING = "lock";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        if (!line.endsWith(MafiaFamiliarChangeLineParser.LOCK_STRING)) {
            final String familiarName;
            if (!line
                    .endsWith(MafiaFamiliarChangeLineParser.NO_FAMILIAR_STRING)) {
                try (final Scanner scanner = new Scanner(line)) {
                    scanner.findInLine(MafiaFamiliarChangeLineParser.FAMILIAR_CHANGE_CAPTURE_PATTERN);
                    final MatchResult result = scanner.match();
                    familiarName = result.group(1);
                }
            } else {
                familiarName = MafiaFamiliarChangeLineParser.NO_FAMILIAR_STRING;
            }
            logData.addFamiliarChange(new FamiliarChange(familiarName, logData
                    .getTurnsSpent().last().getEndTurn()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return line
                .startsWith(MafiaFamiliarChangeLineParser.FAMILIAR_CHANGE_START_STRING);
    }
}
