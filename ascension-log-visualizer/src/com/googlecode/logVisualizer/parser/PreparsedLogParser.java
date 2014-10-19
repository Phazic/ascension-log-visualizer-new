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
package com.googlecode.logVisualizer.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.logSummary.LogSummaryData;
import com.googlecode.logVisualizer.parser.blockParsers.BottleneckSummaryBlockParser;
import com.googlecode.logVisualizer.parser.blockParsers.FamiliarSummaryBlockParser;
import com.googlecode.logVisualizer.parser.blockParsers.LevelSummaryBlockParser;
import com.googlecode.logVisualizer.parser.blockParsers.MPSummaryBlockParser;
import com.googlecode.logVisualizer.parser.blockParsers.MeatSummaryBlockParser;
import com.googlecode.logVisualizer.parser.blockParsers.SemirareSummaryBlockParser;
import com.googlecode.logVisualizer.parser.blockParsers.SkillSummaryBlockParser;
import com.googlecode.logVisualizer.parser.blockParsers.StatsSummaryBlockParser;
import com.googlecode.logVisualizer.parser.lineParsers.AbstractLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.ConsumableLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.DayChangeLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.DroppedItemLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.FamiliarChangeLineParse;
import com.googlecode.logVisualizer.parser.lineParsers.FreeRunawaysLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.PullLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.TurnsSpentLineParser;
import com.googlecode.logVisualizer.util.DataNumberPair;

/**
 * A parser for preparsed ascension logs with a format equal or at least very
 * similar to the one which the AFH parser uses.
 * <p>
 * It tries to get as much information out of these ascension logs as possible,
 * although not all the possibilities the {@link LogDataHolder} class gives can
 * be filled out, due to the data simply not being present in these logs.
 * <p>
 * Note that this class is immutable.
 */
public final class PreparsedLogParser extends AbstractLogParser {
    private static final String ASCENDED_STRING = "Ascended!";
    private static final String TURN_RUNDOWN_FINISHED_STRING = "Turn rundown finished!";
    private final File log;
    private final List<DataNumberPair<String>> semirares = new ArrayList<>();
    private final List<DataNumberPair<String>> badMoonAdventures = new ArrayList<>();
    private final List<DataNumberPair<String>> huntedCombats = new ArrayList<>();
    private final List<DataNumberPair<String>> disintegratedCombats = new ArrayList<>();

    /**
     * @param log
     *            The preparsed ascension log which is intended to be parsed to
     *            set.
     * @throws NullPointerException
     *             if log is {@code null}
     */
    public PreparsedLogParser(final File log) {
        super(new LogDataHolder());
        this.log = log;
        // Set the log name
        if (log.getName().contains("_ascend")) {
            try (Scanner scanner = new Scanner(log.getName())) {
                scanner.useDelimiter("_ascend|(?:_\\d+_\\d+)?\\..+$");
                this.getLogData().setLogName(
                        scanner.next() + "-" + scanner.next());
                scanner.close();
            }
        } else {
            this.getLogData().setLogName(
                    log.getName().replace(".txt", UsefulPatterns.EMPTY_STRING));
        }
        // Add line parsers for parsing the turn rundown data
        this.addLineParser(new TurnsSpentLineParser());
        this.addLineParser(new DroppedItemLineParser());
        this.addLineParser(new ConsumableLineParser());
        this.addLineParser(new FamiliarChangeLineParse());
        this.addLineParser(new PullLineParser());
        this.addLineParser(new FreeRunawaysLineParser());
        this.addLineParser(new DayChangeLineParser());
        this.addSpecialLineParsers();
        // Add block parsers for parsing the summaries at the end of the log.
        this.addBlockParser(new LevelSummaryBlockParser());
        this.addBlockParser(new StatsSummaryBlockParser());
        this.addBlockParser(new FamiliarSummaryBlockParser());
        this.addBlockParser(new SemirareSummaryBlockParser());
        this.addBlockParser(new SkillSummaryBlockParser());
        this.addBlockParser(new MPSummaryBlockParser());
        this.addBlockParser(new MeatSummaryBlockParser());
        this.addBlockParser(new BottleneckSummaryBlockParser());
    }

    /**
     * While line parsers normally should be put into their own classes, some of
     * these have to have access to data structures inside this class and thus
     * are implemented as anonymous classes.
     * <p>
     * This has to be done, because of deficits or design decisions of the
     * {@link LogDataHolder} class. The most notable example for this is the
     * fact that the {@link LogSummaryData} can only be created after the turn
     * rundown data has been parsed and put into the {@link LogDataHolder}.
     * Thus, access to members of the {@link LogSummaryData} class is only
     * possible after the turn rundown part of preparsed ascension logs has been
     * parsed. But some of the data present in the turn rundown part of the
     * mentioned logs belongs inside {@link LogSummaryData}. Thus, this data has
     * to be saved in data structures inside this class temporarily, which in
     * turn can only be accessed be anonymous classes.
     * <p>
     * There might be other ways to accomplish this, but as long as the number
     * special classes is relatively low this will do.
     */
    private void addSpecialLineParsers() {
        // Semirare parser
        this.addLineParser(new AbstractLineParser() {
            private final Matcher semirareMatcher = UsefulPatterns.SEMIRARE
                    .matcher("");

            @Override
            protected void doParsing(final String line,
                    final LogDataHolder logData) {
                // Parse the turn number
                try (Scanner scanner = new Scanner(line)) {
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    final int turnNumber = scanner.nextInt();
                    scanner.close();
                    // Parse semirare name
                    try (Scanner scanner2 = new Scanner(line)) {
                        scanner2.useDelimiter(UsefulPatterns.ALL_BEFORE_COLON);
                        final String semirareName = scanner2.next();
                        scanner2.close();
                        // Add semirare
                        PreparsedLogParser.this.semirares.add(DataNumberPair
                                .of(semirareName, turnNumber));
                    }
                }
            }

            @Override
            protected boolean isCompatibleLine(final String line) {
                return this.semirareMatcher.reset(line).matches();
            }
        });
        // Bad Moon Adventure parser
        this.addLineParser(new AbstractLineParser() {
            private final Matcher badmoonMatcher = UsefulPatterns.BADMOON
                    .matcher("");

            @Override
            protected void doParsing(final String line,
                    final LogDataHolder logData) {
                // Parse the turn number
                try (Scanner scanner = new Scanner(line)) {
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    final int turnNumber = scanner.nextInt();
                    scanner.close();
                    // Parse adventure name
                    try (Scanner scanner2 = new Scanner(line)) {
                        scanner2.useDelimiter(UsefulPatterns.ALL_BEFORE_COLON);
                        final String badMoonAdventureName = scanner2.next();
                        scanner2.close();
                        // Add Bad Moon adventure
                        PreparsedLogParser.this.badMoonAdventures
                                .add(DataNumberPair.of(badMoonAdventureName,
                                        turnNumber));
                    }
                }
            }

            @Override
            protected boolean isCompatibleLine(final String line) {
                return this.badmoonMatcher.reset(line).matches();
            }
        });
        // Hunted combat parser
        this.addLineParser(new AbstractLineParser() {
            private final Matcher huntedCombatMatcher = UsefulPatterns.HUNTED_COMBAT
                    .matcher("");
            private final Pattern notCombatName = Pattern
                    .compile("^.*Started hunting\\s+");

            @Override
            protected void doParsing(final String line,
                    final LogDataHolder logData) {
                // Parse the turn number
                try (Scanner scanner = new Scanner(line)) {
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    final int turnNumber = scanner.nextInt();
                    scanner.close();
                    // Parse the combat name
                    try (Scanner scanner2 = new Scanner(line)) {
                        scanner2.useDelimiter(this.notCombatName);
                        if (scanner2.hasNext()) {
                            final String combatName = scanner2.next();
                            // Add hunted combat
                            PreparsedLogParser.this.huntedCombats
                                    .add(DataNumberPair.of(combatName,
                                            turnNumber));
                        }
                        scanner2.close();
                    }
                }
            }

            @Override
            protected boolean isCompatibleLine(final String line) {
                return this.huntedCombatMatcher.reset(line).matches();
            }
        });
        // Disintegrated combat parser
        this.addLineParser(new AbstractLineParser() {
            private final Matcher disintegrateMatcher = UsefulPatterns.DISINTEGRATED_COMBAT
                    .matcher(UsefulPatterns.EMPTY_STRING);
            private final Pattern notCombatName = Pattern
                    .compile("^.*Disintegrated\\s+");

            @Override
            protected void doParsing(final String line,
                    final LogDataHolder logData) {
                // Parse the turn number
                try (Scanner scanner = new Scanner(line)) {
                    scanner.useDelimiter(UsefulPatterns.NOT_A_NUMBER);
                    final int turnNumber = scanner.nextInt();
                    scanner.close();
                    // Parse the combat name
                    try (Scanner scanner2 = new Scanner(line)) {
                        scanner2.useDelimiter(this.notCombatName);
                        if (scanner2.hasNext()) {
                            final String combatName = scanner2.next();
                            // Add disintegrated combat
                            PreparsedLogParser.this.disintegratedCombats
                                    .add(DataNumberPair.of(combatName,
                                            turnNumber));
                        }
                        scanner2.close();
                    }
                }
            }

            @Override
            protected boolean isCompatibleLine(final String line) {
                return this.disintegrateMatcher.reset(line).matches();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse() throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(
                this.log))) {
            String line;
            // Parse the turn rundown part of the log.
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0) {
                    // Stop the loop if the turn rundown is finished.
                    if (line.startsWith(PreparsedLogParser.ASCENDED_STRING)
                            || line.startsWith(PreparsedLogParser.TURN_RUNDOWN_FINISHED_STRING)) {
                        break;
                    }
                    this.parseLine(line);
                }
            }
            // Create the log summary from the turn rundown data. Since these
            // preparsed logs don't hold enough data, some of the summaries will
            // stay empty.
            this.getLogData().createLogSummary();
            this.getLogData().getLogSummary().setSemirares(this.semirares);
            this.getLogData().getLogSummary()
                    .setBadmoonAdventures(this.badMoonAdventures);
            this.getLogData().getLogSummary()
                    .setHuntedCombats(this.huntedCombats);
            this.getLogData().getLogSummary()
                    .setDisintegratedCombats(this.disintegratedCombats);
            // Parse the summaries at the end of the log.
            while ((line = reader.readLine()) != null) {
                this.parseBlock(reader);
            }
            reader.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDetailedLogData() {
        return false;
    }
}
