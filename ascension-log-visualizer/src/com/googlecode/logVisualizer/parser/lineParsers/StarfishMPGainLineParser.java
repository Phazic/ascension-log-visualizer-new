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
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

public final class StarfishMPGainLineParser extends AbstractLineParser {
    private static final Pattern STARFISH_ATK = Pattern
            .compile("Round \\d+: .+ floats behind your opponent, and begins to glow brightly. Starlight"
                    + " shines through your opponent, doing X damage, and pours into your body.");
    private static final Pattern SPIRIT_HOBO_ATK = Pattern
            .compile("Round \\d+: .+ holds up an empty bottle of booze and gazes at it sadly."
                    + " Starlight filters through the bottle, through the spirit hobo, and"
                    + " through the booze inside the spirit hobo, then pierces your opponent"
                    + " for \\d+ damage, and then shines into you. What the hell\\?");
    private static final Pattern GGG_ATK1 = Pattern
            .compile("Round \\d+: .+ slimes your opponent thoroughly, dealing \\d+ damage."
                    + "  The resulting ectoplasmic shock wave gives you a mystical jolt.");
    private static final Pattern GGG_ATK2 = Pattern
            .compile("Round \\d+: .+ swoops through your opponent, somehow transferring \\d+ points"
                    + " of \\w+ lifeforce into \\w+ Points for you.  You feel slightly skeeved out.");
    private static final Pattern GGG_ATK3 = Pattern
            .compile("Round \\d+: .+ swoops back and forth through your opponent, scaring the bejeezus"
                    + " out of \\w+ to the tune of \\d+ damage.  Then he converts the bejeezus into \\w+ Points!");
    private static final Pattern SLIMELING_ATK = Pattern
            .compile("Round \\d+: .+ leaps on your opponent, sliming \\w+ for \\d+ damage.  It's inspiring!");
    private static final String OPPONENT_STRING = "opponent";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        // Luckily all starfish familiars have the word "opponent" inside their
        // attack messages before the damage is given.
        final String tmp = line.substring(line
                .lastIndexOf(StarfishMPGainLineParser.OPPONENT_STRING));
        try (final Scanner scanner = new Scanner(tmp)) {
            scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
            final int dmg = scanner.nextInt();
            final TurnInterval lastInterval = logData.getTurnsSpent().last();
            lastInterval.addStarfishMPGain(dmg);
            // Subtract from encounter mp gains the amount of starfish mp,
            // because
            // mafia throws the mp gain of the starfish also out in a way that
            // will
            // be catched by the MPGainLineParser.
            lastInterval.addEncounterMPGain(dmg * -1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        if (line.startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING)
                && line.contains(StarfishMPGainLineParser.OPPONENT_STRING)) {
            return StarfishMPGainLineParser.SPIRIT_HOBO_ATK.matcher(line)
                    .matches()
                    || StarfishMPGainLineParser.SLIMELING_ATK.matcher(line)
                            .matches()
                    || StarfishMPGainLineParser.STARFISH_ATK.matcher(line)
                            .matches()
                    || StarfishMPGainLineParser.GGG_ATK1.matcher(line)
                            .matches()
                    || StarfishMPGainLineParser.GGG_ATK2.matcher(line)
                            .matches()
                    || StarfishMPGainLineParser.GGG_ATK3.matcher(line)
                            .matches();
        }
        return false;
    }
}
