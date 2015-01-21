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
package com.googlecode.logVisualizer.parser.mafiaLogBlockParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.SingleTurn.TurnVersion;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.parser.LineParser;
import com.googlecode.logVisualizer.parser.MafiaSessionLogReader;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.parser.lineParsers.CombatRecognizerLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.EquipmentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.ItemAcquisitionLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser.MPGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaDisintegrateLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MafiaFreeRunawaysLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser.MeatGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MeatSpentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.OdorExtractorUsageLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.SkillCastLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.StarfishMPGainLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.StatLineParser;
import com.googlecode.logVisualizer.util.DataNumberPair;

/**
 * A parser for the turn spent notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code [_turnNumber_] _areaName_}
 * <p>
 * {@code Encounter: _encounterName_}
 */
public final class EncounterBlockParser implements LogBlockParser {
    private static final Map<String, String> areaNameStandardizerMap;
    static {
        areaNameStandardizerMap = new HashMap<>(50);
        final Pattern areaNameMappingPattern = Pattern.compile(".+\\|\\s*.+");
        final Pattern splitPattern = Pattern.compile("\\s*\\|\\s*");
        final String commentStart = "//";
        String tmpLine;
        try (final BufferedReader br = DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "areaNameMappings.txt")) {
            while ((tmpLine = br.readLine()) != null) {
                if (!tmpLine.startsWith(commentStart)
                        && areaNameMappingPattern.matcher(tmpLine).matches()) {
                    try (final Scanner s = new Scanner(tmpLine)) {
                        s.useDelimiter(splitPattern);
                        final String areaName = s.next();
                        final String newAreaName = s.next();
                        s.close();
                        EncounterBlockParser.areaNameStandardizerMap.put(
                                areaName, newAreaName);
                    }
                }
            }
            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    private static final Set<String> OTHER_ENCOUNTER_AREAS_SET = new HashSet<>(
            Arrays.asList("Unlucky Sewer", "Sewer With Clovers", "Lemon Party",
                    "Guild Challenge", "Mining (In Disguise)"));
    private static final String ENCOUNTER_START_STRING = "Encounter: ";
    private static final String COOKING_START_STRING = "Cook ";
    private static final String MIXING_START_STRING = "Mix ";
    private static final String SMITHING_START_STRING = "Smith ";
    private static final String SHORE_AREAS_END_STRING = " Vacation";
    private static final String CLOWNLORD_CHOICE_ENCOUNTER_STRING = "Adventurer, $1.99";
    private static final String OUTFIT_STRING = "outfit";
    private static final String HP_LOSE_STRING_BEGINNING = "You lose ";
    private static final Pattern HP_LOSE_PATTERN = Pattern
            .compile("You lose \\d+ hit points");
    private final List<LineParser> lineParsers = new ArrayList<>();

    public EncounterBlockParser() {
        this.lineParsers.add(new ItemAcquisitionLineParser());
        this.lineParsers.add(new MafiaDisintegrateLineParser());
        this.lineParsers.add(new SkillCastLineParser());
        this.lineParsers.add(new MeatLineParser(MeatGainType.ENCOUNTER));
        this.lineParsers.add(new MeatSpentLineParser());
        this.lineParsers.add(new StatLineParser());
        this.lineParsers.add(new MPGainLineParser(MPGainType.ENCOUNTER));
        this.lineParsers.add(new CombatRecognizerLineParser());
        this.lineParsers.add(new EquipmentLineParser());
        this.lineParsers.add(new MafiaFreeRunawaysLineParser());
        this.lineParsers.add(new OdorExtractorUsageLineParser());
        this.lineParsers.add(new StarfishMPGainLineParser());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseBlock(final List<String> block, final LogDataHolder logData) {
        final String turnSpentLine = block.get(0).startsWith(
                UsefulPatterns.SQUARE_BRACKET_OPEN) ? block.get(0) : block
                .get(1);
        final SingleTurn turn;
        // Some areas have broken turn spent strings. If a turn is recognised as
        // being spent in such an area, the block will start with the encounter
        // name. We attempt to parse these encounters here.
        // If it is not a broken area, use the normal parsing.
        if (turnSpentLine
                .startsWith(EncounterBlockParser.ENCOUNTER_START_STRING)) {
            final String encounterName = turnSpentLine
                    .substring(EncounterBlockParser.ENCOUNTER_START_STRING
                            .length());
            final int turnNumber = logData.getTurnsSpent().last().getEndTurn() + 1;
            turn = new SingleTurn(encounterName, encounterName, turnNumber,
                    logData.getLastEquipmentChange(),
                    logData.getLastFamiliarChange());
            turn.setTurnVersion(TurnVersion.OTHER);
        } else {
            // Area name
            final int positionTurnEndBrace = turnSpentLine
                    .indexOf(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            String areaName = turnSpentLine.substring(positionTurnEndBrace + 2);
            // Check whether there is a mapping for the given area name
            final String mappedAreaName = EncounterBlockParser.areaNameStandardizerMap
                    .get(areaName);
            areaName = mappedAreaName != null ? mappedAreaName : areaName;
            // Special handling for crafting turns is needed, because mafia
            // screws up the turn number, plus the turn version of these should
            // be marked as OTHER, so this check has to be done anyway later on.
            final boolean isCraftingTurn = areaName
                    .startsWith(EncounterBlockParser.COOKING_START_STRING)
                    || areaName
                            .startsWith(EncounterBlockParser.MIXING_START_STRING)
                    || areaName
                            .startsWith(EncounterBlockParser.SMITHING_START_STRING);
            // Turn number
            // Special handling for crafting turns, because mafia screws up the
            // turn numbers of those.
            int turnNumber;
            final int positionTurnStartBrace = turnSpentLine
                    .indexOf(UsefulPatterns.SQUARE_BRACKET_OPEN);
            if (isCraftingTurn) {
                turnNumber = Integer.parseInt(turnSpentLine.substring(
                        positionTurnStartBrace + 1, positionTurnEndBrace)) - 1;
            } else {
                turnNumber = Integer.parseInt(turnSpentLine.substring(
                        positionTurnStartBrace + 1, positionTurnEndBrace));
            }
            // Now parse the encounter name.
            String encounterName = UsefulPatterns.EMPTY_STRING;
            boolean isMultipleCombatsHandling = false;
            for (final String line : block) {
                if (line.startsWith(EncounterBlockParser.ENCOUNTER_START_STRING)) {
                    if (line.length() == EncounterBlockParser.ENCOUNTER_START_STRING
                            .length()) {
                        // Something strange happened here. Do not count this
                        // turn. (clicking on a already cleansed cyrpt area can
                        // result in this)
                        return;
                    }
                    encounterName = line
                            .substring(EncounterBlockParser.ENCOUNTER_START_STRING
                                    .length());
                    isMultipleCombatsHandling = MafiaSessionLogReader.BROKEN_AREAS_ENCOUNTER_SET
                            .contains(line);
                    break;
                }
            }
            // If a combat may span over multiple turns, it will be handled in
            // here.
            if (isMultipleCombatsHandling) {
                areaName = encounterName;
                int combatCounter = -1;
                for (final String line : block) {
                    if (line.startsWith(EncounterBlockParser.ENCOUNTER_START_STRING)) {
                        combatCounter++;
                    }
                }
                // Every extra combat counted should be added now. This will
                // result in stats, meat and so on all being added to the last
                // combat only, but this problem shouldn't be happening often
                // enough to be a big deal. (currently only Ed the Undying falls
                // into this category here)
                for (int i = 0; i < combatCounter; i++) {
                    final SingleTurn tmp = new SingleTurn(areaName,
                            encounterName, turnNumber,
                            logData.getLastEquipmentChange(),
                            logData.getLastFamiliarChange());
                    tmp.setTurnVersion(TurnVersion.COMBAT);
                    logData.addTurnSpent(tmp);
                    turnNumber++;
                }
            }
            turn = new SingleTurn(areaName, encounterName, turnNumber,
                    logData.getLastEquipmentChange(),
                    logData.getLastFamiliarChange());
            // Set turn version. If the turn is a crafting turn, or the area
            // name is inside the other-encounters set, set the turn version to
            // OTHER, otherwise set it to NONCOMBAT. Combats are recognised
            // separately.
            if (isCraftingTurn
                    || EncounterBlockParser.OTHER_ENCOUNTER_AREAS_SET
                            .contains(areaName)) {
                turn.setTurnVersion(TurnVersion.OTHER);
            } else {
                turn.setTurnVersion(TurnVersion.NONCOMBAT);
            }
        }
        // Check handling for special encounters. If the encounter is indeed
        // a special encounter, the specialEncounterHandling() method will
        // handle adding the turn to the LogDataHolder.
        if (!EncounterBlockParser
                .specialEncounterHandling(turn, block, logData)) {
            // Add the turn to the given LogDataHolder instance.
            logData.addTurnSpent(turn);
        }
        for (final String line : block) {
            for (final LineParser lp : this.lineParsers) {
                // If the line parser can parse the line, this method also
                // returns true. This is used to cut back on the amount of
                // loops.
                if (lp.parseLine(line, logData)) {
                    break;
                }
            }
        }
        // Check whether the turn was a combat and whether it was lost. (If you
        // lose a combat the last line of the block is you losing HP.)
        String lastLine = block.get(block.size() - 1);
        if (lastLine.contains(EncounterBlockParser.OUTFIT_STRING)) {
            lastLine = block.get(block.size() - 2);
        }
        if ((turn.getTurnVersion() == TurnVersion.COMBAT)
                && lastLine
                        .startsWith(EncounterBlockParser.HP_LOSE_STRING_BEGINNING)
                && EncounterBlockParser.HP_LOSE_PATTERN.matcher(lastLine)
                        .matches()) {
            logData.addLostCombat(DataNumberPair.of(turn.getEncounterName(),
                    turn.getTurnNumber()));
        }
    }

    /**
     * Handling of special encounters which need additional computation, because
     * most of the time KolMafia doesn't log them correctly, such as multi-turn
     * encounters (e.g. the shore).
     * <p>
     * If an encounter is special and thus was processed here, this method will
     * return {@code true} and otherwise {@code false}.
     * <p>
     * Additionally, this method has to and will handle adding the given turn to
     * the LogDataHolder if the encounter is special.
     *
     * @param turn
     *            The encounter which is tested on whether it is special.
     * @param block
     *            The current working block of an ascension log.
     * @param logData
     *            The LogDataHolder of an ascension log in which the data should
     *            be saved in.
     * @return {@code true} if this method did some computation, because the
     *         given encounter is special, otherwise {@code false}.
     */
    private static boolean specialEncounterHandling(final SingleTurn turn,
            final List<String> block, final LogDataHolder logData) {
        if (turn.getAreaName().endsWith(
                EncounterBlockParser.SHORE_AREAS_END_STRING)) {
            final EquipmentChange lastEquipment = logData
                    .getLastEquipmentChange();
            final FamiliarChange lastFamiliar = logData.getLastFamiliarChange();
            final SingleTurn tmpTurn1 = new SingleTurn(turn.getAreaName(),
                    turn.getEncounterName(), turn.getTurnNumber() + 1,
                    lastEquipment, lastFamiliar);
            final SingleTurn tmpTurn2 = new SingleTurn(turn.getAreaName(),
                    turn.getEncounterName(), turn.getTurnNumber() + 2,
                    lastEquipment, lastFamiliar);
            turn.setTurnVersion(TurnVersion.OTHER);
            tmpTurn1.setTurnVersion(TurnVersion.OTHER);
            tmpTurn2.setTurnVersion(TurnVersion.OTHER);
            // Shore trip costs 500 meat.
            logData.getTurnsSpent().last().addMeatSpent(500);
            logData.addTurnSpent(turn);
            logData.addTurnSpent(tmpTurn1);
            logData.addTurnSpent(tmpTurn2);
            return true;
        } else if (turn.getEncounterName().equals(
                EncounterBlockParser.CLOWNLORD_CHOICE_ENCOUNTER_STRING)) {
            String firstChoice = UsefulPatterns.EMPTY_STRING;
            String secondChoice = UsefulPatterns.EMPTY_STRING;
            for (final String line : block) {
                if (line.contains("choice.php?")) {
                    final String tmp = line.replace("pwd", "").replace("&", "");
                    if (firstChoice.equals(UsefulPatterns.EMPTY_STRING)) {
                        firstChoice = tmp;
                    } else {
                        secondChoice = tmp;
                    }
                }
            }
            // Check if the correct choices were taken.
            if (firstChoice.equals("choice.php?whichchoice=151option=1")
                    && secondChoice
                            .equals("choice.php?whichchoice=152option=1")) {
                final SingleTurn clownlord = new SingleTurn(turn.getAreaName(),
                        "Clownlord Beelzebozo", turn.getTurnNumber() + 1,
                        logData.getLastEquipmentChange(),
                        logData.getLastFamiliarChange());
                logData.addTurnSpent(turn);
                logData.addTurnSpent(clownlord);
                return true;
            }
        }
        return false;
    }
}
