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
package com.googlecode.logVisualizer.logData.turn.turnAction;

import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * This immutable class is a representation of a player login snapshot as
 * KolMafia logs them whenever an account logs in. It holds the turn number of
 * when the login occurred and various data from within the snapshot.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class PlayerSnapshot extends AbstractTurnAction<PlayerSnapshot> {
    private final int musStats;
    private final int mystStats;
    private final int moxStats;
    private final int adventures;
    private final int meat;

    /**
     * @param playerStats
     *            The base player stats as listed in the player snapshot.
     * @param adventures
     *            The number of adventures left when the snapshot occurred.
     * @param meat
     *            The meat on hand when the snapshot occurred.
     * @param turnNumber
     *            The turn number on which this player snapshot happened.
     * @throws NullPointerException
     *             if playerStats is {@code null}.
     * @throws IllegalArgumentException
     *             if one of the stats is below 0; if turnNumber is below 0
     */
    public PlayerSnapshot(final Statgain playerStats, final int adventures,
            final int meat, final int turnNumber) {
        this(playerStats.mus, playerStats.myst, playerStats.mox, adventures,
                meat, turnNumber);
    }

    /**
     * @param musStats
     *            The base player muscle as listed in the player snapshot.
     * @param mystStats
     *            The base player mysticality as listed in the player snapshot.
     * @param moxStats
     *            The base player moxie as listed in the player snapshot.
     * @param adventures
     *            The number of adventures left when the snapshot occurred.
     * @param meat
     *            The meat on hand when the snapshot occurred.
     * @param turnNumber
     *            The turn number on which this player snapshot happened.
     * @throws IllegalArgumentException
     *             if any of the values is negative
     */
    public PlayerSnapshot(final int musStats, final int mystStats,
            final int moxStats, final int adventures, final int meat,
            final int turnNumber) {
        super(turnNumber);
        if (musStats < 0) {
            throw new IllegalArgumentException(
                    "Player stats cannot be below 0.");
        }
        if (mystStats < 0) {
            throw new IllegalArgumentException(
                    "Player stats cannot be below 0.");
        }
        if (moxStats < 0) {
            throw new IllegalArgumentException(
                    "Player stats cannot be below 0.");
        }
        if (adventures < 0) {
            throw new IllegalArgumentException(
                    "Adventures left cannot be below 0.");
        }
        if (meat < 0) {
            throw new IllegalArgumentException(
                    "Current meat cannot be below 0.");
        }
        this.musStats = musStats;
        this.mystStats = mystStats;
        this.moxStats = moxStats;
        this.adventures = adventures;
        this.meat = meat;
    }

    /**
     * @return The base player muscle as it was listed in the player snapshot.
     */
    public int getMuscleStats() {
        return this.musStats;
    }

    /**
     * @return The base player mysticality as it was listed in the player
     *         snapshot.
     */
    public int getMystStats() {
        return this.mystStats;
    }

    /**
     * @return The base player moxie as it was listed in the player snapshot.
     */
    public int getMoxieStats() {
        return this.moxStats;
    }

    /**
     * @return The number of adventures left when the snapshot occurred.
     */
    public int getAdventuresLeft() {
        return this.adventures;
    }

    /**
     * @return The meat on hand when the snapshot occurred.
     */
    public int getCurrentMeat() {
        return this.meat;
    }

    @Override
    public String toString() {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder str = new StringBuilder(60);
        str.append("Mus:");
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.musStats);
        str.append(newLine);
        str.append("Myst:");
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.mystStats);
        str.append(newLine);
        str.append("Mox:");
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.moxStats);
        str.append(newLine);
        str.append("Adventures left:");
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.adventures);
        str.append(newLine);
        str.append("Current meat:");
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.meat);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (super.equals(o) && (o instanceof PlayerSnapshot)) {
            return (((PlayerSnapshot) o).getMuscleStats() == this.musStats)
                    && (((PlayerSnapshot) o).getMystStats() == this.mystStats)
                    && (((PlayerSnapshot) o).getMoxieStats() == this.moxStats)
                    && (((PlayerSnapshot) o).getAdventuresLeft() == this.adventures)
                    && (((PlayerSnapshot) o).getCurrentMeat() == this.meat);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 2334;
        result = (31 * result) + super.hashCode();
        result = (31 * result) + this.musStats;
        result = (31 * result) + this.mystStats;
        result = (31 * result) + this.moxStats;
        result = (31 * result) + this.adventures;
        result = (31 * result) + this.meat;
        return result;
    }
}
