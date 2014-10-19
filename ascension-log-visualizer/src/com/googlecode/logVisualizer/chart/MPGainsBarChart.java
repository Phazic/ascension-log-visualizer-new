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

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public final class MPGainsBarChart extends HorizontalBarChartBuilder {
    /**
     * 
     */
    private static final long serialVersionUID = 523839757049071039L;

    public MPGainsBarChart(final LogDataHolder logData) {
        super(logData, "MP gains", "Category", "MP gain", false);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final String seriesName = "MP gains";
        dataset.addValue(this.getLogData().getLogSummary().getMPGains()
                .getEncounterMPGain(), seriesName, "Inside Encounters");
        dataset.addValue(this.getLogData().getLogSummary().getMPGains()
                .getStarfishMPGain(), seriesName, "Starfish Familiars");
        dataset.addValue(this.getLogData().getLogSummary().getMPGains()
                .getRestingMPGain(), seriesName, "Resting");
        dataset.addValue(this.getLogData().getLogSummary().getMPGains()
                .getOutOfEncounterMPGain(), seriesName, "Outside Encounters");
        dataset.addValue(this.getLogData().getLogSummary().getMPGains()
                .getConsumableMPGain(), seriesName, "Consumables");
        return dataset;
    }
}
