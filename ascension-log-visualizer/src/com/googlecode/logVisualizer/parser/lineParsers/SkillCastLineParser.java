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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the skill cast notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code cast _amount_ _skillName_}
 * <p>
 * OR
 * <p>
 * {@code Round _roundNumber_: _accountName_ casts _skillName_!}
 * <p>
 * OR
 * <p>
 * {@code Round _roundNumber_: _accountName_ casts _skillName_! (auto-attack)}
 */
public final class SkillCastLineParser extends AbstractLineParser {
    private static final Pattern SKILL_CAST = Pattern
            .compile("cast \\d+ [\\w\\p{Punct}\\s]+|.*casts [\\w\\p{Punct}\\s]+!(?: \\(auto-attack\\))?");
    private static final Pattern COMBAT_CAST_CAPTURE_PATTERN = Pattern
            .compile(".*casts ([\\w\\p{Punct}\\s]+)!(?: \\(auto-attack\\))?");
    private static final Pattern NONCOMBAT_CAST_CAPTURE_PATTERN = Pattern
            .compile("cast (\\d+) ([\\w\\p{Punct}\\s]+)");
    private static final String COMBAT_CAST_STRING = "casts";
    private final Matcher skillCastMatcher = SkillCastLineParser.SKILL_CAST
            .matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        final String skillName;
        int amount = 1;
        // Skill casts during combats and between turns use different formats.
        if (line.contains(SkillCastLineParser.COMBAT_CAST_STRING)) {
            final Matcher result = SkillCastLineParser.COMBAT_CAST_CAPTURE_PATTERN
                    .matcher(line);
            result.find();
            skillName = result.group(1).toLowerCase(Locale.ENGLISH);
        } else {
            final Matcher result = SkillCastLineParser.NONCOMBAT_CAST_CAPTURE_PATTERN
                    .matcher(line);
            result.find();
            amount = Integer.parseInt(result.group(1));
            skillName = result.group(2).toLowerCase(Locale.ENGLISH);
        }
        // Add the skill to the current turn and turn interval.
        final TurnInterval currentInterval = logData.getTurnsSpent().last();
        currentInterval.addSkillCast(new Skill(skillName, amount,
                currentInterval.getEndTurn()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return line.contains("cast")
                && this.skillCastMatcher.reset(line).matches();
    }
}
