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
package com.googlecode.logVisualizer.gui.notetaker;

import com.googlecode.logVisualizer.logData.turn.TurnInterval;

/**
 * This container class contains a {@link TurnInterval} and the textual
 * print-out of it as they are seen in textual ascension logs.
 */
final class TurnIntervalContainer {
    private final TurnInterval ti;
    private final String turnIntervalPrintout;

    /**
     * @param ti
     *            The turn interval.
     * @param turnIntervalPrintout
     *            The matching textual print-out of the turn interval.
     */
    TurnIntervalContainer(final TurnInterval ti,
            final String turnIntervalPrintout) {
        if (ti == null) {
            throw new IllegalArgumentException(
                    "The turn interval must not be null.");
        }
        if (turnIntervalPrintout == null) {
            throw new IllegalArgumentException(
                    "The turn interval print-out must not be null.");
        }
        this.ti = ti;
        this.turnIntervalPrintout = turnIntervalPrintout;
    }

    /**
     * Returns the turn interval.
     * <p>
     * Please note, that even though the turn interval is mutable, you should
     * not alter it through this method in any way.
     *
     * @return The turn interval.
     */
    TurnInterval getTurnInterval() {
        return this.ti;
    }

    /**
     * @return The textual turn interval print-out.
     */
    String getTurnIntervalPrintout() {
        return this.turnIntervalPrintout;
    }

    /**
     * Sets the notes attached to the turn interval contained in this instance.
     *
     * @param notes
     *            The notes that should be attached to the turn interval.
     */
    void setNotes(final String notes) {
        this.ti.setNotes(notes);
    }

    /**
     * @return The notes attached to the turn interval contained in this
     *         instance.
     */
    String getNotes() {
        return this.ti.getNotes();
    }

    @Override
    public String toString() {
        return this.turnIntervalPrintout;
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof TurnIntervalContainer) {
                return this.ti.equals(((TurnIntervalContainer) o)
                        .getTurnInterval())
                        && this.turnIntervalPrintout
                                .equals(((TurnIntervalContainer) o)
                                        .getTurnIntervalPrintout());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 12;
        result = (31 * result) + this.ti.hashCode();
        result = (31 * result) + this.turnIntervalPrintout.hashCode();
        return result;
    }
}
