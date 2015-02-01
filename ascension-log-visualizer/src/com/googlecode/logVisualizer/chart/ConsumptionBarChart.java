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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.consumables.Consumable;

public final class ConsumptionBarChart extends HorizontalStackedBarChartBuilder {
    /**
     *
     */
    private static final long serialVersionUID = 4858869713775012904L;

    public ConsumptionBarChart(final LogDataHolder logData) {
        super(logData, "Turns gained per consumable", "Consumable",
                "Adventures gained", true);
    }

    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final CategoryPlot plot = (CategoryPlot) panel.getChart().getPlot();
        final StackedBarRenderer renderer = (StackedBarRenderer) plot
                .getRenderer();
        final CategoryDataset dataset = plot.getDataset();
        for (int i = 0; i < dataset.getRowCount(); i++) {
            if (dataset.getRowKey(i).equals("Food")) {
                renderer.setSeriesPaint(i, new Color(255, 80, 80));
            } else if (dataset.getRowKey(i).equals("Booze")) {
                renderer.setSeriesPaint(i, new Color(100, 100, 255));
            } else if (dataset.getRowKey(i).equals("Spleen")) {
                renderer.setSeriesPaint(i, new Color(80, 255, 80));
            } else if (dataset.getRowKey(i).equals("Other")) {
                renderer.setSeriesPaint(i, Color.LIGHT_GRAY);
            }
        }
        return panel;
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final List<Consumable> consumables = new ArrayList<>(this.getLogData()
                .getAllConsumablesUsed().size());
        for (final Consumable c : this.getLogData().getAllConsumablesUsed()) {
            if (c.getAdventureGain() > 0) {
                consumables.add(c);
            }
        }
        // Sort consumables from highest to lowest adventure gain.
        Collections.sort(consumables, new Comparator<Consumable>() {
            @Override
            public int compare(final Consumable o1, final Consumable o2) {
                return o2.getAdventureGain() - o1.getAdventureGain();
            }
        });
        // Add consumables to the dataset. Differentiate between the consumable
        // versions.
        for (final Consumable c : consumables) {
            switch (c.getConsumableVersion()) {
            case FOOD:
                dataset.addValue(c.getAdventureGain(), "Food", c.getName()
                        + " (" + c.getAmount() + ")");
                break;
            case BOOZE:
                dataset.addValue(c.getAdventureGain(), "Booze", c.getName()
                        + " (" + c.getAmount() + ")");
                break;
            case SPLEEN:
                dataset.addValue(c.getAdventureGain(), "Spleen", c.getName()
                        + " (" + c.getAmount() + ")");
                break;
            default:
                dataset.addValue(c.getAdventureGain(), "Other", c.getName()
                        + " (" + c.getAmount() + ")");
            }
        }
        return dataset;
    }
}
