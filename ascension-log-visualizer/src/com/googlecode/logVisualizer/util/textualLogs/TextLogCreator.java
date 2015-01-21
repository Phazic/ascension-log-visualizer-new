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
package com.googlecode.logVisualizer.util.textualLogs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.LogDataHolder.StatClass;
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.MeatGain;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.consumables.Consumable.ConsumableVersion;
import com.googlecode.logVisualizer.logData.logSummary.AreaStatgains;
import com.googlecode.logVisualizer.logData.logSummary.LevelData;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.SingleTurn.TurnVersion;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.TurnInterval.FreeRunaways;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.logData.turn.turnAction.Pull;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataCounter;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.DataTablesHandler;
import com.googlecode.logVisualizer.util.Pair;

/**
 * This utility class creates a parsed ascension log from a
 * {@link LogDataHolder}. The format of the parsed log is similar to the one
 * which the AFH parser uses.
 * <p>
 * Note that this class should only be used to create parsed ascension logs from
 * mafia logs. Using pre-parsed logs as the basis will not work, because those
 * do not contain enough data.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class TextLogCreator {
    private static final Map<String, String> TEXT_LOG_ADDITIONS_MAP = new HashMap<>();
    private static final Map<String, String> HTML_LOG_ADDITIONS_MAP = new HashMap<>();
    private static final Map<String, String> BBCODE_LOG_ADDITIONS_MAP = new HashMap<>();
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String COMMA = ", ";
    private static final String OPENING_TURN_BRACKET = " [";
    private static final String CLOSING_TURN_BRACKET = "] ";
    private static final String ITEM_PREFIX = "     +>";
    private static final String ITEM_MIDDLE_STRING = "Got ";
    private static final String CONSUMABLE_PREFIX = "     o> ";
    private static final String PULL_PREFIX = "     #> Turn";
    private static final String LEVEL_CHANGE_PREFIX = "     => Level ";
    private static final String HUNTED_COMBAT_PREFIX = "     *>";
    private static final String HUNTED_COMBAT_MIDDLE_STRING = "Started hunting ";
    private static final String DISINTEGRATED_COMBAT_PREFIX = "     }>";
    private static final String DISINTEGRATED_COMBAT_MIDDLE_STRING = "Disintegrated ";
    private static final String FAMILIAR_CHANGE_PREFIX = "     -> Turn";
    private static final String SEMIRARE_PREFIX = "     #>";
    private static final String SEMIRARE_MIDDLE_STRING = "Semirare: ";
    private static final String BAD_MOON_PREFIX = "     %>";
    private static final String BAD_MOON_MIDDLE_STRING = "Badmoon: ";
    private static final String FREE_RUNAWAYS_PREFIX = "     &> ";
    private static final String ADVENTURES_LEFT_STRING = "Adventure count at day start: ";
    private static final String CURRENT_MEAT_STRING = "Current meat: ";
    private static final DayChange NO_DAY_CHANGE = new DayChange(
            Integer.MAX_VALUE, Integer.MAX_VALUE);
    static {
        TextLogCreator.TEXT_LOG_ADDITIONS_MAP.put("notesStart", "[/code]");
        TextLogCreator.TEXT_LOG_ADDITIONS_MAP.put("notesEnd", "[code]");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("logHeaderStart", "<i>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("logHeaderEnd", "</i>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("turnStart", "<b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("turnEnd", "</b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("dayChangeLineStart", "<b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("dayChangeLineEnd", "</b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("statgainStart",
                "<font color=#808080>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("statgainEnd", "</font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("pullStart",
                "<font color=#008B8B>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("pullEnd", "</font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("consumableStart",
                "<font color=#009933><b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("consumableEnd",
                "</b></font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("itemStart",
                "<font color=#0000CD>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("itemEnd", "</font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("familiarStart",
                "<font color=#B03030>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("familiarEnd", "</font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("huntedStart",
                "<font color=#006400><b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("huntedEnd", "</b></font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("yellowRayStart",
                "<font color=#B8860B><b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP
                .put("yellowRayEnd", "</b></font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("specialEncounterStart",
                "<font color=#8B008B><b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("specialEncounterEnd",
                "</b></font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("levelStart",
                "<font color=#DC143C><b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("levelEnd", "</b></font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("runawayStart",
                "<font color=#CD853F><b>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("runawayEnd", "</b></font>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("notesStart", "<br>");
        TextLogCreator.HTML_LOG_ADDITIONS_MAP.put("notesEnd", "<br><br>");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("logHeaderStart", "[i]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("logHeaderEnd",
                "[/i][quote]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("turnRundownEnd",
                "[/quote]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("turnStart", "[b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("turnEnd", "[/b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP
                .put("dayChangeLineStart", "[b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("dayChangeLineEnd", "[/b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("statgainStart",
                "[color=#808080]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("statgainEnd", "[/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("pullStart",
                "[color=#008B8B]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("pullEnd", "[/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("consumableStart",
                "[color=#009933][b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("consumableEnd",
                "[/b][/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("itemStart",
                "[color=#0000CD]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("itemEnd", "[/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("familiarStart",
                "[color=#B03030]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("familiarEnd", "[/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("huntedStart",
                "[color=#006400][b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP
                .put("huntedEnd", "[/b][/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("yellowRayStart",
                "[color=#B8860B][b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("yellowRayEnd",
                "[/b][/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("specialEncounterStart",
                "[color=#8B008B][b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("specialEncounterEnd",
                "[/b][/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("levelStart",
                "[color=#DC143C][b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("levelEnd", "[/b][/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("runawayStart",
                "[color=#CD853F][b]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("runawayEnd",
                "[/b][/color]");
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("notesStart", "[/quote]"
                + TextLogCreator.NEW_LINE);
        TextLogCreator.BBCODE_LOG_ADDITIONS_MAP.put("notesEnd",
                TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE + "[quote]");
    }
    private final Map<String, String> logAdditionsMap;
    private final Set<String> localeOnetimeItemsSet = new HashSet<>(
            DataTablesHandler.getOnetimeItems());
    private final StringBuilder log;
    private final Iterator<FamiliarChange> familiarChangeIter;
    private FamiliarChange currentFamChange;
    private final Iterator<Pull> pullIter;
    private Pull currentPull;
    private final Iterator<LevelData> levelIter;
    private LevelData nextLevel;
    private final Iterator<DataNumberPair<String>> huntedCombatIter;
    private DataNumberPair<String> currentHuntedCombat;
    private final Iterator<DataNumberPair<String>> disintegratedCombatIter;
    private DataNumberPair<String> currentDisintegratedCombat;
    private boolean isShowNotes = true;

    /**
     * Creates a list of all turn interval print-outs as they are composed in a
     * turn rundown inside a textual ascension log.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @return The turn rundown list.
     */
    public static List<String> getTurnRundownList(final LogDataHolder logData) {
        final TextLogCreator logCreator = new TextLogCreator(logData,
                TextualLogVersion.TEXT_LOG);
        logCreator.isShowNotes = false;
        return logCreator.createTurnRundownList(logData);
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * returns it as a String.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     */
    public static String getTextualLog(final LogDataHolder logData,
            final TextualLogVersion logVersion) {
        // Sometimes, geek jokes are fun! ;)
        int logDate = 404;
        if (UsefulPatterns.USUAL_FORMAT_LOG_NAME.matcher(logData.getLogName())
                .matches()) {
            logDate = UsefulPatterns.getLogDate(logData.getLogName());
        }
        return TextLogCreator.getTextualLog(logData, logDate, logVersion);
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * returns it as a String.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     */
    public static String getTextualLog(final LogDataHolder logData,
            final int ascensionStartDate, final TextualLogVersion logVersion) {
        final TextLogCreator logCreator = new TextLogCreator(logData,
                logVersion);
        final String logOutput = logCreator.createTextLog(logData,
                ascensionStartDate);
        if (logVersion == TextualLogVersion.HTML_LOG) {
            return "<html><body>"
                    + logOutput.replace(TextLogCreator.NEW_LINE, "<br>"
                            + TextLogCreator.NEW_LINE) + "</body></html>";
        } else {
            return logOutput;
        }
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * saves it to the given file.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param saveDest
     *            The file in which the parsed ascension log should be saved in.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory
     */
    public static void saveTextualLogToFile(final LogDataHolder logData,
            final File saveDest, final TextualLogVersion logVersion)
            throws IOException {
        if (!saveDest.exists()) {
            throw new IllegalArgumentException("The file doesn't exist.");
        }
        if (saveDest.isDirectory()) {
            throw new IllegalArgumentException("The file is a directory.");
        }
        try (final PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(saveDest), 50000))) {
            writer.print(TextLogCreator.getTextualLog(logData, logVersion));
            writer.close();
        }
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * saves it to the given file.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     * @param saveDest
     *            The file in which the parsed ascension log should be saved in.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory
     */
    public static void saveTextualLogToFile(final LogDataHolder logData,
            final int ascensionStartDate, final File saveDest,
            final TextualLogVersion logVersion) throws IOException {
        if (!saveDest.exists()) {
            throw new IllegalArgumentException("The file doesn't exist.");
        }
        if (saveDest.isDirectory()) {
            throw new IllegalArgumentException("The file is a directory.");
        }
        try (final PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(saveDest), 50000))) {
            writer.print(TextLogCreator.getTextualLog(logData,
                    ascensionStartDate, logVersion));
            writer.close();
        }
    }

    /**
     * Sets up a TextLogCreator instance for further use.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param logVersion
     *            The wanted version of the textual log output.
     */
    private TextLogCreator(final LogDataHolder logData,
            final TextualLogVersion logVersion) {
        if (logData == null) {
            throw new NullPointerException(
                    "The LogDataHolder must not be null.");
        }
        switch (logVersion) {
        case HTML_LOG:
            this.logAdditionsMap = Collections
                    .unmodifiableMap(TextLogCreator.HTML_LOG_ADDITIONS_MAP);
            break;
        case BBCODE_LOG:
            this.logAdditionsMap = Collections
                    .unmodifiableMap(TextLogCreator.BBCODE_LOG_ADDITIONS_MAP);
            break;
        default:
            this.logAdditionsMap = Collections
                    .unmodifiableMap(TextLogCreator.TEXT_LOG_ADDITIONS_MAP);
        }
        // Most logs stay below 50000 characters.
        this.log = new StringBuilder(50000);
        this.familiarChangeIter = logData.getFamiliarChanges().iterator();
        this.pullIter = logData.getPulls().iterator();
        this.levelIter = logData.getLevels().iterator();
        this.huntedCombatIter = logData.getLogSummary().getHuntedCombats()
                .iterator();
        this.disintegratedCombatIter = logData.getLogSummary()
                .getDisintegratedCombats().iterator();
    }

    /**
     * Creates a parsed ascension log in a style similar to the format used by
     * the AFH parser.
     *
     * @param logData
     *            The LogDataHolder from which the ascension log should be
     *            created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     */
    private List<String> createTurnRundownList(final LogDataHolder logData) {
        final List<String> turnRundown = new ArrayList<>(logData
                .getTurnsSpent().size());
        this.currentFamChange = this.familiarChangeIter.hasNext() ? this.familiarChangeIter
                .next() : null;
        this.currentPull = this.pullIter.hasNext() ? this.pullIter.next()
                : null;
        this.currentHuntedCombat = this.huntedCombatIter.hasNext() ? this.huntedCombatIter
                .next() : null;
        this.currentDisintegratedCombat = this.disintegratedCombatIter
                .hasNext() ? this.disintegratedCombatIter.next() : null;
        // Level 1 can be skipped.
        this.levelIter.next();
        this.nextLevel = this.levelIter.hasNext() ? this.levelIter.next()
                : null;
        // Day 1 day change is handled differently and can be ignored here.
        final Iterator<DayChange> dayChangeIter = logData.getDayChanges()
                .iterator();
        DayChange nextDayChange = dayChangeIter.next();
        nextDayChange = dayChangeIter.hasNext() ? dayChangeIter.next()
                : TextLogCreator.NO_DAY_CHANGE;
        int currentDayNumber = 1;
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            if (!nextDayChange.equals(TextLogCreator.NO_DAY_CHANGE)
                    && (ti.getEndTurn() >= nextDayChange.getTurnNumber())) {
                if (ti.getEndTurn() == nextDayChange.getTurnNumber()) {
                    this.printTurnIntervalContents(ti, currentDayNumber);
                    final int currentStringLenght = this.log.length();
                    final Pair<Integer, DayChange> newDayChangeData = this
                            .printDayChanges(logData, ti.getEndTurn(),
                                    nextDayChange, dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();
                    this.log.delete(currentStringLenght, this.log.length());
                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    this.printCurrentConsumables(ti.getConsumablesUsed(),
                            currentDayNumber);
                    this.printCurrentPulls(currentDayNumber, ti.getEndTurn());
                } else if (ti.getStartTurn() < nextDayChange.getTurnNumber()) {
                    SingleTurn dayChangeTurn = null;
                    for (final SingleTurn st : ti.getTurns()) {
                        if (st.getTurnNumber() > nextDayChange.getTurnNumber()) {
                            dayChangeTurn = st;
                            break;
                        }
                    }
                    if (dayChangeTurn != null) {
                        final TurnInterval turnsBeforeDayChange = new TurnInterval(
                                ti.getTurns().headSet(dayChangeTurn),
                                dayChangeTurn.getAreaName());
                        final TurnInterval turnsAfterDayChange = new TurnInterval(
                                ti.getTurns().tailSet(dayChangeTurn),
                                dayChangeTurn.getAreaName());
                        this.printTurnIntervalContents(turnsBeforeDayChange,
                                currentDayNumber);
                        final int currentStringLenght = this.log.length();
                        final Pair<Integer, DayChange> newDayChangeData = this
                                .printDayChanges(logData, ti.getEndTurn(),
                                        nextDayChange, dayChangeIter);
                        currentDayNumber = newDayChangeData.getVar1();
                        nextDayChange = newDayChangeData.getVar2();
                        this.log.delete(currentStringLenght, this.log.length());
                        // Consumables usage or pulls that happened nominally on
                        // the
                        // last turn before the day change, but were actually
                        // done
                        // on the next day.
                        this.printCurrentConsumables(
                                turnsBeforeDayChange.getConsumablesUsed(),
                                currentDayNumber);
                        this.printCurrentPulls(currentDayNumber,
                                turnsBeforeDayChange.getEndTurn());
                        this.log.append(TextLogCreator.NEW_LINE);
                        this.printTurnIntervalContents(turnsAfterDayChange,
                                currentDayNumber);
                    }
                } else {
                    final int currentStringLenght = this.log.length();
                    final Pair<Integer, DayChange> newDayChangeData = this
                            .printDayChanges(logData, ti.getEndTurn(),
                                    nextDayChange, dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();
                    this.log.delete(currentStringLenght, this.log.length());
                    this.printTurnIntervalContents(ti, currentDayNumber);
                }
            } else {
                this.printTurnIntervalContents(ti, currentDayNumber);
            }
            turnRundown.add(this.log.toString());
            this.log.delete(0, this.log.length());
        }
        return turnRundown;
    }

    /**
     * Creates a parsed ascension log in a style similar to the format used by
     * the AFH parser.
     *
     * @param logData
     *            The LogDataHolder from which the ascension log should be
     *            created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     */
    private String createTextLog(final LogDataHolder logData,
            final int ascensionStartDate) {
        this.currentFamChange = this.familiarChangeIter.hasNext() ? this.familiarChangeIter
                .next() : null;
        this.currentPull = this.pullIter.hasNext() ? this.pullIter.next()
                : null;
        this.currentHuntedCombat = this.huntedCombatIter.hasNext() ? this.huntedCombatIter
                .next() : null;
        this.currentDisintegratedCombat = this.disintegratedCombatIter
                .hasNext() ? this.disintegratedCombatIter.next() : null;
        // Level 1 can be skipped.
        this.levelIter.next();
        this.nextLevel = this.levelIter.hasNext() ? this.levelIter.next()
                : null;
        // Day 1 day change is handled differently and can be ignored here.
        final Iterator<DayChange> dayChangeIter = logData.getDayChanges()
                .iterator();
        DayChange nextDayChange = dayChangeIter.next();
        nextDayChange = dayChangeIter.hasNext() ? dayChangeIter.next()
                : TextLogCreator.NO_DAY_CHANGE;
        int currentDayNumber = 1;
        // Add the log file header.
        this.write("NEW " + logData.getCharacterClass() + " ASCENSION STARTED "
                + ascensionStartDate + TextLogCreator.NEW_LINE);
        this.write("------------------------------" + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        this.write(this.logAdditionsMap.get("logHeaderStart"));
        this.write("This log was created by the Ascension Log Visualizer "
                + Settings.getSettingString("Version") + "."
                + TextLogCreator.NEW_LINE);
        this.write("The basic idea and the format of this parser have been burrowed from the AFH MafiaLog Parser by VladimirPootin and QuantumNightmare."
                + TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE);
        this.write(this.logAdditionsMap.get("logHeaderEnd"));
        this.write(this.logAdditionsMap.get("dayChangeLineStart"));
        this.write("===Day 1===");
        this.write(this.logAdditionsMap.get("dayChangeLineEnd"));
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE);
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            if (!nextDayChange.equals(TextLogCreator.NO_DAY_CHANGE)
                    && (ti.getEndTurn() >= nextDayChange.getTurnNumber())) {
                if (ti.getEndTurn() == nextDayChange.getTurnNumber()) {
                    this.printTurnIntervalContents(ti, currentDayNumber);
                    final Pair<Integer, DayChange> newDayChangeData = this
                            .printDayChanges(logData, ti.getEndTurn(),
                                    nextDayChange, dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();
                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    this.printCurrentConsumables(ti.getConsumablesUsed(),
                            currentDayNumber);
                    this.printCurrentPulls(currentDayNumber, ti.getEndTurn());
                } else if (ti.getStartTurn() < nextDayChange.getTurnNumber()) {
                    SingleTurn dayChangeTurn = null;
                    for (final SingleTurn st : ti.getTurns()) {
                        if (st.getTurnNumber() > nextDayChange.getTurnNumber()) {
                            dayChangeTurn = st;
                            break;
                        }
                    }
                    if (dayChangeTurn != null) {
                        final TurnInterval turnsBeforeDayChange = new TurnInterval(
                                ti.getTurns().headSet(dayChangeTurn),
                                dayChangeTurn.getAreaName());
                        final TurnInterval turnsAfterDayChange = new TurnInterval(
                                ti.getTurns().tailSet(dayChangeTurn),
                                dayChangeTurn.getAreaName());
                        turnsAfterDayChange.incrementSuccessfulFreeRunaways(ti
                                .getFreeRunaways()
                                .getNumberOfSuccessfulRunaways());
                        this.printTurnIntervalContents(turnsBeforeDayChange,
                                currentDayNumber);
                        final Pair<Integer, DayChange> newDayChangeData = this
                                .printDayChanges(logData, ti.getEndTurn(),
                                        nextDayChange, dayChangeIter);
                        currentDayNumber = newDayChangeData.getVar1();
                        nextDayChange = newDayChangeData.getVar2();
                        // Consumables usage or pulls that happened nominally on
                        // the
                        // last turn before the day change, but were actually
                        // done
                        // on the next day.
                        this.printCurrentConsumables(
                                turnsBeforeDayChange.getConsumablesUsed(),
                                currentDayNumber);
                        this.printCurrentPulls(currentDayNumber,
                                turnsBeforeDayChange.getEndTurn());
                        this.printTurnIntervalContents(turnsAfterDayChange,
                                currentDayNumber);
                        // Print the notes from the actual interval.
                        this.printNotes(ti);
                    }
                } else {
                    final Pair<Integer, DayChange> newDayChangeData = this
                            .printDayChanges(logData, ti.getEndTurn(),
                                    nextDayChange, dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();
                    this.printTurnIntervalContents(ti, currentDayNumber);
                }
            } else {
                this.printTurnIntervalContents(ti, currentDayNumber);
            }
        }
        this.write(TextLogCreator.NEW_LINE + "Turn rundown finished!");
        this.write(this.logAdditionsMap.get("turnRundownEnd"));
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE);
        this.printLogSummaries(logData);
        return this.log.toString();
    }

    /**
     * Prints all day changes that occurred and returns the new current day
     * number and the next day change. If no day change occurred, the old values
     * will be returned.
     */
    private Pair<Integer, DayChange> printDayChanges(
            final LogDataHolder logData, final int currentTurnNumber,
            DayChange nextDayChange, final Iterator<DayChange> dayChangeIter) {
        int currentDayNumber = nextDayChange.getDayNumber() - 1;
        while (!nextDayChange.equals(TextLogCreator.NO_DAY_CHANGE)
                && (currentTurnNumber >= nextDayChange.getTurnNumber())) {
            final PlayerSnapshot currentSnapshot = logData
                    .getFirstPlayerSnapshotAfterTurn(nextDayChange
                            .getTurnNumber());
            this.write(TextLogCreator.NEW_LINE);
            this.write(this.logAdditionsMap.get("dayChangeLineStart"));
            this.write(nextDayChange.toString());
            this.write(this.logAdditionsMap.get("dayChangeLineEnd"));
            if (currentSnapshot != null) {
                this.write(TextLogCreator.NEW_LINE);
                this.write(TextLogCreator.ADVENTURES_LEFT_STRING);
                this.write(currentSnapshot.getAdventuresLeft());
                this.write(TextLogCreator.NEW_LINE);
                this.write(TextLogCreator.CURRENT_MEAT_STRING);
                this.write(currentSnapshot.getCurrentMeat());
            }
            this.write(TextLogCreator.NEW_LINE);
            this.write(TextLogCreator.NEW_LINE);
            currentDayNumber = nextDayChange.getDayNumber();
            nextDayChange = dayChangeIter.hasNext() ? dayChangeIter.next()
                    : TextLogCreator.NO_DAY_CHANGE;
        }
        return Pair.of(currentDayNumber, nextDayChange);
    }

    /**
     * Prints all pulls from the given day up to the given turn number.
     */
    private void printCurrentPulls(final int currentDayNumber,
            final int currentTurnNumber) {
        while ((this.currentPull != null)
                && (currentTurnNumber >= this.currentPull.getTurnNumber())) {
            // Only pulls of the current day should be added here.
            if (this.currentPull.getDayNumber() > currentDayNumber) {
                break;
            }
            this.write(TextLogCreator.PULL_PREFIX);
            this.write(TextLogCreator.OPENING_TURN_BRACKET);
            this.write(this.currentPull.getTurnNumber());
            this.write(TextLogCreator.CLOSING_TURN_BRACKET);
            this.write("pulled");
            this.write(UsefulPatterns.WHITE_SPACE);
            this.write(this.logAdditionsMap.get("pullStart"));
            this.write(this.currentPull.getAmount());
            this.write(UsefulPatterns.WHITE_SPACE);
            this.write(this.currentPull.getItemName());
            this.write(this.logAdditionsMap.get("pullEnd"));
            this.write(TextLogCreator.NEW_LINE);
            this.currentPull = this.pullIter.hasNext() ? this.pullIter.next()
                    : null;
        }
    }

    /**
     * Prints all consumables from the given day.
     */
    private void printCurrentConsumables(
            final Collection<Consumable> consumables, final int currentDayNumber) {
        for (final Consumable c : consumables) {
            if (c.getDayNumberOfUsage() == currentDayNumber) {
                if ((c.getAdventureGain() > 0)
                        || !c.getStatGain().isAllStatsZero()) {
                    this.write(TextLogCreator.CONSUMABLE_PREFIX);
                    if (c.getConsumableVersion() == ConsumableVersion.FOOD) {
                        this.write("Ate ");
                    } else if (c.getConsumableVersion() == ConsumableVersion.BOOZE) {
                        this.write("Drank ");
                    } else {
                        this.write("Used ");
                    }
                    this.write(this.logAdditionsMap.get("consumableStart"));
                    this.write(c.getAmount());
                    this.write(UsefulPatterns.WHITE_SPACE);
                    this.write(c.getName());
                    this.write(this.logAdditionsMap.get("consumableEnd"));
                    if ((c.getAdventureGain() > 0)
                            || (c.getConsumableVersion() == ConsumableVersion.FOOD)
                            || (c.getConsumableVersion() == ConsumableVersion.BOOZE)) {
                        this.write(UsefulPatterns.WHITE_SPACE);
                        this.write(UsefulPatterns.ROUND_BRACKET_OPEN);
                        this.write(c.getAdventureGain());
                        this.write(UsefulPatterns.WHITE_SPACE);
                        this.write("adventures gained");
                        this.write(UsefulPatterns.ROUND_BRACKET_CLOSE);
                    }
                    this.write(UsefulPatterns.WHITE_SPACE);
                    this.write(this.logAdditionsMap.get("statgainStart"));
                    this.write(c.getStatGain().toString());
                    this.write(this.logAdditionsMap.get("statgainEnd"));
                    this.write(TextLogCreator.NEW_LINE);
                }
            }
        }
    }

    /**
     * Prints the notes contained inside the given turn interval. If the
     * interval contains no notes, this method won't print anything.
     */
    private void printNotes(final TurnInterval ti) {
        if (ti.getNotes().length() > 0) {
            this.write(this.logAdditionsMap.get("notesStart"));
            this.write(ti.getNotes().replaceAll("[\r\n]|\r\n",
                    TextLogCreator.NEW_LINE));
            this.write(this.logAdditionsMap.get("notesEnd"));
            this.write(TextLogCreator.NEW_LINE);
        }
    }

    private void printItemAcquisitionStartString(final int turnNumber) {
        this.write(TextLogCreator.ITEM_PREFIX);
        this.write(TextLogCreator.OPENING_TURN_BRACKET);
        this.write(turnNumber);
        this.write(TextLogCreator.CLOSING_TURN_BRACKET);
        this.write(TextLogCreator.ITEM_MIDDLE_STRING);
    }

    /**
     * @param ti
     *            The turn interval whose contents should be printed.
     */
    private void printTurnIntervalContents(final TurnInterval ti,
            final int currentDayNumber) {
        this.write(this.logAdditionsMap.get("turnStart"));
        this.write(UsefulPatterns.SQUARE_BRACKET_OPEN);
        if (ti.getTotalTurns() > 1) {
            this.write(ti.getStartTurn() + 1);
            this.write(UsefulPatterns.MINUS);
        }
        this.write(ti.getEndTurn());
        this.write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        this.write(this.logAdditionsMap.get("turnEnd"));
        this.write(UsefulPatterns.WHITE_SPACE);
        this.write(ti.getAreaName());
        this.write(UsefulPatterns.WHITE_SPACE);
        this.write(this.logAdditionsMap.get("statgainStart"));
        this.write(ti.getStatGain().toString());
        this.write(this.logAdditionsMap.get("statgainEnd"));
        this.write(TextLogCreator.NEW_LINE);
        for (final SingleTurn st : ti.getTurns()) {
            if (DataTablesHandler.isSemirareEncounter(st)) {
                this.write(TextLogCreator.SEMIRARE_PREFIX);
                this.write(TextLogCreator.OPENING_TURN_BRACKET);
                this.write(st.getTurnNumber());
                this.write(TextLogCreator.CLOSING_TURN_BRACKET);
                this.write(TextLogCreator.SEMIRARE_MIDDLE_STRING);
                this.write(this.logAdditionsMap.get("specialEncounterStart"));
                this.write(st.getEncounterName());
                this.write(this.logAdditionsMap.get("specialEncounterEnd"));
                this.write(TextLogCreator.NEW_LINE);
            }
            if (DataTablesHandler.isBadMoonEncounter(st)) {
                this.write(TextLogCreator.BAD_MOON_PREFIX);
                this.write(TextLogCreator.OPENING_TURN_BRACKET);
                this.write(st.getTurnNumber());
                this.write(TextLogCreator.CLOSING_TURN_BRACKET);
                this.write(TextLogCreator.BAD_MOON_MIDDLE_STRING);
                this.write(this.logAdditionsMap.get("specialEncounterStart"));
                this.write(st.getEncounterName());
                this.write(this.logAdditionsMap.get("specialEncounterEnd"));
                this.write(TextLogCreator.NEW_LINE);
            }
            final List<Item> importantItems = new ArrayList<>();
            for (final Item i : st.getDroppedItems()) {
                final String itemName = i.getName().toLowerCase(Locale.ENGLISH);
                if (DataTablesHandler.isImportantItem(itemName)) {
                    importantItems.add(i);
                }
                if (this.localeOnetimeItemsSet.contains(itemName)) {
                    importantItems.add(i);
                    this.localeOnetimeItemsSet.remove(itemName);
                }
            }
            final Iterator<Item> aquiredItemsIter = importantItems.iterator();
            if (aquiredItemsIter.hasNext()) {
                this.printItemAcquisitionStartString(st.getTurnNumber());
                int itemCounter = 0;
                while (aquiredItemsIter.hasNext()) {
                    final Item currentItem = aquiredItemsIter.next();
                    for (int i = currentItem.getAmount(); i > 0; i--) {
                        this.write(this.logAdditionsMap.get("itemStart"));
                        this.write(currentItem.getName());
                        this.write(this.logAdditionsMap.get("itemEnd"));
                        itemCounter++;
                        if ((aquiredItemsIter.hasNext() || (i > 1))
                                && (itemCounter >= 4)) {
                            this.write(TextLogCreator.NEW_LINE);
                            this.printItemAcquisitionStartString(st
                                    .getTurnNumber());
                            itemCounter = 0;
                        } else if (i > 1) {
                            this.write(TextLogCreator.COMMA);
                        }
                    }
                    if (aquiredItemsIter.hasNext() && (itemCounter != 0)) {
                        this.write(TextLogCreator.COMMA);
                    }
                }
                this.write(TextLogCreator.NEW_LINE);
            }
        }
        this.printCurrentConsumables(ti.getConsumablesUsed(), currentDayNumber);
        this.printCurrentPulls(currentDayNumber, ti.getEndTurn());
        while ((this.currentHuntedCombat != null)
                && (ti.getEndTurn() >= this.currentHuntedCombat.getNumber())) {
            this.write(TextLogCreator.HUNTED_COMBAT_PREFIX);
            this.write(TextLogCreator.OPENING_TURN_BRACKET);
            this.write(this.currentHuntedCombat.getNumber());
            this.write(TextLogCreator.CLOSING_TURN_BRACKET);
            this.write(TextLogCreator.HUNTED_COMBAT_MIDDLE_STRING);
            this.write(this.logAdditionsMap.get("huntedStart"));
            this.write(this.currentHuntedCombat.getData());
            this.write(this.logAdditionsMap.get("huntedEnd"));
            this.write(TextLogCreator.NEW_LINE);
            this.currentHuntedCombat = this.huntedCombatIter.hasNext() ? this.huntedCombatIter
                    .next() : null;
        }
        while ((this.currentDisintegratedCombat != null)
                && (ti.getEndTurn() >= this.currentDisintegratedCombat
                        .getNumber())) {
            this.write(TextLogCreator.DISINTEGRATED_COMBAT_PREFIX);
            this.write(TextLogCreator.OPENING_TURN_BRACKET);
            this.write(this.currentDisintegratedCombat.getNumber());
            this.write(TextLogCreator.CLOSING_TURN_BRACKET);
            this.write(TextLogCreator.DISINTEGRATED_COMBAT_MIDDLE_STRING);
            this.write(this.logAdditionsMap.get("yellowRayStart"));
            this.write(this.currentDisintegratedCombat.getData());
            this.write(this.logAdditionsMap.get("yellowRayEnd"));
            this.write(TextLogCreator.NEW_LINE);
            this.currentDisintegratedCombat = this.disintegratedCombatIter
                    .hasNext() ? this.disintegratedCombatIter.next() : null;
        }
        while ((this.currentFamChange != null)
                && (ti.getEndTurn() >= this.currentFamChange.getTurnNumber())) {
            this.write(TextLogCreator.FAMILIAR_CHANGE_PREFIX);
            this.write(TextLogCreator.OPENING_TURN_BRACKET);
            this.write(this.currentFamChange.getTurnNumber());
            this.write(TextLogCreator.CLOSING_TURN_BRACKET);
            this.write(this.logAdditionsMap.get("familiarStart"));
            this.write(this.currentFamChange.getFamiliarName());
            this.write(this.logAdditionsMap.get("familiarEnd"));
            this.write(TextLogCreator.NEW_LINE);
            this.currentFamChange = this.familiarChangeIter.hasNext() ? this.familiarChangeIter
                    .next() : null;
        }
        final FreeRunaways freeRunaways = ti.getFreeRunaways();
        if (freeRunaways.getNumberOfAttemptedRunaways() > 0) {
            this.write(this.logAdditionsMap.get("runawayStart"));
            this.write(TextLogCreator.FREE_RUNAWAYS_PREFIX);
            this.write(freeRunaways.getNumberOfSuccessfulRunaways());
            this.write(UsefulPatterns.WHITE_SPACE);
            this.write("/");
            this.write(UsefulPatterns.WHITE_SPACE);
            this.write(freeRunaways.getNumberOfAttemptedRunaways());
            this.write(UsefulPatterns.WHITE_SPACE);
            this.write("free retreats");
            this.write(this.logAdditionsMap.get("runawayEnd"));
            this.write(TextLogCreator.NEW_LINE);
        }
        while ((this.nextLevel != null)
                && (ti.getEndTurn() >= this.nextLevel.getLevelReachedOnTurn())) {
            final int musStat = (int) Math.sqrt(this.nextLevel
                    .getStatsAtLevelReached().mus);
            final int mystStat = (int) Math.sqrt(this.nextLevel
                    .getStatsAtLevelReached().myst);
            final int moxStat = (int) Math.sqrt(this.nextLevel
                    .getStatsAtLevelReached().mox);
            this.write(this.logAdditionsMap.get("levelStart"));
            this.write(TextLogCreator.LEVEL_CHANGE_PREFIX);
            this.write(this.nextLevel.getLevelNumber());
            this.write(" (Turn ");
            this.write(this.nextLevel.getLevelReachedOnTurn());
            this.write(")! (");
            this.write(musStat);
            this.write("/");
            this.write(mystStat);
            this.write("/");
            this.write(moxStat);
            this.write(UsefulPatterns.ROUND_BRACKET_CLOSE);
            this.write(this.logAdditionsMap.get("levelEnd"));
            this.write(TextLogCreator.NEW_LINE);
            this.nextLevel = this.levelIter.hasNext() ? this.levelIter.next()
                    : null;
        }
        if (this.isShowNotes) {
            this.printNotes(ti);
        }
    }

    private void printLogSummaries(final LogDataHolder logData) {
        // Turns spent per area summary
        this.write("ADVENTURES" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary()
                .getTurnsPerArea()) {
            this.write(dn.getData());
            this.write(": ");
            this.write(dn.getNumber());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Quest Turns summary
        this.write("QUEST TURNS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        this.write("Mosquito Larva: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getMosquitoQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Opening the Hidden Temple: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getTempleOpeningTurns() + TextLogCreator.NEW_LINE);
        this.write("Tavern quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getTavernQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Bat quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getBatQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Cobb's Knob quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getKnobQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Friars' part 1: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getFriarsQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Defiled Cyrpt quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getCyrptQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Trapzor quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getTrapzorQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Orc Chasm quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getChasmQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Airship: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getAirshipQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Giant's Castle: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getCastleQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Opening the Ballroom: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getBallroomOpeningTurns() + TextLogCreator.NEW_LINE);
        this.write("Pirate quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getPirateQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Black Forest quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getBlackForrestQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Desert Oasis quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getDesertOasisQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Spookyraven quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getSpookyravenQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Hidden City quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getTempleCityQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Palindome quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getPalindomeQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Pyramid quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getPyramidQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("Starting the War: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getWarIslandOpeningTurns() + TextLogCreator.NEW_LINE);
        this.write("War Island quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getWarIslandQuestTurns() + TextLogCreator.NEW_LINE);
        this.write("DoD quest: "
                + logData.getLogSummary().getQuestTurncounts()
                        .getDodQuestTurns() + TextLogCreator.NEW_LINE);
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Pulls summary
        this.write("PULLS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        final DataCounter<String> pullsCounter = new DataCounter<>(
                (int) (logData.getPulls().size() * 1.4) + 1);
        for (final Pull p : logData.getPulls()) {
            pullsCounter.addDataElement(p.getItemName(), p.getAmount());
        }
        final List<DataNumberPair<String>> pulls = pullsCounter
                .getCountedData();
        // ordered from highest to lowest amount
        Collections.sort(pulls, new Comparator<DataNumberPair<String>>() {
            @Override
            public int compare(final DataNumberPair<String> o1,
                    final DataNumberPair<String> o2) {
                return o2.compareTo(o1);
            }
        });
        for (final DataNumberPair<String> dn : pulls) {
            this.write("Pulled ");
            this.write(dn.getNumber());
            this.write(" ");
            this.write(dn.getData());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Level summary
        this.write("LEVELS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        final NumberFormat formatter = NumberFormat
                .getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
        LevelData lastLevel = null;
        for (final LevelData ld : logData.getLevels()) {
            final int turnDifference = lastLevel != null ? ld
                    .getLevelReachedOnTurn()
                    - lastLevel.getLevelReachedOnTurn() : 0;
            final double statsPerTurn = lastLevel != null ? lastLevel
                    .getStatGainPerTurn() : 0;
            final int combatTurns = lastLevel != null ? lastLevel
                    .getCombatTurns() : 0;
            final int noncombatTurns = lastLevel != null ? lastLevel
                    .getNoncombatTurns() : 0;
            final int otherTurns = lastLevel != null ? lastLevel
                    .getOtherTurns() : 0;
            this.write(ld.toString());
            this.write(TextLogCreator.COMMA);
            this.write(turnDifference);
            this.write(" from last level. (");
            this.write(formatter.format(statsPerTurn));
            this.write(" substats / turn)");
            this.write(TextLogCreator.NEW_LINE);
            this.write("   Combats: ");
            this.write(combatTurns);
            this.write(TextLogCreator.NEW_LINE);
            this.write("   Noncombats: ");
            this.write(noncombatTurns);
            this.write(TextLogCreator.NEW_LINE);
            this.write("   Other: ");
            this.write(otherTurns);
            this.write(TextLogCreator.NEW_LINE);
            lastLevel = ld;
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE);
        final int totalTurns = logData.getTurnsSpent().last().getEndTurn();
        this.write("Total COMBATS: "
                + logData.getLogSummary().getTotalTurnsCombat()
                + " ("
                + (Math.round((logData.getLogSummary().getTotalTurnsCombat() * 1000.0)
                        / totalTurns) / 10.0) + "%)" + TextLogCreator.NEW_LINE);
        this.write("Total NONCOMBATS: "
                + logData.getLogSummary().getTotalTurnsNoncombat()
                + " ("
                + (Math.round((logData.getLogSummary().getTotalTurnsNoncombat() * 1000.0)
                        / totalTurns) / 10.0) + "%)" + TextLogCreator.NEW_LINE);
        this.write("Total OTHER: "
                + logData.getLogSummary().getTotalTurnsOther()
                + " ("
                + (Math.round((logData.getLogSummary().getTotalTurnsOther() * 1000.0)
                        / totalTurns) / 10.0) + "%)" + TextLogCreator.NEW_LINE);
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Stats summary
        this.write("STATS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        final Statgain totalStats = logData.getLogSummary().getTotalStatgains();
        final Statgain combatStats = logData.getLogSummary()
                .getCombatsStatgains();
        final Statgain noncombatStats = logData.getLogSummary()
                .getNoncombatsStatgains();
        final Statgain otherStats = logData.getLogSummary()
                .getOthersStatgains();
        final Statgain foodStats = logData.getLogSummary()
                .getFoodConsumablesStatgains();
        final Statgain boozeStats = logData.getLogSummary()
                .getBoozeConsumablesStatgains();
        final Statgain usingStats = logData.getLogSummary()
                .getUsedConsumablesStatgains();
        this.write("           \tMuscle\tMyst\tMoxie" + TextLogCreator.NEW_LINE);
        this.write("Totals:   \t" + totalStats.mus + "\t" + totalStats.myst
                + "\t" + totalStats.mox + TextLogCreator.NEW_LINE);
        this.write("Combats:\t" + combatStats.mus + "\t" + combatStats.myst
                + "\t" + combatStats.mox + TextLogCreator.NEW_LINE);
        this.write("Noncombats:\t" + noncombatStats.mus + "\t"
                + noncombatStats.myst + "\t" + noncombatStats.mox
                + TextLogCreator.NEW_LINE);
        this.write("Others:   \t" + otherStats.mus + "\t" + otherStats.myst
                + "\t" + otherStats.mox + TextLogCreator.NEW_LINE);
        this.write("Eating:   \t" + foodStats.mus + "\t" + foodStats.myst
                + "\t" + foodStats.mox + TextLogCreator.NEW_LINE);
        this.write("Drinking:\t" + boozeStats.mus + "\t" + boozeStats.myst
                + "\t" + boozeStats.mox + TextLogCreator.NEW_LINE);
        this.write("Using:   \t" + usingStats.mus + "\t" + usingStats.myst
                + "\t" + usingStats.mox + TextLogCreator.NEW_LINE);
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE);
        final List<AreaStatgains> areas = new ArrayList<>(logData
                .getLogSummary().getAreasStatgains());
        Collections.sort(areas, new Comparator<AreaStatgains>() {
            @Override
            public int compare(final AreaStatgains o1, final AreaStatgains o2) {
                if (logData.getCharacterClass().getStatClass() == StatClass.MUSCLE) {
                    return o2.getStatgain().mus - o1.getStatgain().mus;
                } else if (logData.getCharacterClass().getStatClass() == StatClass.MYSTICALITY) {
                    return o2.getStatgain().myst - o1.getStatgain().myst;
                } else {
                    return o2.getStatgain().mox - o1.getStatgain().mox;
                }
            }
        });
        this.write("Top 10 mainstat gaining areas:" + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        for (int i = 0; (i < areas.size()) && (i < 10); i++) {
            this.write(areas.get(i).toString());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // +Stat Breakdown summary
        final List<StatgiverItem> statGivers = new ArrayList<>(20);
        for (final Pair<String, Double> p : DataTablesHandler.getStatsItems()) {
            statGivers.add(new StatgiverItem(p.getVar1(), p.getVar2()));
        }
        final StatgiverItem serpentineSword = new StatgiverItem(
                "serpentine sword", 1.25);
        final StatgiverItem snakeShield = new StatgiverItem("snake shield",
                1.25);
        final Iterator<LevelData> lvlIndex = logData.getLevels().iterator();
        LevelData nextLvl = lvlIndex.hasNext() ? lvlIndex.next() : null;
        int currentLvlNumber = 1;
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            for (final SingleTurn st : ti.getTurns()) {
                while ((nextLvl != null)
                        && (nextLvl.getLevelReachedOnTurn() < st
                                .getTurnNumber())) {
                    currentLvlNumber = nextLvl.getLevelNumber();
                    nextLvl = lvlIndex.hasNext() ? lvlIndex.next() : null;
                }
                if (currentLvlNumber >= 13) {
                    break;
                }
                if (st.getTurnVersion() == TurnVersion.COMBAT) {
                    for (final StatgiverItem sgi : statGivers) {
                        sgi.incrementLvlStatgain(
                                currentLvlNumber,
                                st.getUsedEquipment().getNumberOfEquips(
                                        sgi.getItemName()));
                    }
                    // Special cases
                    final int serpentineSwordEquips = st.getUsedEquipment()
                            .getNumberOfEquips(serpentineSword.getItemName());
                    serpentineSword.incrementLvlStatgain(currentLvlNumber,
                            serpentineSwordEquips);
                    if (serpentineSwordEquips == 1) {
                        snakeShield.incrementLvlStatgain(
                                currentLvlNumber,
                                st.getUsedEquipment().getNumberOfEquips(
                                        snakeShield.getItemName()));
                    }
                }
            }
        }
        // Add special cases to list for text print out.
        statGivers.add(serpentineSword);
        statGivers.add(snakeShield);
        // Sort item list from highest total stat gain to lowest.
        Collections.sort(statGivers, new Comparator<StatgiverItem>() {
            @Override
            public int compare(final StatgiverItem o1, final StatgiverItem o2) {
                return o2.getTotalStats() - o1.getTotalStats();
            }
        });
        this.write("+STAT BREAKDOWN" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        this.write("Need to gain level (last is total):                          \t10\t39\t105\t231\t441\t759\t1209\t1815\t2601\t3591\t4809\t6279\t21904"
                + TextLogCreator.NEW_LINE);
        for (final StatgiverItem sgi : statGivers) {
            if (sgi.getTotalStats() > 0) {
                this.write(sgi.toString());
                this.write(TextLogCreator.NEW_LINE);
            }
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Familiars summary
        this.write("FAMILIARS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary()
                .getFamiliarUsage()) {
            this.write(dn.getData());
            this.write(" : ");
            this.write(dn.getNumber());
            this.write(" combat turns (");
            this.write(String.valueOf(Math.round((dn.getNumber() * 1000.0)
                    / logData.getLogSummary().getTotalTurnsCombat()) / 10.0));
            this.write("%)");
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Semi-rares summary
        this.write("SEMI-RARES" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary()
                .getSemirares()) {
            this.write(dn.getNumber());
            this.write(" : ");
            this.write(dn.getData());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Hunted combats summary
        this.write("HUNTED COMBATS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary()
                .getHuntedCombats()) {
            this.write(dn.getNumber());
            this.write(" : ");
            this.write(dn.getData());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Disintegrated combats summary
        this.write("YELLOW RAYS" + TextLogCreator.NEW_LINE
                + "----------" + TextLogCreator.NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary()
                .getDisintegratedCombats()) {
            this.write(dn.getNumber());
            this.write(" : ");
            this.write(dn.getData());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Skills cast summary
        this.write("CASTS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        for (final Skill s : logData.getLogSummary().getSkillsCast()) {
            this.write(s.toString());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + "------------------"
                + TextLogCreator.NEW_LINE + "| Total Casts    |  "
                + logData.getLogSummary().getTotalAmountSkillCasts()
                + TextLogCreator.NEW_LINE + "------------------"
                + TextLogCreator.NEW_LINE);
        this.write(TextLogCreator.NEW_LINE + "------------------"
                + TextLogCreator.NEW_LINE + "| Total MP Spent    |  "
                + logData.getLogSummary().getTotalMPUsed()
                + TextLogCreator.NEW_LINE + "------------------"
                + TextLogCreator.NEW_LINE);
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // MP summary
        final MPGain mpGains = logData.getLogSummary().getMPGains();
        this.write("MP GAINS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        this.write("Total mp gained: " + mpGains.getTotalMPGains()
                + TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE);
        this.write("Inside Encounters: " + mpGains.getEncounterMPGain()
                + TextLogCreator.NEW_LINE);
        this.write("Starfish Familiars: " + mpGains.getStarfishMPGain()
                + TextLogCreator.NEW_LINE);
        this.write("Resting: " + mpGains.getRestingMPGain()
                + TextLogCreator.NEW_LINE);
        this.write("Outside Encounters: " + mpGains.getOutOfEncounterMPGain()
                + TextLogCreator.NEW_LINE);
        this.write("Consumables: " + mpGains.getConsumableMPGain()
                + TextLogCreator.NEW_LINE);
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Consumables summary
        this.write("EATING AND DRINKING AND USING" + TextLogCreator.NEW_LINE
                + "----------" + TextLogCreator.NEW_LINE);
        this.write("Adventures gained eating: "
                + logData.getLogSummary().getTotalTurnsFromFood()
                + TextLogCreator.NEW_LINE);
        this.write("Adventures gained drinking: "
                + logData.getLogSummary().getTotalTurnsFromBooze()
                + TextLogCreator.NEW_LINE);
        this.write("Adventures gained using: "
                + logData.getLogSummary().getTotalTurnsFromOther()
                + TextLogCreator.NEW_LINE);
        this.write("Adventures gained rollover: "
                + logData.getLogSummary().getTotalTurnsFromRollover()
                + TextLogCreator.NEW_LINE);
        this.write(TextLogCreator.NEW_LINE);
        for (final Consumable c : logData.getLogSummary()
                .getFoodConsumablesUsed()) {
            this.write(c.toString());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE);
        for (final Consumable c : logData.getLogSummary()
                .getBoozeConsumablesUsed()) {
            this.write(c.toString());
            this.write(TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE);
        for (final Consumable c : logData.getLogSummary()
                .getSpleenConsumablesUsed()) {
            if (c.getAdventureGain() > 0) {
                this.write(c.toString());
                this.write(TextLogCreator.NEW_LINE);
            }
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Meat summary
        this.write("MEAT" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        this.write("Total meat gained: "
                + logData.getLogSummary().getTotalMeatGain()
                + TextLogCreator.NEW_LINE);
        this.write("Total meat spent: "
                + logData.getLogSummary().getTotalMeatSpent()
                + TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE);
        for (final DataNumberPair<MeatGain> dnp : logData.getLogSummary()
                .getMeatSummary().getAllLevelsMeatData()) {
            this.write("Level " + dnp.getNumber() + UsefulPatterns.COLON
                    + TextLogCreator.NEW_LINE);
            this.write("   Meat gain inside Encounters: "
                    + dnp.getData().encounterMeatGain + TextLogCreator.NEW_LINE);
            this.write("   Meat gain outside Encounters: "
                    + dnp.getData().otherMeatGain + TextLogCreator.NEW_LINE);
            this.write("   Meat spent: " + dnp.getData().meatSpent
                    + TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE + TextLogCreator.NEW_LINE
                + TextLogCreator.NEW_LINE);
        // Bottlenecks summary
        final List<DataNumberPair<String>> lostCombats = logData
                .getLostCombats();
        this.write("BOTTLENECKS" + TextLogCreator.NEW_LINE + "----------"
                + TextLogCreator.NEW_LINE);
        this.write("Sewered "
                + logData.getLogSummary().getSewer().getTurnsSpent()
                + " times for "
                + logData.getLogSummary().getSewer().getTrinketsFound()
                + " trinkets" + TextLogCreator.NEW_LINE);
        this.write("Spent "
                + logData.getLogSummary().get8BitRealm().getTurnsSpent()
                + " turns in the 8-Bit Realm" + TextLogCreator.NEW_LINE);
        this.write("Fought "
                + logData.getLogSummary().get8BitRealm().getBloopersFound()
                + " bloopers" + TextLogCreator.NEW_LINE);
        this.write("Fought "
                + logData.getLogSummary().get8BitRealm().getBulletsFound()
                + " bullet bills" + TextLogCreator.NEW_LINE);
        this.write("Spent "
                + logData.getLogSummary().getGoatlet().getTurnsSpent()
                + " turns in the Goatlet" + TextLogCreator.NEW_LINE);
        this.write("Fought "
                + logData.getLogSummary().getGoatlet().getDairyGoatsFound()
                + " dairy goats for "
                + logData.getLogSummary().getGoatlet().getCheeseFound()
                + " cheeses and "
                + logData.getLogSummary().getGoatlet().getMilkFound()
                + " glasses of milk" + TextLogCreator.NEW_LINE);
        this.write("Number of lost combats: " + lostCombats.size()
                + TextLogCreator.NEW_LINE);
        for (final DataNumberPair<String> dnp : lostCombats) {
            this.write("     " + dnp + TextLogCreator.NEW_LINE);
        }
        this.write(TextLogCreator.NEW_LINE);
        this.write("Free runaways: ");
        this.write(logData.getLogSummary().getFreeRunaways().toString());
        this.write(" overall");
        this.write(TextLogCreator.NEW_LINE);
    }

    private void write(final String s) {
        if (s != null) {
            this.log.append(s);
        } else {
            this.log.append(UsefulPatterns.EMPTY_STRING);
        }
    }

    private void write(final int i) {
        this.log.append(i);
    }

    /**
     * Enumeration to specify the wanted textual log output.
     */
    public static enum TextualLogVersion {
        TEXT_LOG, HTML_LOG, BBCODE_LOG;
    }
}
