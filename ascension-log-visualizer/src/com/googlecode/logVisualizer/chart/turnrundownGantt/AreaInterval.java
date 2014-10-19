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
package com.googlecode.logVisualizer.chart.turnrundownGantt;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.logVisualizer.logData.turn.TurnInterval;

public final class AreaInterval {
    private final String name;
    private int startTurn;
    private int endTurn;
    private List<TurnInterval> subIntervals = new ArrayList<>();

    public AreaInterval(final TurnInterval area, final String areaIntervalName) {
        this.name = areaIntervalName;
        this.startTurn = area.getStartTurn();
        this.endTurn = area.getEndTurn();
        this.subIntervals.add(area);
    }

    public AreaInterval(final String name, final int startTurn,
            final int endTurn) {
        this(new TurnInterval(name, startTurn, endTurn), name);
    }

    public String getName() {
        return this.name;
    }

    public int getStartTurn() {
        return this.startTurn;
    }

    public int getEndTurn() {
        return this.endTurn;
    }

    public void addSubInterval(final TurnInterval interval) {
        if (interval.getStartTurn() < this.startTurn) {
            this.startTurn = interval.getStartTurn();
        }
        if (interval.getEndTurn() > this.endTurn) {
            this.endTurn = interval.getEndTurn();
        }
        this.subIntervals.add(interval);
    }

    public TurnInterval getSubInterval(final int index) {
        return (index < this.subIntervals.size()) && (index >= 0) ? this.subIntervals
                .get(index) : null;
    }

    public void setSubIntervals(final List<TurnInterval> subIntervals) {
        this.subIntervals = subIntervals;
    }

    public List<TurnInterval> getSubIntervals() {
        return this.subIntervals;
    }

    @Override
    public boolean equals(final Object o) {
        if ((o != null) && (o instanceof AreaInterval)) {
            return (this.startTurn == ((AreaInterval) o).getStartTurn())
                    && (this.endTurn == ((AreaInterval) o).getEndTurn())
                    && this.name.equals(((AreaInterval) o).getName())
                    && this.subIntervals.equals(((AreaInterval) o)
                            .getSubIntervals());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 3242;
        result = (31 * result) + this.startTurn;
        result = (31 * result) + this.endTurn;
        result = (31 * result) + this.name.hashCode();
        result = (31 * result) + this.subIntervals.hashCode();
        return result;
    }
}
