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
import com.googlecode.logVisualizer.parser.UsefulPatterns;

public final class MafiaDisintegrateLineParser extends AbstractLineParser {
    private static final Pattern MAJOR_YELLOW_RAY = Pattern
            .compile("Round \\d+: .+? swings his eyestalk around and unleashes a massive"
                    + " ray of yellow energy, completely disintegrating your opponent."
                    + "|Nanobots stream out of your fingers and explode your foe with a flash of bright yellow light"
                    + "|After it goes off in a flash of dazzling yellow and flying pumpkin guts"
                    + "|You toss the ball of light at your opponent."
                    + "|You toss the light at your opponent. "
                    + "|You toss the taffy, and the salt water soaks into it. There is a burst of yellow "
                    + "|You light the bomb and toss it in your opponent's direction. What ensues is like mutually assured destruction, except for the mutual part."
                    + "|You flash your headlight. A sweet guitar riff plays as your opponent is vaporized."
                    + "|A blinding ball of lightning bursts in front of her. When your vision returns"
                    + "|Round \\d+: .+? casts OPEN A BIG YELLOW PRESENT!");
    private final Matcher majorYellowRayMatcher = MafiaDisintegrateLineParser.MAJOR_YELLOW_RAY
            .matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        logData.getTurnsSpent().last().getTurns().last().setDisintegrated(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return line
                .startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING)
                && this.majorYellowRayMatcher.reset(line).matches();
    }
}
