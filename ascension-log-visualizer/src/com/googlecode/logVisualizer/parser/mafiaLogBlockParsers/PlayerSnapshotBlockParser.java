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

import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.LogDataHolder.CharacterClass;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the player login snapshot data in mafia logs.
 * <p>
 * The snapshot start with this:
 * <p>
 *
 * <pre>
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 *                Player Snapshot
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 * </pre>
 * <p>
 * And ends with this:
 * <p>
 *
 * <pre>
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 * </pre>
 */
public final class PlayerSnapshotBlockParser implements LogBlockParser {
    private static final Pattern PLAYERSTATS_WBUFFED_PATTERN = Pattern
            .compile("(?:Mus|Mys|Mox)\\: \\d+ \\(\\d+\\).*");
    private static final Pattern PLAYERSTATS_WOBUFFED_PATTERN = Pattern
            .compile("(?:Mus|Mys|Mox)\\: \\d+(?:$|, tnp =.*)");
    private static final Pattern NOT_FAMILIAR_NAME_PATTERN = Pattern
            .compile("Pet: | \\(\\d+ lbs\\)\\s*");
    private static final String CLASS_LINE_BEGINNING_STRING = "Class: ";
    private static final String FAMILIAR_LINE_BEGINNING_STRING = "Pet: ";
    private static final String ADVENTURES_LINE_BEGINNING_STRING = "Advs: ";
    private static final String MEAT_LINE_BEGINNING_STRING = "Meat: ";
    private static final String HAT_BEGINNING_STRING = "Hat: ";
    private static final String WEAPON_BEGINNING_STRING = "Weapon: ";
    private static final String OFFHAND_BEGINNING_STRING = "Off-hand: ";
    private static final String SHIRT_BEGINNING_STRING = "Shirt: ";
    private static final String PANTS_BEGINNING_STRING = "Pants: ";
    private static final String ACC1_BEGINNING_STRING = "Acc. 1: ";
    private static final String ACC2_BEGINNING_STRING = "Acc. 2: ";
    private static final String ACC3_BEGINNING_STRING = "Acc. 3: ";
    private static final String FAM_EQUIP_BEGINNING_STRING = "Item: ";
    private static final String NO_EQUIP_STRING = "(none)";
    private static final String DAY_CHANGE_STRING = "Day change occurred";
    private final Matcher statsWithBuffed = PlayerSnapshotBlockParser.PLAYERSTATS_WBUFFED_PATTERN
            .matcher(UsefulPatterns.EMPTY_STRING);
    private final Matcher statsWithoutBuffed = PlayerSnapshotBlockParser.PLAYERSTATS_WOBUFFED_PATTERN
            .matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseBlock(final List<String> block, final LogDataHolder logData) {
        String hat = EquipmentChange.NO_EQUIPMENT_STRING;
        String weapon = EquipmentChange.NO_EQUIPMENT_STRING;
        String offhand = EquipmentChange.NO_EQUIPMENT_STRING;
        String shirt = EquipmentChange.NO_EQUIPMENT_STRING;
        String pants = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc1 = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc2 = EquipmentChange.NO_EQUIPMENT_STRING;
        String acc3 = EquipmentChange.NO_EQUIPMENT_STRING;
        String famEquip = EquipmentChange.NO_EQUIPMENT_STRING;
        final int turnNumber = logData.getTurnsSpent().last().getEndTurn();
        int mus = -1;
        int myst = -1;
        int mox = -1;
        int adventuresLeft = 0;
        int meat = 0;
        for (final String line : block) {
            if (line.length() > 0) {
                if (this.statsWithBuffed.reset(line).matches()) {
                    if (mus < 0) {
                        mus = PlayerSnapshotBlockParser.parseStatWBuffed(line);
                    } else if (myst < 0) {
                        myst = PlayerSnapshotBlockParser.parseStatWBuffed(line);
                    } else if (mox < 0) {
                        mox = PlayerSnapshotBlockParser.parseStatWBuffed(line);
                    }
                } else if (this.statsWithoutBuffed.reset(line).matches()) {
                    if (mus < 0) {
                        mus = PlayerSnapshotBlockParser.parseStatWOBuffed(line);
                    } else if (myst < 0) {
                        myst = PlayerSnapshotBlockParser
                                .parseStatWOBuffed(line);
                    } else if (mox < 0) {
                        mox = PlayerSnapshotBlockParser.parseStatWOBuffed(line);
                    }
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.FAMILIAR_LINE_BEGINNING_STRING)) {
                    try (final Scanner s = new Scanner(line)) {
                        s.useDelimiter(PlayerSnapshotBlockParser.NOT_FAMILIAR_NAME_PATTERN);
                        logData.addFamiliarChange(new FamiliarChange(s.next(),
                                turnNumber));
                        s.close();
                    }
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.ADVENTURES_LINE_BEGINNING_STRING)) {
                    adventuresLeft = Integer.parseInt(line.substring(line
                            .indexOf(UsefulPatterns.COLON) + 2));
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.MEAT_LINE_BEGINNING_STRING)
                        && !line.contains(UsefulPatterns.PERCENTAGE_SIGN)) {
                    meat = Integer.parseInt(line.substring(
                            line.indexOf(UsefulPatterns.COLON) + 2).replace(
                            UsefulPatterns.COMMA, UsefulPatterns.EMPTY_STRING));
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.HAT_BEGINNING_STRING)) {
                    hat = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.WEAPON_BEGINNING_STRING)) {
                    weapon = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.OFFHAND_BEGINNING_STRING)) {
                    offhand = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.SHIRT_BEGINNING_STRING)) {
                    shirt = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.PANTS_BEGINNING_STRING)) {
                    pants = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.ACC1_BEGINNING_STRING)) {
                    acc1 = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.ACC2_BEGINNING_STRING)) {
                    acc2 = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.ACC3_BEGINNING_STRING)) {
                    acc3 = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.FAM_EQUIP_BEGINNING_STRING)
                        && !line.contains(UsefulPatterns.PERCENTAGE_SIGN)) {
                    famEquip = PlayerSnapshotBlockParser.getEquipmentName(line);
                } else if (line
                        .startsWith(PlayerSnapshotBlockParser.DAY_CHANGE_STRING)) {
                    // Get day number of last day change
                    final int dayNumber = logData.getLastDayChange()
                            .getDayNumber();
                    // Get turn number of last turn spent
                    final int turn = logData.getTurnsSpent().last()
                            .getEndTurn();
                    // Add day change
                    logData.addDayChange(new DayChange(dayNumber + 1, turn));
                } else if (logData.getCharacterClass() == CharacterClass.NOT_DEFINED) {
                    if (line.startsWith(PlayerSnapshotBlockParser.CLASS_LINE_BEGINNING_STRING)) {
                        logData.setCharacterClass(line
                                .substring(PlayerSnapshotBlockParser.CLASS_LINE_BEGINNING_STRING
                                        .length()));
                    }
                }
            }
        }
        // Add the currently worn equipment.
        logData.addEquipmentChange(new EquipmentChange(turnNumber, hat, weapon,
                offhand, shirt, pants, acc1, acc2, acc3, famEquip));
        // A check to make sure the parsing worked, if it did, add the player
        // snapshot.
        if ((mus >= 0) && (myst >= 0) && (mox >= 0)) {
            logData.addPlayerSnapshot(new PlayerSnapshot(mus, myst, mox,
                    adventuresLeft, meat, turnNumber));
        }
    }

    private static String getEquipmentName(final String line) {
        String itemName = line
                .substring(line.indexOf(UsefulPatterns.COLON) + 2).toLowerCase(
                        Locale.ENGLISH);
        if (itemName.contains(PlayerSnapshotBlockParser.NO_EQUIP_STRING)) {
            itemName = EquipmentChange.NO_EQUIPMENT_STRING;
        } else if (itemName.endsWith(UsefulPatterns.ROUND_BRACKET_CLOSE)) {
            itemName = itemName
                    .substring(0, itemName
                            .lastIndexOf(UsefulPatterns.ROUND_BRACKET_OPEN) - 1);
        }
        return itemName;
    }

    private static int parseStatWBuffed(final String line) {
        return Integer.parseInt(line.substring(
                line.indexOf(UsefulPatterns.ROUND_BRACKET_OPEN) + 1,
                line.indexOf(UsefulPatterns.ROUND_BRACKET_CLOSE)));
    }

    private static int parseStatWOBuffed(final String line) {
        final String tmp = line.substring(line
                .indexOf(UsefulPatterns.WHITE_SPACE) + 1);
        if (tmp.contains(UsefulPatterns.COMMA)) {
            return Integer.parseInt(tmp.substring(0,
                    tmp.indexOf(UsefulPatterns.COMMA)));
        }
        return Integer.parseInt(tmp);
    }
}
