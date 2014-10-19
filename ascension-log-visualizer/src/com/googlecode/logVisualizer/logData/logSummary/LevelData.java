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

import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * This is a container class for various data on levels.
 */
public final class LevelData implements Comparable<LevelData> {
    private static final String HIT_LEVEL_START_STRING = "Hit Level";
    private static final String HIT_LEVEL_MIDDLE_STRING = "on turn";
    private final int levelNumber;
    private final int levelReachedOnTurn;
    private int combatTurns;
    private int noncombatTurns;
    private int otherTurns;
    private Statgain statsAtLevelReached = new Statgain();
    private double statGainPerTurn;

    /**
     * @param levelNumber
     *            The level number to set.
     * @param levelReachedOnTurn
     *            The turn on which this level was reached to set.
     * @throws IllegalArgumentException
     *             if levelNumber is below 1; if levelReachedOnTurn is below 0
     */
    public LevelData(final int levelNumber, final int levelReachedOnTurn) {
        if (levelNumber < 1) {
            throw new IllegalArgumentException("Level cannot be below 1.");
        }
        if (levelReachedOnTurn < 0) {
            throw new IllegalArgumentException(
                    "There are no negative turn numbers.");
        }
        this.levelNumber = levelNumber;
        this.levelReachedOnTurn = levelReachedOnTurn;
    }

    /**
     * @return The level number.
     */
    public int getLevelNumber() {
        return this.levelNumber;
    }

    /**
     * @return The turn on which this level was reached.
     */
    public int getLevelReachedOnTurn() {
        return this.levelReachedOnTurn;
    }

    /**
     * @param combatTurns
     *            The number of combats encountered during this level to set. A
     *            value below 0 is not a valid parameter.
     * @throws IllegalArgumentException
     *             if combatTurns is below 0
     */
    public void setCombatTurns(final int combatTurns) {
        if (combatTurns < 0) {
            throw new IllegalArgumentException(
                    "Cannot spent less than 0 turns.");
        }
        this.combatTurns = combatTurns;
    }

    /**
     * @return The number of combats encountered during this level.
     */
    public int getCombatTurns() {
        return this.combatTurns;
    }

    /**
     * @param noncombatTurns
     *            The number of noncombats encountered during this level to set.
     * @throws IllegalArgumentException
     *             if noncombatTurns is below 0
     */
    public void setNoncombatTurns(final int noncombatTurns) {
        if (noncombatTurns < 0) {
            throw new IllegalArgumentException(
                    "Cannot spent less than 0 turns.");
        }
        this.noncombatTurns = noncombatTurns;
    }

    /**
     * @return The number of noncombats encountered during this level.
     */
    public int getNoncombatTurns() {
        return this.noncombatTurns;
    }

    /**
     * @param otherTurns
     *            The number of adventures spent on other things (such as
     *            crafting) during this level to set.
     * @throws IllegalArgumentException
     *             if otherTurns is below 0
     */
    public void setOtherTurns(final int otherTurns) {
        if (otherTurns < 0) {
            throw new IllegalArgumentException(
                    "Cannot spent less than 0 turns.");
        }
        this.otherTurns = otherTurns;
    }

    /**
     * @return The number of adventures spent on other things (such as crafting)
     *         during this level.
     */
    public int getOtherTurns() {
        return this.otherTurns;
    }

    /**
     * @return The number of adventures spent during this level.
     */
    public int getTotalTurns() {
        return this.combatTurns + this.noncombatTurns + this.otherTurns;
    }

    /**
     * @param stats
     *            The stats when the level was reached to set.
     */
    public void setStatsAtLevelReached(final Statgain stats) {
        this.statsAtLevelReached = stats;
    }

    /**
     * @return The character stats when this level was reached.
     */
    public Statgain getStatsAtLevelReached() {
        return this.statsAtLevelReached;
    }

    /**
     * @param statGainPerTurn
     *            The mainstat gain per turn during this level to set.
     */
    public void setStatGainPerTurn(final double statGainPerTurn) {
        if (statGainPerTurn < 0) {
            throw new IllegalArgumentException(
                    "Stats per turn cannot be negative.");
        }
        this.statGainPerTurn = statGainPerTurn;
    }

    /**
     * @return The mainstat gain per turn during this level.
     */
    public double getStatGainPerTurn() {
        return this.statGainPerTurn;
    }

    /**
     * @return The difference between the level number of this LevelData
     *         instance and the level number of the given LevelData object.
     * @see Comparable
     */
    @Override
    public int compareTo(final LevelData ld) {
        return this.levelNumber - ld.getLevelNumber();
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(40);
        str.append(LevelData.HIT_LEVEL_START_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getLevelNumber());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(LevelData.HIT_LEVEL_MIDDLE_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getLevelReachedOnTurn());
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof LevelData) {
                final LevelData ld = (LevelData) o;
                return (ld.getLevelNumber() == this.levelNumber)
                        && (ld.getLevelReachedOnTurn() == this.levelReachedOnTurn)
                        && (ld.getCombatTurns() == this.combatTurns)
                        && (ld.getNoncombatTurns() == this.noncombatTurns)
                        && (ld.getOtherTurns() == this.otherTurns)
                        && this.statsAtLevelReached.equals(ld
                                .getStatsAtLevelReached());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 5555;
        result = (31 * result) + this.levelNumber;
        result = (31 * result) + this.levelReachedOnTurn;
        result = (31 * result) + this.combatTurns;
        result = (31 * result) + this.noncombatTurns;
        result = (31 * result) + this.otherTurns;
        result = (31 * result) + this.statsAtLevelReached.hashCode();
        return result;
    }
}
