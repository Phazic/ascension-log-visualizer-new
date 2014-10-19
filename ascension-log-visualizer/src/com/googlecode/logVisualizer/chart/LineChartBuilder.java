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

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public abstract class LineChartBuilder extends AbstractChart {
    /**
     * 
     */
    private static final long serialVersionUID = 5929161163715760050L;
    private final String xLable;
    private final String yLable;
    private double lastXValue;

    protected LineChartBuilder(final LogDataHolder logData, final String title,
            final String xLable, final String yLable,
            final boolean isIncludeLegend) {
        super(title, logData, isIncludeLegend);
        this.xLable = xLable;
        this.yLable = yLable;
        this.addChart();
    }

    protected abstract XYDataset createDataset();

    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYLineChart(
                this.getTitle(), this.xLable, this.yLable, dataset,
                PlotOrientation.VERTICAL, this.isIncludeLegend(), true, false);
        final XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(new FixedZoomNumberAxis());
        plot.getDomainAxis().setStandardTickUnits(
                NumberAxis.createIntegerTickUnits());
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);
        plot.setNoDataMessage("No data available");
        if (dataset.getSeriesCount() > 0) {
            this.lastXValue = dataset.getXValue(0, dataset.getItemCount(0) - 1);
            ((NumberAxis) plot.getDomainAxis()).setUpperBound(this.lastXValue);
        }
        ((NumberAxis) plot.getRangeAxis()).setStandardTickUnits(NumberAxis
                .createIntegerTickUnits());
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesStroke(i, new BasicStroke(2));
        }
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        plot.setRenderer(renderer);
        return chart;
    }

    @Override
    protected ChartPanel createChartPanel() {
        return new ChartPanel(this.createChart(this.createDataset()));
    }

    /**
     * A "hacked" NumberAxis class. The only difference is that it zooms out to
     * 0 to the maximum x value instead of simply turning autoRange on, which
     * gives less than desirable results in the form of blank unused space
     * behind the graphs.
     */
    private final class FixedZoomNumberAxis extends NumberAxis {
        /**
         * 
         */
        private static final long serialVersionUID = 454167166479334811L;

        FixedZoomNumberAxis() {
            super(null);
        }

        @Override
        public void resizeRange(final double percent, final double anchorValue) {
            if (percent > 0.0) {
                super.resizeRange(percent, anchorValue);
            } else {
                this.setRange(0, LineChartBuilder.this.lastXValue);
            }
        }
    }
}
