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
package com.googlecode.logVisualizer.logData.logSummary;

import java.util.Collection;

import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;

/**
 * This immutable class calculates and holds all the data on various quest
 * turncounts.
 */
public final class QuestTurncounts {
    private final int mosquitoQuestTurns;
    private final int templeOpeningTurns;
    private final int tavernQuestTurns;
    private final int batQuestTurns;
    private final int knobQuestTurns;
    private final int friarsQuestTurns;
    private final int cyrptQuestTurns;
    private final int trapzorQuestTurns;
    private final int chasmQuestTurns;
    private final int airshipQuestTurns;
    private final int castleQuestTurns;
    private final int ballroomOpeningTurns;
    private final int pirateQuestTurns;
    private final int blackForrestQuestTurns;
    private final int desertOasisQuestTurns;
    private final int spookyravenQuestTurns;
    private final int templeCityQuestTurns;
    private final int palindomeQuestTurns;
    private final int pyramidQuestTurns;
    private final int warIslandOpeningTurns;
    private final int warIslandQuestTurns;
    private final int dodQuestTurns;

    /**
     * Constructs a new instance and calculates all quest turncounts.
     *
     * @param turns
     *            The turn rundown of the ascension.
     * @param droppedItems
     *            All dropped items during the ascension.
     */
    public QuestTurncounts(final Collection<TurnInterval> turns,
            final Collection<Item> droppedItems) {
        if (turns == null) {
            throw new NullPointerException("Turn rundown set must not be null.");
        }
        if (droppedItems == null) {
            throw new NullPointerException(
                    "Dropped items list must not be null.");
        }
        this.mosquitoQuestTurns = QuestTurncounts.getTurnsUntilItemFound("Spooky Forest",
                "mosquito larva", turns, droppedItems);
        this.templeOpeningTurns = QuestTurncounts.calculateTempleOpening(turns,
                droppedItems);
        this.tavernQuestTurns = QuestTurncounts.getTurnsInLocation("Typical Tavern Quest",
                turns);
        this.batQuestTurns = QuestTurncounts.getTurnsUntilOtherLocationOpen(
                "Guano Junction", "Boss Bat's Lair", turns)
                + QuestTurncounts.getTurnsInLocation("Boss Bat's Lair", turns);
        this.knobQuestTurns = QuestTurncounts.getTurnsUntilItemFound(
                "Outskirts of The Knob", "Knob Goblin encryption key", turns,
                droppedItems)
                + QuestTurncounts.getTurnsInLocation("Knob Goblin Harem", turns)
                + QuestTurncounts.getTurnsInLocation("King's Chamber", turns);
        this.friarsQuestTurns = QuestTurncounts.getTurnsUntilItemFound(
                "Dark Neck of the Woods", "dodecagram", turns, droppedItems)
                + QuestTurncounts.getTurnsUntilItemFound("Dark Heart of the Woods",
                        "box of birthday candles", turns, droppedItems)
                + QuestTurncounts.getTurnsUntilItemFound("Dark Elbow of the Woods",
                        "eldritch butterknife", turns, droppedItems);
        this.cyrptQuestTurns = QuestTurncounts.getTurnsInLocation("Defiled Cranny", turns)
                + QuestTurncounts.getTurnsInLocation("Defiled Nook", turns)
                + QuestTurncounts.getTurnsInLocation("Defiled Alcove", turns)
                + QuestTurncounts.getTurnsInLocation("Defiled Niche", turns)
                + QuestTurncounts.getTurnsInLocation("Haert of the Cyrpt", turns);
        this.trapzorQuestTurns = QuestTurncounts.getTurnsInLocation("Itznotyerzitz Mine",
                turns)
                + QuestTurncounts.getTurnsInLocation("Mining (In Disguise)", turns)
                + QuestTurncounts.getTurnsInLocation("Goatlet", turns);
        this.chasmQuestTurns = QuestTurncounts.getTurnsInLocation("Orc Chasm", turns);
        this.airshipQuestTurns = QuestTurncounts.getTurnsUntilItemFound("Fantasy Airship",
                "S.O.C.K.", turns, droppedItems);
        this.castleQuestTurns = QuestTurncounts.getTurnsUntilOtherLocationOpen(
                "Giant's Castle", "Hole in the Sky", turns);
        this.ballroomOpeningTurns = QuestTurncounts.getTurnsUntilOtherLocationOpen(
                "Haunted Pantry", "Haunted Billiards Room", turns)
                + QuestTurncounts.getTurnsUntilOtherLocationOpen("Haunted Billiards Room",
                        "Haunted Library", turns)
                + QuestTurncounts.getTurnsUntilOtherLocationOpen("Haunted Library",
                        "Haunted Bedroom", turns)
                + QuestTurncounts.getTurnsUntilOtherLocationOpen("Haunted Bedroom",
                        "Haunted Ballroom", turns);
        this.pirateQuestTurns = QuestTurncounts.getTurnsUntilOtherLocationOpen(
                "Pirate Cove", "Barrrney's Barrr", turns)
                + QuestTurncounts.getTurnsUntilOtherLocationOpen("Barrrney's Barrr",
                        "F'c'le", turns)
                + QuestTurncounts.getTurnsInLocation("Cap'm Caronch's Map", turns)
                + QuestTurncounts.getTurnsInLocation("The Infiltrationist", turns)
                + QuestTurncounts.getTurnsUntilOtherLocationOpen("F'c'le", "Poop Deck",
                        turns)
                + QuestTurncounts.getTurnsUntilOtherLocationOpen("Poop Deck",
                        "Belowdecks", turns)
                + QuestTurncounts.getTurnsUntilOtherLocationOpen("Belowdecks",
                        "Palindome", turns);
        this.blackForrestQuestTurns = QuestTurncounts.getTurnsInLocation("Black Forest",
                turns);
        this.desertOasisQuestTurns = QuestTurncounts.getTurnsInLocation(
                "Desert (Unhydrated)", turns)
                + QuestTurncounts.getTurnsInLocation("Desert (Ultrahydrated)", turns)
                + QuestTurncounts.getTurnsInLocation("Oasis in the Desert", turns);
        this.spookyravenQuestTurns = QuestTurncounts.getTurnsAfterLocationOpen(
                "Haunted Ballroom", "Black Forest", turns)
                + QuestTurncounts.getTurnsInLocation("Haunted Wine Cellar (Northwest)",
                        turns)
                + QuestTurncounts.getTurnsInLocation("Haunted Wine Cellar (Northeast)",
                        turns)
                + QuestTurncounts.getTurnsInLocation("Haunted Wine Cellar (Southwest)",
                        turns)
                + QuestTurncounts.getTurnsInLocation("Haunted Wine Cellar (Southeast)",
                        turns)
                + QuestTurncounts.getTurnsInLocation("Haunted Wine Cellar", turns)
                + QuestTurncounts.getTurnsInLocation("Lord Spookyraven", turns)
                + QuestTurncounts.getTurnsInLocation("Summoning Chamber", turns);
        this.templeCityQuestTurns = QuestTurncounts.getTurnsInLocation("Hidden Temple",
                turns)
                + QuestTurncounts.getTurnsInLocation("Hidden City", turns)
                + QuestTurncounts.getTurnsInLocation("Hidden City (Altar)", turns)
                + QuestTurncounts.getTurnsInLocation("Hidden City (Temple)", turns);
        this.palindomeQuestTurns = QuestTurncounts.getTurnsInLocation("Palindome", turns)
                + QuestTurncounts.getTurnsAfterLocationOpen("Knob Goblin Laboratory",
                        "Palindome", turns)
                + QuestTurncounts.getTurnsAfterLocationOpen("Whitey's Grove", "Palindome",
                        turns);
        this.pyramidQuestTurns = QuestTurncounts.getTurnsInLocation("The Upper Chamber",
                turns)
                + QuestTurncounts.getTurnsInLocation("The Middle Chamber", turns)
                + QuestTurncounts.getTurnsInLocation("The Lower Chamber", turns)
                + QuestTurncounts.getTurnsInLocation("The Lower Chambers (Token/Empty)",
                        turns)
                + QuestTurncounts.getTurnsInLocation("The Lower Chambers (Rubble/Bomb)",
                        turns)
                + QuestTurncounts.getTurnsInLocation(
                        "The Lower Chambers (Empty/Empty/Ed's Chamber)", turns)
                + QuestTurncounts.getTurnsInLocation("The Lower Chambers (Empty/Rubble)",
                        turns)
                + QuestTurncounts.getTurnsInLocation("Ed the Undying", turns);
        this.warIslandOpeningTurns = QuestTurncounts.getTurnsAfterLocationOpen(
                "Hippy Camp", "Palindome", turns)
                + QuestTurncounts.getTurnsInLocation(
                        "Wartime Hippy Camp (Hippy Disguise)", turns)
                + QuestTurncounts.getTurnsInLocation("Wartime Hippy Camp (Frat Disguise)",
                        turns)
                + QuestTurncounts.getTurnsAfterLocationOpen("Frat House", "Palindome",
                        turns)
                + QuestTurncounts.getTurnsInLocation(
                        "Wartime Frat House (Hippy Disguise)", turns)
                + QuestTurncounts.getTurnsInLocation("Wartime Frat House (Frat Disguise)",
                        turns);
        this.warIslandQuestTurns = QuestTurncounts.getTurnsInLocation("Hatching Chamber",
                turns)
                + QuestTurncounts.getTurnsInLocation("Feeding Chamber", turns)
                + QuestTurncounts.getTurnsInLocation("Guards' Chamber", turns)
                + QuestTurncounts.getTurnsInLocation("Queen's Chamber", turns)
                + QuestTurncounts.getTurnsInLocation(
                        "Barrel with Something Burning in it", turns)
                + QuestTurncounts.getTurnsInLocation("Over Where the Old Tires Are", turns)
                + QuestTurncounts.getTurnsInLocation("Near an Abandoned Refrigerator",
                        turns)
                + QuestTurncounts.getTurnsInLocation("Out by that Rusted-Out Car", turns)
                + QuestTurncounts.getTurnsInLocation("Wartime Sonofa Beach", turns)
                + QuestTurncounts.getTurnsInLocation("Themthar Hills", turns)
                + QuestTurncounts.getTurnsInLocation("Barn", turns)
                + QuestTurncounts.getTurnsInLocation("Family Plot", turns)
                + QuestTurncounts.getTurnsInLocation("Pond", turns)
                + QuestTurncounts.getTurnsInLocation("Other Back 40", turns)
                + QuestTurncounts.getTurnsInLocation("Back 40", turns)
                + QuestTurncounts.getTurnsInLocation("Granary", turns)
                + QuestTurncounts.getTurnsInLocation("Bog", turns)
                + QuestTurncounts.getTurnsInLocation("Shady Thicket", turns)
                + QuestTurncounts.getTurnsInLocation("Battlefield (Frat Uniform)", turns)
                + QuestTurncounts.getTurnsInLocation("Battlefield (Hippy Uniform)", turns)
                + QuestTurncounts.getTurnsInLocation("Big Wisniewski", turns)
                + QuestTurncounts.getTurnsInLocation("The Big Wisniewski", turns)
                + QuestTurncounts.getTurnsInLocation("The Man", turns);
        this.dodQuestTurns = QuestTurncounts
                .getTurnsInLocation("Greater-Than Sign", turns)
                + QuestTurncounts.getTurnsUntilItemFound("Dungeons of Doom", "dead mimic",
                        turns, droppedItems);
    }

    /**
     * @param turns
     *            The turn rundown of the ascension.
     * @param droppedItems
     *            All dropped items during the ascension.
     * @return Turns spent.
     */
    private static int calculateTempleOpening(final Collection<TurnInterval> turns,
            final Collection<Item> droppedItems) {
        final int spookyGro = QuestTurncounts.getTurnsUntilItemFound("Spooky Forest",
                "Spooky-Gro fertilizer", turns, droppedItems);
        final int sapling = QuestTurncounts.getTurnsUntilItemFound("Spooky Forest",
                "spooky sapling", turns, droppedItems);
        return spookyGro > sapling ? spookyGro : sapling;
    }

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    private static int getTurnsInLocation(final String areaName,
            final Collection<TurnInterval> turns) {
        int turnsSpent = 0;
        for (final TurnInterval ti : turns) {
            if (ti.getAreaName().equals(areaName)) {
                turnsSpent += ti.getTotalTurns();
            }
        }
        return turnsSpent;
    }

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param openedLocation
     *            The name of the area whose opening stops the turncounting.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    private static int getTurnsUntilOtherLocationOpen(final String areaName,
            final String openedLocation, final Collection<TurnInterval> turns) {
        int turnsSpent = 0;
        int firstTurnInOpenedLocation = Integer.MAX_VALUE;
        for (final TurnInterval ti : turns) {
            if (ti.getAreaName().equals(openedLocation)) {
                firstTurnInOpenedLocation = ti.getStartTurn();
                break;
            }
        }
        for (final TurnInterval ti : turns) {
            if (ti.getStartTurn() >= firstTurnInOpenedLocation) {
                break;
            } else if (ti.getAreaName().equals(areaName)) {
                turnsSpent += ti.getTotalTurns();
            }
        }
        return turnsSpent;
    }

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param alreadyOpenLocation
     *            The name of the area which has to be already open before the
     *            turncounting starts.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    private static int getTurnsAfterLocationOpen(final String areaName,
            final String alreadyOpenLocation,
            final Collection<TurnInterval> turns) {
        return QuestTurncounts.getTurnsAfterLocationOpenUntilOtherLocationOpen(areaName,
                alreadyOpenLocation, "", turns);
    }

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param alreadyOpenLocation
     *            The name of the area which has to be already open before the
     *            turncounting starts.
     * @param toBeOpenedLocation
     *            The name of the area whose opening stops the turncounting.
     * @param turns
     *            The turn rundown of the ascension.
     * @return Turns spent.
     */
    private static int getTurnsAfterLocationOpenUntilOtherLocationOpen(
            final String areaName, final String alreadyOpenLocation,
            final String toBeOpenedLocation,
            final Collection<TurnInterval> turns) {
        int turnsSpent = 0;
        int firstTurnInAlreadyOpenedLocation = Integer.MIN_VALUE;
        int firstTurnInToBeOpenedLocation = Integer.MAX_VALUE;
        for (final TurnInterval ti : turns) {
            if ((firstTurnInAlreadyOpenedLocation == Integer.MIN_VALUE)
                    && ti.getAreaName().equals(alreadyOpenLocation)) {
                firstTurnInAlreadyOpenedLocation = ti.getStartTurn();
            } else if (ti.getAreaName().equals(toBeOpenedLocation)) {
                firstTurnInToBeOpenedLocation = ti.getStartTurn();
                break;
            }
        }
        for (final TurnInterval ti : turns) {
            if (ti.getStartTurn() >= firstTurnInAlreadyOpenedLocation) {
                if (ti.getStartTurn() >= firstTurnInToBeOpenedLocation) {
                    break;
                } else if (ti.getAreaName().equals(areaName)) {
                    turnsSpent += ti.getTotalTurns();
                }
            }
        }
        return turnsSpent;
    }

    /**
     * @param areaName
     *            The name of the area the turns are spent in.
     * @param itemName
     *            The name of the item which has to be found.
     * @param turns
     *            The turn rundown of the ascension.
     * @param droppedItems
     *            All dropped items during the ascension.
     * @return Turns spent.
     */
    private static int getTurnsUntilItemFound(final String areaName,
            final String itemName, final Collection<TurnInterval> turns,
            final Collection<Item> droppedItems) {
        int turnsSpent = 0;
        int finishedOnTurn = Integer.MAX_VALUE;
        for (final Item i : droppedItems) {
            if (i.getName().equals(itemName)) {
                finishedOnTurn = i.getFoundOnTurn();
                break;
            }
        }
        for (final TurnInterval ti : turns) {
            if (ti.getAreaName().equals(areaName)) {
                if ((ti.getStartTurn() <= finishedOnTurn)
                        && (ti.getEndTurn() <= finishedOnTurn)) {
                    turnsSpent += ti.getTotalTurns();
                } else if ((ti.getStartTurn() <= finishedOnTurn)
                        && (ti.getEndTurn() > finishedOnTurn)) {
                    turnsSpent += finishedOnTurn - ti.getStartTurn();
                    break;
                }
            }
        }
        return turnsSpent;
    }

    /**
     * @return The amount of turns the Mosquito quest took.
     */
    public int getMosquitoQuestTurns() {
        return this.mosquitoQuestTurns;
    }

    /**
     * @return The amount of turns opening the Hidden Temple took.
     */
    public int getTempleOpeningTurns() {
        return this.templeOpeningTurns;
    }

    /**
     * @return The amount of turns the Tavern quest took.
     */
    public int getTavernQuestTurns() {
        return this.tavernQuestTurns;
    }

    /**
     * @return The amount of turns the Bat quest took.
     */
    public int getBatQuestTurns() {
        return this.batQuestTurns;
    }

    /**
     * @return The amount of turns the Knob Goblin quest took.
     */
    public int getKnobQuestTurns() {
        return this.knobQuestTurns;
    }

    /**
     * @return The amount of turns the Friars quest took.
     */
    public int getFriarsQuestTurns() {
        return this.friarsQuestTurns;
    }

    /**
     * @return The amount of turns the Cyrpt quest took.
     */
    public int getCyrptQuestTurns() {
        return this.cyrptQuestTurns;
    }

    /**
     * @return The amount of turns the Trapzor quest took.
     */
    public int getTrapzorQuestTurns() {
        return this.trapzorQuestTurns;
    }

    /**
     * @return The amount of turns the Orc Chasm quest took.
     */
    public int getChasmQuestTurns() {
        return this.chasmQuestTurns;
    }

    /**
     * @return The amount of turns the Airship quest took.
     */
    public int getAirshipQuestTurns() {
        return this.airshipQuestTurns;
    }

    /**
     * @return The amount of turns the Castle quest took.
     */
    public int getCastleQuestTurns() {
        return this.castleQuestTurns;
    }

    /**
     * @return The amount of turns opening the ballroom took.
     */
    public int getBallroomOpeningTurns() {
        return this.ballroomOpeningTurns;
    }

    /**
     * @return The amount of turns the Pirate quest took.
     */
    public int getPirateQuestTurns() {
        return this.pirateQuestTurns;
    }

    /**
     * @return The amount of turns the Black Forest quest took.
     */
    public int getBlackForrestQuestTurns() {
        return this.blackForrestQuestTurns;
    }

    /**
     * @return The amount of turns finding the pyramid took.
     */
    public int getDesertOasisQuestTurns() {
        return this.desertOasisQuestTurns;
    }

    /**
     * @return The amount of turns the Spookyraven quest took.
     */
    public int getSpookyravenQuestTurns() {
        return this.spookyravenQuestTurns;
    }

    /**
     * @return The amount of turns the Hidden City quest took.
     */
    public int getTempleCityQuestTurns() {
        return this.templeCityQuestTurns;
    }

    /**
     * @return The amount of turns the Palindome quest took.
     */
    public int getPalindomeQuestTurns() {
        return this.palindomeQuestTurns;
    }

    /**
     * @return The amount of turns completing the pyramid took.
     */
    public int getPyramidQuestTurns() {
        return this.pyramidQuestTurns;
    }

    /**
     * @return The amount of turns opening the battlefield took.
     */
    public int getWarIslandOpeningTurns() {
        return this.warIslandOpeningTurns;
    }

    /**
     * @return The amount of turns completing the battlefield with all
     *         sidequests took.
     */
    public int getWarIslandQuestTurns() {
        return this.warIslandQuestTurns;
    }

    /**
     * @return The amount of turns the DoD quest took.
     */
    public int getDodQuestTurns() {
        return this.dodQuestTurns;
    }
}