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

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

public final class MafiaFreeRunawaysLineParser extends AbstractLineParser {
    private static final String BANDER_RUNAWAY_MESSAGE_PART_STRING = " snatches you up in his jaws,"
            + " tosses you onto his back, and flooms away,"
            + " weaving slightly and hiccelping fire.";
    private static final String POPPER_USAGE_END_STRING = " uses the divine champagne popper!";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        logData.getTurnsSpent().last().incrementSuccessfulFreeRunaways(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return line
                .startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING)
                && (line.endsWith(MafiaFreeRunawaysLineParser.POPPER_USAGE_END_STRING) || line
                        .contains(MafiaFreeRunawaysLineParser.BANDER_RUNAWAY_MESSAGE_PART_STRING));
    }
}
