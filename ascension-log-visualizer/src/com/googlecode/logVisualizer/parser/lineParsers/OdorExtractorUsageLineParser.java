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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser to recognise whether an odor extractor was used during combat (and
 * thus the given combat was started to be hunted).
 * <p>
 * Please note that a used odor extractor will be saved as a consumed item, but
 * its day number of usage will be saved as {@code Integer.MAX_VALUE}.
 * <p>
 * The format looks like this:
 * <p>
 * {@code Round _roundNumber_: _userName_ uses the odor extractor!}
 */
public final class OdorExtractorUsageLineParser extends AbstractLineParser {
    private static final Pattern ODOR_EXTRACTOR_USAGE_PATTERN = Pattern
            .compile("Round \\d+: .+ uses the odor extractor!");
    private final Matcher odorExtractorUsageMatcher = OdorExtractorUsageLineParser.ODOR_EXTRACTOR_USAGE_PATTERN
            .matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        final TurnInterval currentInterval = logData.getTurnsSpent().last();
        final Consumable odorExtractor = Consumable.newOtherConsumable(
                "Odor Extractor", 0, 1, currentInterval.getEndTurn());
        odorExtractor.setDayNumberOfUsage(Integer.MAX_VALUE);
        currentInterval.addConsumableUsed(odorExtractor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return this.odorExtractorUsageMatcher.reset(line).matches();
    }
}
