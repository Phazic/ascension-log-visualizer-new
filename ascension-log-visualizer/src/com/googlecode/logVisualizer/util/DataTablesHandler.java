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
package com.googlecode.logVisualizer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.logData.turn.SingleTurn;

/**
 * This class is a centralised place to handle access to various useful data
 * tables.
 */
public final class DataTablesHandler {
    private static final Map<String, Integer> skillMPCostMap;
    private static final Map<String, Integer> fullnessHitMap;
    private static final Map<String, Integer> drunkennessHitMap;
    private static final Map<String, Integer> spleenHitMap;
    private static final Map<String, Double> statsItemsMap;
    private static final Set<String> importantItemsSet;
    private static final Set<String> onetimeItemsSet;
    private static final Set<String> semirareSet;
    private static final Set<String> badmoonSet;
    private static final String FLOWERS_FOR_BAD_MOON_ADVENUTRE = "flowers for ";
    static {
        skillMPCostMap = new HashMap<>(150);
        fullnessHitMap = new HashMap<>(300);
        drunkennessHitMap = new HashMap<>(300);
        spleenHitMap = new HashMap<>(300);
        statsItemsMap = new HashMap<>(100);
        importantItemsSet = new HashSet<>(300);
        onetimeItemsSet = new HashSet<>(300);
        semirareSet = new HashSet<>(100);
        badmoonSet = new HashSet<>(100);
        DataTablesHandler.readFormattedTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "skills.txt"),
                DataTablesHandler.skillMPCostMap, Pattern
                        .compile("(.+)\\s+\\|\\s+(\\d+)"), true);
        DataTablesHandler.readFormattedTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "fullness.txt"),
                DataTablesHandler.fullnessHitMap, Pattern
                        .compile("([.[^\t]]+)\\s+(\\d+)\\s+.+"), true);
        DataTablesHandler.readFormattedTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "inebriety.txt"),
                DataTablesHandler.drunkennessHitMap, Pattern
                        .compile("([.[^\t]]+)\\s+(\\d+)\\s+.+"), true);
        DataTablesHandler.readFormattedTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "spleenhit.txt"),
                DataTablesHandler.spleenHitMap, Pattern
                        .compile("([.[^\t]]+)\\s+(\\d+)\\s+.+"), true);
        DataTablesHandler.readFormattedTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "statsItems.txt"),
                DataTablesHandler.statsItemsMap, Pattern
                        .compile("(.+)\\s+\\|\\s+(\\d+\\.?\\d*)"), false);
        DataTablesHandler.readTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "importantItems.txt"),
                DataTablesHandler.importantItemsSet);
        DataTablesHandler.readTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "onetimeItems.txt"),
                DataTablesHandler.onetimeItemsSet);
        DataTablesHandler.readTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "semirares.txt"),
                DataTablesHandler.semirareSet);
        DataTablesHandler.readTable(DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "badmoon.txt"),
                DataTablesHandler.badmoonSet);
    }

    private static final void readTable(final BufferedReader br,
            final Set<String> savedToSet) {
        String tmpLine;
        try {
            while ((tmpLine = br.readLine()) != null) {
                // Ignore empty lines and comments
                if ((tmpLine.length() > 0) && !tmpLine.startsWith("//")
                        && !tmpLine.startsWith("#")) {
                    savedToSet.add(tmpLine.toLowerCase(Locale.ENGLISH));
                }
            }
            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Currently only Integer and Double are supported by this method for the
     * value of the savedToMap.
     */
    private static final void readFormattedTable(final BufferedReader br,
            final Map<String, ? extends Number> savedToMap,
            final Pattern capturePattern, final boolean isInteger) {
        String tmpLine;
        // The savedToMap will always contain String->Number pairs.
        @SuppressWarnings("unchecked")
        final Map<String, Number> tmp = (Map<String, Number>) savedToMap;
        try {
            while ((tmpLine = br.readLine()) != null) {
                // Ignore empty lines and comments
                if ((tmpLine.length() > 0) && !tmpLine.startsWith("//")
                        && !tmpLine.startsWith("#")) {
                    final Matcher m = capturePattern.matcher(tmpLine);
                    if (m.matches()) {
                        final Number n;
                        if (isInteger) {
                            n = Integer.valueOf(Integer.parseInt(m.group(2)));
                        } else {
                            n = Double.valueOf(Double.parseDouble(m.group(2)));
                        }
                        tmp.put(m.group(1).toLowerCase(Locale.ENGLISH), n);
                    }
                }
            }
            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param skillName
     *            The skill name whose MP cost should be returned.
     * @return The MP cost of the given skill.
     */
    public static int getSkillMPCost(final String skillName) {
        final Integer mpCost = DataTablesHandler.skillMPCostMap.get(skillName
                .toLowerCase(Locale.ENGLISH));
        return mpCost != null ? mpCost.intValue() : 0;
    }

    /**
     * @param consumableName
     *            The consumable name whose fullness hit should be returned.
     * @return The fullness hit of the given consumable.
     */
    public static int getFullnessHit(final String consumableName) {
        final Integer fullnessHit = DataTablesHandler.fullnessHitMap
                .get(consumableName.toLowerCase(Locale.ENGLISH));
        return fullnessHit != null ? fullnessHit.intValue() : 0;
    }

    /**
     * @param consumableName
     *            The consumable name whose drunkenness hit should be returned.
     * @return The drunkenness hit of the given consumable.
     */
    public static int getDrunkennessHit(final String consumableName) {
        final Integer drunkennessHit = DataTablesHandler.drunkennessHitMap
                .get(consumableName.toLowerCase(Locale.ENGLISH));
        return drunkennessHit != null ? drunkennessHit.intValue() : 0;
    }

    /**
     * @param consumableName
     *            The consumable name whose spleen hit should be returned.
     * @return The spleen hit of the given consumable.
     */
    public static int getSpleenHit(final String consumableName) {
        final Integer spleenHit = DataTablesHandler.spleenHitMap
                .get(consumableName.toLowerCase(Locale.ENGLISH));
        return spleenHit != null ? spleenHit.intValue() : 0;
    }

    /**
     * @return A list of items and the amount of main substats they give per
     *         turn. The list is sorted from the highest stats per turn to the
     *         lowest.
     */
    public static List<Pair<String, Double>> getStatsItems() {
        final List<Pair<String, Double>> result = new ArrayList<>(
                DataTablesHandler.statsItemsMap.size());
        for (final String s : DataTablesHandler.statsItemsMap.keySet()) {
            result.add(Pair.of(s, DataTablesHandler.statsItemsMap.get(s)));
        }
        Collections.sort(result, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(final Pair<String, Double> p1,
                    final Pair<String, Double> p2) {
                if (p2.getVar2() > p1.getVar2()) {
                    return 1;
                } else if (p2.getVar2() < p1.getVar2()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        return result;
    }

    /**
     * A check for the important items collection. Important items are items
     * that are always listed in textual logs.
     *
     * @param itemName
     *            The item name which should be checked on whether it is an
     *            important item.
     * @return True if the item is an important item, otherwise false.
     */
    public static boolean isImportantItem(final String itemName) {
        return DataTablesHandler.importantItemsSet.contains(itemName
                .toLowerCase(Locale.ENGLISH));
    }

    /**
     * A check for the one-time items collection. One-time items are items that
     * are listed in textual logs only the first time they dropped.
     *
     * @param itemName
     *            The item name which should be checked on whether it is an
     *            one-time item.
     * @return True if the item is an one-time item, otherwise false.
     */
    public static boolean isOnetimeItem(final String itemName) {
        return DataTablesHandler.onetimeItemsSet.contains(itemName
                .toLowerCase(Locale.ENGLISH));
    }

    /**
     * @return A read-only set containing all one-time items.
     */
    public static Set<String> getOnetimeItems() {
        return Collections.unmodifiableSet(DataTablesHandler.onetimeItemsSet);
    }

    /**
     * @param encounter
     *            The single turn which should be checked on whether it is a
     *            semi-rare.
     * @return True if the encounter is a semi-rare, otherwise false.
     */
    public static boolean isSemirareEncounter(final SingleTurn encounter) {
        return DataTablesHandler.isSemirareEncounter(encounter
                .getEncounterName());
    }

    /**
     * @param encounterName
     *            The encounter name which should be checked on whether it is a
     *            semi-rare.
     * @return True if the encounter is a semi-rare, otherwise false.
     */
    public static boolean isSemirareEncounter(final String encounterName) {
        return DataTablesHandler.semirareSet.contains(encounterName
                .toLowerCase(Locale.ENGLISH));
    }

    /**
     * @param encounter
     *            The single turn which should be checked on whether it is a Bad
     *            Moon adventure.
     * @return True if the encounter is a Bad Moon adventure, otherwise false.
     */
    public static boolean isBadMoonEncounter(final SingleTurn encounter) {
        return DataTablesHandler.isBadMoonEncounter(encounter
                .getEncounterName());
    }

    /**
     * @param encounterName
     *            The encounter name which should be checked on whether it is a
     *            Bad Moon adventure.
     * @return True if the encounter is a Bad Moon adventure, otherwise false.
     */
    public static boolean isBadMoonEncounter(final String encounterName) {
        final String tmp = encounterName.toLowerCase(Locale.ENGLISH);
        return DataTablesHandler.badmoonSet.contains(tmp) ? true : tmp
                .startsWith(DataTablesHandler.FLOWERS_FOR_BAD_MOON_ADVENUTRE);
    }

    // This class is not to be instanced.
    private DataTablesHandler() {
    }
}
