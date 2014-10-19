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

import java.util.ArrayList;
import java.util.List;

/**
 * A data container for data concerning the Sewer, specifically the RNG of
 * trinket dropping.
 */
public final class Sewer {
    private int turnsSpent;
    private int trinketsFound;
    private final List<Integer> trinketsTurnNumbers = new ArrayList<>();

    Sewer() {
    }

    /**
     * @param turnsSpent
     *            The number of turns spent in the sewer to set.
     * @throws IllegalArgumentException
     *             if turnsSpent is below 0
     */
    public void setTurnsSpent(final int turnsSpent) {
        if (turnsSpent < 0) {
            throw new IllegalArgumentException(
                    "You cannot spent less than 0 turns somewhere.");
        }
        this.turnsSpent = turnsSpent;
    }

    /**
     * @return The number of turns spent in the sewer.
     */
    public int getTurnsSpent() {
        return this.turnsSpent;
    }

    /**
     * @param trinketsFound
     *            The number of trinkets found to set.
     * @throws IllegalArgumentException
     *             if trinketsFound is below 0
     */
    public void setTrinketsFound(final int trinketsFound) {
        if (trinketsFound < 0) {
            throw new IllegalArgumentException(
                    "You cannot find less than 0 trinkets.");
        }
        this.trinketsFound = trinketsFound;
    }

    /**
     * @return The number of trinkets found.
     */
    public int getTrinketsFound() {
        return this.trinketsFound;
    }

    /**
     * @param turnNumber
     *            The turn number of when a trinket was found to add.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     */
    public void addTrinketsTurnNumber(final int turnNumber) {
        if (turnNumber < 0) {
            throw new IllegalArgumentException(
                    "There are no negative turn numbers.");
        }
        this.trinketsTurnNumbers.add(turnNumber);
    }

    /**
     * @return The list of turns on which trinkets were found.
     */
    public List<Integer> getTrinketsTurnNumbers() {
        return this.trinketsTurnNumbers;
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof Sewer) {
                return (((Sewer) o).getTurnsSpent() == this.turnsSpent)
                        && (((Sewer) o).getTrinketsFound() == this.trinketsFound)
                        && this.trinketsTurnNumbers.equals(((Sewer) o)
                                .getTrinketsTurnNumbers());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 333;
        result = (31 * result) + this.turnsSpent;
        result = (31 * result) + this.trinketsFound;
        result = (31 * result) + this.trinketsTurnNumbers.hashCode();
        return result;
    }
}
