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
package com.googlecode.logVisualizer.gui;

import java.awt.CardLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.googlecode.logVisualizer.chart.ConsumptionBarChart;
import com.googlecode.logVisualizer.chart.FamiliarUsagePie;
import com.googlecode.logVisualizer.chart.MPGainsBarChart;
import com.googlecode.logVisualizer.chart.MeatPerLevelBarChart;
import com.googlecode.logVisualizer.chart.QuestTurnsBarChart;
import com.googlecode.logVisualizer.chart.SkillCastsBarChart;
import com.googlecode.logVisualizer.chart.SkillMPCostBarChart;
import com.googlecode.logVisualizer.chart.StatGiverBarChart;
import com.googlecode.logVisualizer.chart.StatsPerAreaBarChart;
import com.googlecode.logVisualizer.chart.StatsPerTurnPerLevelBarChart;
import com.googlecode.logVisualizer.chart.TotalConsumptionPie;
import com.googlecode.logVisualizer.chart.TotalTurnsSpentPie;
import com.googlecode.logVisualizer.chart.TurnsSpentPerAreaBarChart;
import com.googlecode.logVisualizer.chart.TurnsSpentPerLevelBarChart;
import com.googlecode.logVisualizer.chart.perDayConsumption.PerDayConsumptionBarCharts;
import com.googlecode.logVisualizer.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator.TextualLogVersion;

public final class LogGUI extends JSplitPane {
    /**
     *
     */
    private static final long serialVersionUID = -7812320895914942781L;
    private static final String[] LIST_MENU_ITEM_NAMES = {
            "Turn rundown gantt", "Total turns spent", "Turns spent per area",
            "Turns spent per level", "Turns got from ...",
            "Turns per consumable", "Per day consumable stats",
            "Familiar usage", "Skills cast", "Skill MP costs", "MP gains",
            "Meat gained/spent", "Pulls", "Stat gains overview",
            "Stats per area", "Stats per turn per level", "Stat development",
            "Quest Turns", "Misc", "Ascension log" };
    private final GanttPanelGUI ganttPanel;
    private final LogDataHolder logData;

    /**
     * @param log
     *            The file holding the ascension log.
     * @param logData
     *            The {@link LogDataHolder} with all the data of the given
     *            ascension log.
     * @param isTextLogFromFile
     *            A flag denoting whether the textual ascension log already
     *            exists in the given file. (should only be true if the file
     *            holds a pre-parsed ascension log)
     */
    public LogGUI(final File log, final LogDataHolder logData,
            final boolean isTextLogFromFile) {
        super();
        if (log == null) {
            throw new NullPointerException("Log must not be null.");
        }
        if (logData == null) {
            throw new NullPointerException("Log data must not be null.");
        }
        this.ganttPanel = new GanttPanelGUI(logData);
        this.logData = logData;
        final JPanel chartArea = new JPanel(new CardLayout());
        final JList<String> navigation = new JList<>();
        chartArea.add(this.ganttPanel, LogGUI.LIST_MENU_ITEM_NAMES[0]);
        chartArea.add(new TotalTurnsSpentPie(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[1]);
        chartArea.add(new TurnsSpentPerAreaBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[2]);
        chartArea.add(new TurnsSpentPerLevelBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[3]);
        chartArea.add(new TotalConsumptionPie(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[4]);
        chartArea.add(new ConsumptionBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[5]);
        chartArea.add(new PerDayConsumptionBarCharts(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[6]);
        chartArea.add(new FamiliarUsagePie(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[7]);
        chartArea.add(new SkillCastsBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[8]);
        chartArea.add(new SkillMPCostBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[9]);
        chartArea.add(new MPGainsBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[10]);
        chartArea.add(new MeatPerLevelBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[11]);
        chartArea.add(new PullsPanel(logData), LogGUI.LIST_MENU_ITEM_NAMES[12]);
        chartArea.add(new StatGiverBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[13]);
        chartArea.add(new StatsPerAreaBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[14]);
        chartArea.add(new StatsPerTurnPerLevelBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[15]);
        chartArea.add(new StatDevelopmentPanelGUI(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[16]);
        chartArea.add(new QuestTurnsBarChart(logData),
                LogGUI.LIST_MENU_ITEM_NAMES[17]);
        chartArea.add(new MiscPanel(logData), LogGUI.LIST_MENU_ITEM_NAMES[18]);
        if (isTextLogFromFile) {
            chartArea.add(new JScrollPane(new LogViewer(log)),
                    LogGUI.LIST_MENU_ITEM_NAMES[19]);
        } else {
            final JTextArea logArea = new JTextArea();
            logArea.append(TextLogCreator.getTextualLog(logData,
                    TextualLogVersion.TEXT_LOG));
            logArea.setCaretPosition(0);
            chartArea.add(new JScrollPane(logArea),
                    LogGUI.LIST_MENU_ITEM_NAMES[19]);
        }
        navigation.setModel(new AbstractListModel<String>() {
            /**
             *
             */
            private static final long serialVersionUID = -5733605020343410165L;

            @Override
            public int getSize() {
                return LogGUI.LIST_MENU_ITEM_NAMES.length;
            }

            @Override
            public String getElementAt(final int i) {
                return LogGUI.LIST_MENU_ITEM_NAMES[i];
            }
        });
        navigation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navigation.setSelectedIndex(0);
        navigation.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    ((CardLayout) chartArea.getLayout()).show(chartArea,
                            navigation.getSelectedValue().toString());
                }
            }
        });
        this.setRightComponent(chartArea);
        this.setLeftComponent(new JScrollPane(navigation));
        this.setDividerLocation(180);
    }

    /**
     * @return The data of the ascension log.
     */
    public LogDataHolder getLogData() {
        return this.logData;
    }

    /**
     * @return The name of the ascension log.
     */
    public String getLogName() {
        return this.logData.getLogName();
    }

    /**
     * @param gpbl
     *            The button listener for the {@link GanttPanelGUI} to set.
     */
    public void setGanttPanelButtonListener(final GanttPaneButtonListener gpbl) {
        this.ganttPanel.setButtonListener(gpbl);
    }

    /**
     * A simple JTextArea that holds the whole contents of the given log file.
     */
    private final class LogViewer extends JTextArea {
        /**
         *
         */
        private static final long serialVersionUID = -2830191442670556878L;
        private static final String NEW_LINE = "\n";

        LogViewer(final File log) {
            super();
            try (final FileInputStream fis = new FileInputStream(log);
                    final BufferedReader br = new BufferedReader(
                            new InputStreamReader(fis))) {
                final StringBuilder tmpLines = new StringBuilder(40000);
                String tmpLine;
                while ((tmpLine = br.readLine()) != null) {
                    tmpLines.append(tmpLine);
                    tmpLines.append(LogViewer.NEW_LINE);
                    final int tmpLinesLength = tmpLines.length();
                    if (tmpLinesLength > 39500) {
                        this.append(tmpLines.toString());
                        tmpLines.delete(0, tmpLinesLength);
                    }
                }
                if (tmpLines.length() != 0) {
                    this.append(tmpLines.toString());
                }
                br.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            this.setCaretPosition(0);
        }
    }

    /**
     * A listener for all gantt chart configurators.
     */
    public interface GanttPaneButtonListener {
        /**
         * This method will fire if the button for the area category customizer
         * has been pressed.
         *
         * @param turnrundownChart
         *            The turnrundown gantt chart on which certain actions can
         *            be performed.
         */
        public void areaCategoryCustomizerPressed(
                TurnrundownGantt turnrundownChart);

        /**
         * This method will fire if the button for the familiar color customizer
         * has been pressed.
         *
         * @param turnrundownChart
         *            The turnrundown gantt chart on which certain actions can
         *            be performed.
         */
        public void familiarColorizerPressed(TurnrundownGantt turnrundownChart);
    }
}
