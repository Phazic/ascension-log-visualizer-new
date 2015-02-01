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

import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.AIRSHIP;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.BALLROOM;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.BAT;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.BLACK_FOREST;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.CASTLE;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.CHASM;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.CYRPT;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.DESERT_OASIS;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.DOD;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.FRIARS;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.HIDDEN_CITY;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.KNOB;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.MOSQUITO;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.PALINDOME;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.PIRATE;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.PYRAMID;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.SPOOKYRAVEN;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.STARTING_WAR;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.TAVERN;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.TEMPLE;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.TRAPZOR;
import static com.googlecode.logVisualizer.chart.QuestTurnsBarChart.QuestAreas.WAR;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public final class QuestTurnsBarChart extends HorizontalBarChartBuilder {
    /**
     *
     */
    private static final long serialVersionUID = 8427013435620588366L;
    private static final String QUEST_TURNS_STRING = "Quest turns";

    public QuestTurnsBarChart(final LogDataHolder logData) {
        super(logData, QuestTurnsBarChart.QUEST_TURNS_STRING, "Area",
                "Turns used", false);
    }

    @Override
    protected ChartPanel createChartPanel() {
        final ChartPanel panel = super.createChartPanel();
        final JFreeChart chart = panel.getChart();
        final CategoryDataset dataset = ((CategoryPlot) chart.getPlot())
                .getDataset();
        final NumberAxis numberAxis = (NumberAxis) ((CategoryPlot) chart
                .getPlot()).getRangeAxis();
        int maxTurncount = 0;
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            if (dataset.getValue(0, i).intValue() > maxTurncount) {
                maxTurncount = dataset.getValue(0, i).intValue();
            }
        }
        // Use hard ranges to make comparison between different logs easier, but
        // don't cut off the bars if some are too long.
        numberAxis.setAutoRange(false);
        numberAxis.setLowerBound(0);
        numberAxis.setUpperBound(maxTurncount <= 125 ? 125 : maxTurncount + 10);
        return panel;
    }

    @Override
    protected CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        QuestTurnsBarChart.addDatasetValue(MOSQUITO, this.getLogData()
                .getLogSummary().getQuestTurncounts().getMosquitoQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(TEMPLE, this.getLogData()
                .getLogSummary().getQuestTurncounts().getTempleOpeningTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(TAVERN, this.getLogData()
                .getLogSummary().getQuestTurncounts().getTavernQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(BAT, this.getLogData()
                .getLogSummary().getQuestTurncounts().getBatQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(KNOB, this.getLogData()
                .getLogSummary().getQuestTurncounts().getKnobQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(FRIARS, this.getLogData()
                .getLogSummary().getQuestTurncounts().getFriarsQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(CYRPT, this.getLogData()
                .getLogSummary().getQuestTurncounts().getCyrptQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(TRAPZOR, this.getLogData()
                .getLogSummary().getQuestTurncounts().getTrapzorQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(CHASM, this.getLogData()
                .getLogSummary().getQuestTurncounts().getChasmQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(AIRSHIP, this.getLogData()
                .getLogSummary().getQuestTurncounts().getAirshipQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(CASTLE, this.getLogData()
                .getLogSummary().getQuestTurncounts().getCastleQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(BALLROOM,
                this.getLogData().getLogSummary().getQuestTurncounts()
                        .getBallroomOpeningTurns(), dataset);
        QuestTurnsBarChart.addDatasetValue(PIRATE, this.getLogData()
                .getLogSummary().getQuestTurncounts().getPirateQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(BLACK_FOREST, this.getLogData()
                .getLogSummary().getQuestTurncounts()
                .getBlackForrestQuestTurns(), dataset);
        QuestTurnsBarChart.addDatasetValue(DESERT_OASIS, this.getLogData()
                .getLogSummary().getQuestTurncounts()
                .getDesertOasisQuestTurns(), dataset);
        QuestTurnsBarChart.addDatasetValue(SPOOKYRAVEN, this.getLogData()
                .getLogSummary().getQuestTurncounts()
                .getSpookyravenQuestTurns(), dataset);
        QuestTurnsBarChart.addDatasetValue(HIDDEN_CITY,
                this.getLogData().getLogSummary().getQuestTurncounts()
                        .getTempleCityQuestTurns(), dataset);
        QuestTurnsBarChart.addDatasetValue(PALINDOME, this.getLogData()
                .getLogSummary().getQuestTurncounts().getPalindomeQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(PYRAMID, this.getLogData()
                .getLogSummary().getQuestTurncounts().getPyramidQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(STARTING_WAR, this.getLogData()
                .getLogSummary().getQuestTurncounts()
                .getWarIslandOpeningTurns(), dataset);
        QuestTurnsBarChart.addDatasetValue(WAR, this.getLogData()
                .getLogSummary().getQuestTurncounts().getWarIslandQuestTurns(),
                dataset);
        QuestTurnsBarChart.addDatasetValue(DOD, this.getLogData()
                .getLogSummary().getQuestTurncounts().getDodQuestTurns(),
                dataset);
        return dataset;
    }

    /**
     * A helper method to make adding data to the dataset easier.
     *
     * @param area
     *            The enum related to the given turncount.
     * @param turncount
     *            The number of turns a certain quest actually took.
     * @param dataset
     *            The dataset to which the should be added to.
     */
    private static void addDatasetValue(final QuestAreas area,
            final int turncount, final DefaultCategoryDataset dataset) {
        dataset.addValue(turncount, QuestTurnsBarChart.QUEST_TURNS_STRING,
                area.getDescription());
    }

    /**
     * An enum for all the names of the quest areas.
     */
    static enum QuestAreas {
        MOSQUITO("Mosquito Larva"), TEMPLE("Opening the Hidden Temple"), TAVERN(
                "Tavern quest"), BAT("Bat quest"), KNOB("Cobb's Knob quest"), FRIARS(
                "Friars' part 1"), CYRPT("Defiled Cyrpt quest"), TRAPZOR(
                "Trapzor quest"), CHASM("Orc Chasm quest"), AIRSHIP("Airship"), CASTLE(
                "Giant's Castle"), BALLROOM("Opening the Ballroom"), PIRATE(
                "Pirate quest"), BLACK_FOREST("Black Forest quest"), DESERT_OASIS(
                "Desert Oasis quest"), SPOOKYRAVEN("Spookyraven quest"), HIDDEN_CITY(
                "Hidden City quest"), PALINDOME("Palindome quest"), PYRAMID(
                "Pyramid quest"), STARTING_WAR("Starting the War"), WAR(
                "War Island quest"), DOD("DoD quest");
        private final String description;

        private QuestAreas(final String description) {
            this.description = description;
        }

        String getDescription() {
            return this.description;
        }
    }
}
