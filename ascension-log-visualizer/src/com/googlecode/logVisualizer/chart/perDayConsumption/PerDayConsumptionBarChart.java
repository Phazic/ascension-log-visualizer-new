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
package com.googlecode.logVisualizer.chart.perDayConsumption;

import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.logSummary.ConsumptionSummary.ConsumptionDayStats;
import com.googlecode.logVisualizer.util.DataTablesHandler;

final class PerDayConsumptionBarChart extends ConsumptionBarChartBuilder {
    /**
     *
     */
    private static final long serialVersionUID = -3836830057108140815L;
    private final ConsumptionDayStats consumptionStats;

    PerDayConsumptionBarChart(final ConsumptionDayStats consumptionStats) {
        super("Organ hit per consumable", "Consumable version",
                "Organ hit per consumable", false);
        this.consumptionStats = consumptionStats;
        this.addChart();
    }

    @Override
    protected ConsumptionDataset createDataset() {
        final ConsumptionDataset dataset = new ConsumptionDataset(
                this.consumptionStats);
        for (final Consumable c : this.consumptionStats.getConsumablesUsed()) {
            if ((DataTablesHandler.getFullnessHit(c.getName()) > 0)
                    || (DataTablesHandler.getDrunkennessHit(c.getName()) > 0)
                    || (DataTablesHandler.getSpleenHit(c.getName()) > 0)) {
                dataset.addConsumable(c);
            }
        }
        dataset.addLeftoverOrganHits(this.consumptionStats);
        return dataset;
    }
}
