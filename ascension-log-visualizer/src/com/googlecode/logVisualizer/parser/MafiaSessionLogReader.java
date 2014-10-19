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
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class can read mafia session logs and return them to the caller in nice
 * and easier to handle chunks.
 */
public final class MafiaSessionLogReader {
    public static final Set<String> BROKEN_AREAS_ENCOUNTER_SET = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(
                    "Encounter: Big Wisniewski",
                    "Encounter: The Big Wisniewski", "Encounter: The Man",
                    "Encounter: Lord Spookyraven", "Encounter: Ed the Undying",
                    "Encounter: The Infiltrationist",
                    "Encounter: giant sandworm")));
    private static final String ENCOUNTER_START_STRING = "Encounter: ";
    private static final String FAMILIAR_POUND_GAIN_END_STRING = "gains a pound!";
    private static final String USE_STRING = "use";
    private static final String EAT_STRING = "eat";
    private static final String DRINK_STRING = "drink";
    private static final String BUY_STRING = "Buy";
    private static final String SNAPSHOT_START_END = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=";
    private static final String LEVEL_12_QUEST_BOSSFIGHT_BEGINNING_STRING = "bigisland.php?";
    private final BufferedReader log;
    private boolean hasNext = true;

    /**
     * @param log
     *            The condensed mafia session log that is supposed to be parsed.
     * @throws IOException
     *             if there were issues with accessing the log
     */
    MafiaSessionLogReader(final File log) throws IOException {
        if (!log.exists()) {
            throw new IllegalArgumentException("Log file must exist.");
        }
        if (log.isDirectory()) {
            throw new IllegalArgumentException(
                    "Log file has to be a file, not a directory.");
        }
        this.log = new BufferedReader(new FileReader(log));
    }

    /**
     * This method reads and returns the next block of text in the session log.
     * <p>
     * Currently, there are four possible versions of text blocks that can
     * recognised:
     * <li>Encounter blocks</li>
     * <li>Consumable blocks</li>
     * <li>Player snapshot blocks</li>
     * <li>Other blocks (for everything else that wouldn't fit in the above
     * categories)</li>
     *
     * @return The parsed out text block from the session log.
     * @throws IOException
     *             if there were issues with reading the log; in certain
     *             circumstances, if the was a line with more than 500
     *             characters
     * @throws IllegalStateException
     *             if there is no more block to parse in the session log
     */
    LogBlock next() throws IOException {
        final LogBlock block;
        this.log.mark(500);
        String line = this.log.readLine();
        String line2 = this.log.readLine();
        this.log.reset();
        if (line == null) {
            throw new IllegalStateException(
                    "There are no more blocks to be read.");
        }
        if (line2 == null) {
            line2 = UsefulPatterns.EMPTY_STRING;
        }
        if ((line.startsWith(UsefulPatterns.SQUARE_BRACKET_OPEN) && UsefulPatterns.TURNS_USED
                .matcher(line).matches())
                || (line2
                        .startsWith(MafiaSessionLogReader.ENCOUNTER_START_STRING) && MafiaSessionLogReader.BROKEN_AREAS_ENCOUNTER_SET
                        .contains(line2))) {
            block = new EncounterLogBlock(this.parseEncounterBlock());
        } else if ((line.startsWith(MafiaSessionLogReader.USE_STRING)
                || line.startsWith(MafiaSessionLogReader.EAT_STRING)
                || line.startsWith(MafiaSessionLogReader.DRINK_STRING) || line
                    .startsWith(MafiaSessionLogReader.BUY_STRING))
                && UsefulPatterns.CONSUMABLE_USED.matcher(line).matches()) {
            block = new ConsumableLogBlock(this.parseNormalBlock());
        } else if (line.equals(MafiaSessionLogReader.SNAPSHOT_START_END)) {
            block = new PlayerSnapshotLogBlock(this.parsePlayerSnapshotBlock());
        } else {
            block = new OtherLogBlock(this.parseNormalBlock());
        }
        // Skip empty lines and decide at the end whether the log is finished.
        do {
            this.log.mark(500);
        } while (((line = this.log.readLine()) != null) && (line.length() <= 0));
        if (line == null) {
            this.hasNext = false;
        } else {
            this.log.reset();
        }
        return block;
    }

    private List<String> parseEncounterBlock() throws IOException {
        final List<String> result = new ArrayList<>();
        String line;
        while ((line = this.log.readLine()) != null) {
            /**
             * Mafia saves a familiar pound gain this way in older versions:
             *
             * <pre>
             * Round _NUMBER_: _FAMNAME_ gains a pound!
             * 
             * familiar _FAMTYPE_ (_POUNDS_ lbs)
             *
             * </pre>
             *
             * This is problematic because empty lines will end the while loop
             * even though the combat rundown isn't over. Thus we attempt to
             * skip the above mentioned lines.
             */
            if (line.endsWith(MafiaSessionLogReader.FAMILIAR_POUND_GAIN_END_STRING)) {
                // Remember current position.
                this.log.mark(500);
                // Check next line, if it is empty, the problematic logging is
                // occurring, otherwise reset back to the original position.
                final String tmpLine = this.log.readLine();
                if (tmpLine.length() <= 0) {
                    this.log.readLine();
                    this.log.readLine();
                    line = this.log.readLine();
                    if (line == null) {
                        break;
                    }
                } else {
                    this.log.reset();
                }
            }
            // If there is an empty line, it means the encounter is over. There
            // are cases were this is not true for combats however, because
            // sometimes mafia puts empty lines in which aren't actually
            // supposed to be there. Such "false" empty lines should be
            // attempted to be recognised and skipped.
            if (line.length() <= 0) {
                // Remember current position.
                this.log.mark(600);
                // Look-ahead of three lines to try and see whether the combat
                // is actually continued.
                boolean isFightContinued = false;
                for (int i = 0; i < 3; i++) {
                    final String tmpLine = this.log.readLine();
                    // A square bracket means that a new turn was started. Extra
                    // check for the level 12 quest bossfight.
                    if ((tmpLine == null)
                            || tmpLine
                                    .startsWith(UsefulPatterns.SQUARE_BRACKET_OPEN)
                            || tmpLine
                                    .startsWith(MafiaSessionLogReader.LEVEL_12_QUEST_BOSSFIGHT_BEGINNING_STRING)) {
                        break;
                    } else if (tmpLine
                            .startsWith(UsefulPatterns.COMBAT_ROUND_LINE_BEGINNING_STRING)) {
                        isFightContinued = true;
                        line = tmpLine;
                        break;
                    }
                }
                // If the fight has ended, set the reader back to the original
                // position and stop the while loop.
                if (!isFightContinued) {
                    this.log.reset();
                    break;
                }
            }
            result.add(line);
        }
        if (line == null) {
            this.hasNext = false;
        }
        return result;
    }

    private List<String> parsePlayerSnapshotBlock() throws IOException {
        final List<String> result = new ArrayList<>();
        String line;
        // Add first three lines of the snapshot without check, so that the end
        // of the snapshot is not prematurely recognised.
        result.add(this.log.readLine());
        result.add(this.log.readLine());
        result.add(this.log.readLine());
        while (((line = this.log.readLine()) != null)
                && !line.equals(MafiaSessionLogReader.SNAPSHOT_START_END)) {
            result.add(line);
        }
        if (line == null) {
            this.hasNext = false;
        }
        return result;
    }

    private List<String> parseNormalBlock() throws IOException {
        final List<String> result = new ArrayList<>();
        String line;
        while (((line = this.log.readLine()) != null) && (line.length() > 0)) {
            result.add(line);
        }
        if (line == null) {
            this.hasNext = false;
        }
        return result;
    }

    /**
     * Use this method to check whether {@link #next()} is still able to return
     * another {@link LogBlock}.
     *
     * @return True if there are still blocks left to parse in the session log.
     */
    boolean hasNext() {
        return this.hasNext;
    }

    /**
     * Closes the {@link Reader} used to read the session log.
     */
    void close() {
        // Calling close() on a reader should not actually throw an exception,
        // so we'll just catch it in here.
        try {
            this.log.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * An enumeration of all the possible types that a {@link LogBlock} can
     * have.
     */
    static enum LogBlockType {
        ENCOUNTER_BLOCK, CONSUMABLE_BLOCK, PLAYER_SNAPSHOT_BLOCK, OTHER_BLOCK;
    }

    /**
     * Implementations of this interface are container classes to hold the block
     * of text that was parsed by a {@link MafiaSessionLogReader} and link it
     * with a certain version of {@link LogBlockType}.
     */
    static interface LogBlock {
        List<String> getBlockLines();

        LogBlockType getBlockType();
    }

    private static abstract class AbstractLogBlock implements LogBlock {
        private final List<String> blockLines;

        AbstractLogBlock(final List<String> blockLines) {
            this.blockLines = blockLines;
        }

        @Override
        public List<String> getBlockLines() {
            return Collections.unmodifiableList(this.blockLines);
        }
    }

    private static final class EncounterLogBlock extends AbstractLogBlock {
        EncounterLogBlock(final List<String> blockLines) {
            super(blockLines);
        }

        @Override
        public LogBlockType getBlockType() {
            return LogBlockType.ENCOUNTER_BLOCK;
        }
    }

    private static final class ConsumableLogBlock extends AbstractLogBlock {
        ConsumableLogBlock(final List<String> blockLines) {
            super(blockLines);
        }

        @Override
        public LogBlockType getBlockType() {
            return LogBlockType.CONSUMABLE_BLOCK;
        }
    }

    private static final class PlayerSnapshotLogBlock extends AbstractLogBlock {
        PlayerSnapshotLogBlock(final List<String> blockLines) {
            super(blockLines);
        }

        @Override
        public LogBlockType getBlockType() {
            return LogBlockType.PLAYER_SNAPSHOT_BLOCK;
        }
    }

    private static final class OtherLogBlock extends AbstractLogBlock {
        OtherLogBlock(final List<String> blockLines) {
            super(blockLines);
        }

        @Override
        public LogBlockType getBlockType() {
            return LogBlockType.OTHER_BLOCK;
        }
    }
}
