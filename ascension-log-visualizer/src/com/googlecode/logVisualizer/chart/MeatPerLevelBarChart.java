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
import com.googlecode.logVisualizer.logData.MeatGain;
import com.googlecode.logVisualizer.util.DataNumberPair;

public final class MeatPerLevelBarChart extends
        HorizontalStackedBarChartBuilder {
    /**
     *
     */
    private static final long serialVersionUID = -4681689964988665309L;

    public MeatPerLevelBarChart(final LogDataHolder logData) {
        super(logData, "Meat gained/spent per level", "Level", "Meat", true);
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (final DataNumberPair<MeatGain> dnp : this.getLogData()
                .getLogSummary().getMeatSummary().getAllLevelsMeatData()) {
            final String levelStr = "Level " + dnp.getNumber();
            dataset.addValue(dnp.getData().encounterMeatGain,
                    "Meat gained inside encounters", levelStr);
            dataset.addValue(dnp.getData().otherMeatGain,
                    "Meat gained outside encounters", levelStr);
            dataset.addValue(dnp.getData().meatSpent, "Meat spent", levelStr);
        }
        return dataset;
    }
}
