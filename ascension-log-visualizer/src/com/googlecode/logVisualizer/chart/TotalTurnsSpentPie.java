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
package com.googlecode.logVisualizer.chart;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public final class TotalTurnsSpentPie extends PieChartBuilder {
    /**
     *
     */
    private static final long serialVersionUID = -1434238900693647366L;

    public TotalTurnsSpentPie(final LogDataHolder logData) {
        super("Total turns spent", logData, false);
    }

    @Override
    protected PieDataset createDataset() {
        final DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Combats", this.getLogData().getLogSummary()
                .getTotalTurnsCombat());
        dataset.setValue("Noncombats", this.getLogData().getLogSummary()
                .getTotalTurnsNoncombat());
        dataset.setValue("Other", this.getLogData().getLogSummary()
                .getTotalTurnsOther());
        return dataset;
    }
}
