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

import java.awt.Paint;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public final class TotalConsumptionPie extends PieChartBuilder {
    /**
     *
     */
    private static final long serialVersionUID = -7017996531343725189L;
    private final static Paint[] DEFAULT_COLORS = ChartColor
            .createDefaultPaintArray();

    public TotalConsumptionPie(final LogDataHolder logData) {
        super("Turns got from ...", logData, false);
    }

    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final PiePlot plot = (PiePlot) panel.getChart().getPlot();
        plot.setSectionPaint("Eating", TotalConsumptionPie.DEFAULT_COLORS[0]);
        plot.setSectionPaint("Drinking", TotalConsumptionPie.DEFAULT_COLORS[1]);
        plot.setSectionPaint("Other", TotalConsumptionPie.DEFAULT_COLORS[2]);
        plot.setSectionPaint("Rollover", TotalConsumptionPie.DEFAULT_COLORS[3]);
        return panel;
    }

    @Override
    protected PieDataset createDataset() {
        final DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Eating", this.getLogData().getLogSummary()
                .getTotalTurnsFromFood());
        dataset.setValue("Drinking", this.getLogData().getLogSummary()
                .getTotalTurnsFromBooze());
        dataset.setValue("Other", this.getLogData().getLogSummary()
                .getTotalTurnsFromOther());
        dataset.setValue("Rollover", this.getLogData().getLogSummary()
                .getTotalTurnsFromRollover());
        return dataset;
    }
}
