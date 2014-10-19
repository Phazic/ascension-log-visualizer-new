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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.parser.MafiaSessionLogReader.LogBlock;
import com.googlecode.logVisualizer.parser.MafiaSessionLogReader.LogBlockType;
import com.googlecode.logVisualizer.parser.lineParsers.DayChangeLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.EquipmentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.ItemAcquisitionLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser.MPGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaFamiliarChangeLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaPullLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser.MeatGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MeatSpentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.NotesLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.SkillCastLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.StatLineParser;
import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.ConsumableBlockParser;
import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.EncounterBlockParser;
import com.googlecode.logVisualizer.parser.mafiaLogBlockParsers.PlayerSnapshotBlockParser;

public final class MafiaLogParser implements LogParser {
    private static final Pattern THREE_FIGURE_STATGAIN = Pattern
            .compile("You gain \\d{3} [\\w\\s]+");
    private static final String NAUGHTY_SORCERESS_FIGHT_STRING = "Sorceress Tower: Naughty Sorceress";
    private final LogDataHolder logData = new LogDataHolder();
    private final File log;
    private final EncounterBlockParser encounterParser = new EncounterBlockParser();
    private final ConsumableBlockParser consumableParser = new ConsumableBlockParser();
    private final PlayerSnapshotBlockParser playerSnapshotParser = new PlayerSnapshotBlockParser();
    private final List<LineParser> lineParsers = new ArrayList<>();

    /**
     * @param log
     *            The mafia ascension log which is intended to be parsed to set.
     * @throws NullPointerException
     *             if log is {@code null}
     */
    public MafiaLogParser(final File log, final boolean isIncludeMafiaLogNotes) {
        this.log = log;
        // Set the log name
        this.getLogData().setLogName(
                log.getName().replace(".txt", UsefulPatterns.EMPTY_STRING));
        this.lineParsers.add(new ItemAcquisitionLineParser());
        this.lineParsers.add(new SkillCastLineParser());
        this.lineParsers.add(new MafiaFamiliarChangeLineParser());
        this.lineParsers.add(new MeatLineParser(MeatGainType.OTHER));
        this.lineParsers.add(new MeatSpentLineParser());
        this.lineParsers.add(new StatLineParser());
        this.lineParsers.add(new MPGainLineParser(MPGainType.NOT_ENCOUNTER));
        this.lineParsers.add(new EquipmentLineParser());
        this.lineParsers.add(new MafiaPullLineParser());
        this.lineParsers.add(new DayChangeLineParser());
        if (isIncludeMafiaLogNotes) {
            this.lineParsers.add(new NotesLineParser());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse() throws IOException {
        final MafiaSessionLogReader reader = new MafiaSessionLogReader(this.log);
        final boolean isOldAscensionCounting = Settings
                .getSettingBoolean("Using old ascension counting");
        boolean nsFightWon = false;
        while (reader.hasNext() && !nsFightWon) {
            final LogBlock block = reader.next();
            // In case old ascension turn counting is turned off and the current
            // block is an encounter block, we need to check whether the Naughty
            // Sorceress was beaten in it.
            if (!isOldAscensionCounting
                    && (block.getBlockType() == LogBlockType.ENCOUNTER_BLOCK)) {
                final String tmp = block.getBlockLines().get(0);
                if (tmp.endsWith(MafiaLogParser.NAUGHTY_SORCERESS_FIGHT_STRING)) {
                    nsFightWon = MafiaLogParser.isNaughtySorceressBeaten(block);
                }
            }
            // Now, we do the actual parsing.
            switch (block.getBlockType()) {
            case ENCOUNTER_BLOCK:
                this.encounterParser.parseBlock(block.getBlockLines(),
                        this.logData);
                break;
            case CONSUMABLE_BLOCK:
                this.consumableParser.parseBlock(block.getBlockLines(),
                        this.logData);
                break;
            case PLAYER_SNAPSHOT_BLOCK:
                this.playerSnapshotParser.parseBlock(block.getBlockLines(),
                        this.logData);
                break;
            case OTHER_BLOCK:
                for (final String line : block.getBlockLines()) {
                    for (final LineParser lp : this.lineParsers) {
                        // If the line parser can parse the line, this
                        // method also returns true. This is used to cut
                        // back on the amount of loops.
                        if (lp.parseLine(line, this.logData)) {
                            break;
                        }
                    }
                }
                break;
            default:
                break;
            }
        }
        reader.close();
        this.getLogData().createLogSummary();
    }

    /**
     * This method checks whether the Naughty Sorceress has been beaten.
     *
     * @param block
     *            The Naughty Sorceress encounter block.
     * @return True if the Naughty Sorceress was beaten, otherwise false.
     */
    private static boolean isNaughtySorceressBeaten(final LogBlock block) {
        for (final String line : block.getBlockLines()) {
            // Three figure stat gains aren't possible through combat items
            // while winning against the NS will give these amounts, so if there
            // is such a line, it means the fight has been won.
            if (MafiaLogParser.THREE_FIGURE_STATGAIN.matcher(line).matches()) {
                try (final Scanner scanner = new Scanner(line)) {
                    scanner.findInLine(UsefulPatterns.GAIN_LOSE_CAPTURE_PATTERN);
                    final String substatName = scanner.match().group(2);
                    scanner.close();
                    if (UsefulPatterns.MUSCLE_SUBSTAT_NAMES
                            .contains(substatName)
                            || UsefulPatterns.MYST_SUBSTAT_NAMES
                                    .contains(substatName)
                            || UsefulPatterns.MOXIE_SUBSTAT_NAMES
                                    .contains(substatName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogDataHolder getLogData() {
        return this.logData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDetailedLogData() {
        return true;
    }
}
