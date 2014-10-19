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
package com.googlecode.logVisualizer;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.logVisualizer.gui.FamiliarUsageCustomizer;
import com.googlecode.logVisualizer.gui.InternalMafiaLogParserDialog;
import com.googlecode.logVisualizer.gui.LocationCategoryCustomizer;
import com.googlecode.logVisualizer.gui.LogGUI;
import com.googlecode.logVisualizer.gui.LogGUI.GanttPaneButtonListener;
import com.googlecode.logVisualizer.gui.LogVisualizerGUI;
import com.googlecode.logVisualizer.gui.LogVisualizerGUI.LogLoaderListener;
import com.googlecode.logVisualizer.parser.LogParser;
import com.googlecode.logVisualizer.parser.LogsCreator;
import com.googlecode.logVisualizer.parser.MafiaLogParser;
import com.googlecode.logVisualizer.parser.PreparsedLogParser;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator.TextualLogVersion;
import com.puttysoftware.updaterx.ProductData;
import com.puttysoftware.updaterx.UpdateChecker;

public final class LogVisualizer {
    static {
        // Create data directories if they do not exist.
        if (!UtilityConstants.ROOT_LOCATION.exists()) {
            UtilityConstants.ROOT_LOCATION.mkdir();
        }
        if (!UtilityConstants.TEMP_LOCATION.exists()) {
            UtilityConstants.TEMP_LOCATION.mkdir();
        }
        if (!UtilityConstants.DATA_LOCATION.exists()) {
            UtilityConstants.DATA_LOCATION.mkdir();
        }
        if (!UtilityConstants.KOL_DATA_LOCATION.exists()) {
            UtilityConstants.KOL_DATA_LOCATION.mkdir();
        }
        // Delete all files in the temporary directory. Ignore subdirectories.
        for (final File f : UtilityConstants.TEMP_LOCATION.listFiles()) {
            if (!f.isDirectory()) {
                f.delete();
            }
        }
        LogVisualizer.writeDataFilesToFileSystem();
        // Create normal data files if they do not exist.
        final List<File> normalDataFiles = new ArrayList<>();
        normalDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.DATA_DIRECTORY
                + "standardView.cvw"));
        for (final File f : normalDataFiles) {
            if (!f.exists()) {
                String tmpLine;
                try (final BufferedReader br = DataUtilities.getReader(
                        UtilityConstants.DATA_DIRECTORY, f.getName())) {
                    f.createNewFile();
                    try (final PrintWriter fileWriter = new PrintWriter(f)) {
                        while ((tmpLine = br.readLine()) != null) {
                            fileWriter.println(tmpLine);
                        }
                        fileWriter.close();
                    }
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Set chart theme back to what it looked like before JFreeChart 1.0.11.
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
    }
    private final LogVisualizerGUI gui;

    LogVisualizer() {
        try {
            final String wantedLaf = Settings.getSettingString("LookAndFeel");
            LookAndFeelInfo usedLaf = null;
            for (final LookAndFeelInfo lafi : UIManager
                    .getInstalledLookAndFeels()) {
                if (lafi.getName().equals(wantedLaf)) {
                    usedLaf = lafi;
                    break;
                }
            }
            if (usedLaf != null) {
                UIManager.setLookAndFeel(usedLaf.getClassName());
            } else {
                UIManager.setLookAndFeel(UIManager
                        .getCrossPlatformLookAndFeelClassName());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        this.gui = new LogVisualizerGUI(new LogLoaderListener() {
            @Override
            public void loadMafiaLog(final File file) {
                LogVisualizer.this.loadLog(file, new MafiaLogParser(file,
                        Settings.getSettingBoolean("Include mafia log notes")));
            }

            @Override
            public void loadPreparsedLog(final File file) {
                LogVisualizer.this.loadLog(file, new PreparsedLogParser(file));
            }
        });
        this.gui.setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this.gui);
        this.gui.setVisible(true);
        if (Settings.getSettingBoolean("Check Updates")) {
            // Initialize Strings
            String UPDATE_SITE = "https://github.com/wrldwzrd89/ascension-log-visualizer-new/raw/master/ascension-log-visualizer/updater/";
            String PRODUCT_NAME = "ascension-log-visualizer";
            String COMPANY_NAME = "Putty Software";
            String RDNS_COMPANY_NAME = "com.puttysoftware.alv";
            int VERSION_MAJOR = 3;
            int VERSION_MINOR = 0;
            int VERSION_BUGFIX = 0;
            int VERSION_CODE = ProductData.CODE_BETA;
            int VERSION_BETA = 1;
            // Initialize update checkers
            ProductData rpd = new ProductData(UPDATE_SITE, UPDATE_SITE,
                    RDNS_COMPANY_NAME, COMPANY_NAME, PRODUCT_NAME,
                    VERSION_MAJOR, VERSION_MINOR, VERSION_BUGFIX,
                    ProductData.CODE_STABLE, 0);
            ProductData bpd = new ProductData(UPDATE_SITE, UPDATE_SITE,
                    RDNS_COMPANY_NAME, COMPANY_NAME, PRODUCT_NAME,
                    VERSION_MAJOR, VERSION_MINOR, VERSION_BUGFIX,
                    ProductData.CODE_BETA, VERSION_BETA);
            UpdateChecker bUpdater = new UpdateChecker(bpd);
            UpdateChecker rUpdater = new UpdateChecker(rpd);
            // Check for Updates in the background
            final Thread updateCheck = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (VERSION_CODE != ProductData.CODE_STABLE) {
                        bUpdater.checkForUpdatesAtStartup();
                        rUpdater.checkForUpdatesAtStartup();
                    } else {
                        rUpdater.checkForUpdatesAtStartup();
                    }
                }
            });
            updateCheck.start();
        }
    }

    void loadLog(final File file, final LogParser parser) {
        final long t = System.currentTimeMillis();
        try {
            parser.parse();
            this.addLogGUI(file, parser);
        } catch (final IOException e) {
            // If there was an IO error of some kind while reading the
            // log file, print the stack trace and show an error dialog,
            // so the user actually knows something is up.
            e.printStackTrace();
            JOptionPane
                    .showMessageDialog(
                            null,
                            "There was a problem with reading and/or parsing the ascension log.\n\n"
                                    + "Make sure that the ascension log file isn't corrupted or doesn't contain any line longer than 500 characters\n"
                                    + "and try again. If it still doesn't work, try contacting the developers of the Ascension Log Visualizer for\n"
                                    + "further help.", "Problem occurred",
                            JOptionPane.ERROR_MESSAGE);
        }
        System.out.println((System.currentTimeMillis() - t));
    }

    /**
     * Adds a {@link LogGUI} with the given logData to the log pane.
     */
    private synchronized void addLogGUI(final File log, final LogParser parser) {
        final LogGUI logGUI = new LogGUI(log, parser.getLogData(),
                !parser.isDetailedLogData());
        logGUI.setGanttPanelButtonListener(new GanttPaneButtonListener() {
            @Override
            public void areaCategoryCustomizerPressed(
                    final TurnrundownGantt turnrundownChart) {
                new LocationCategoryCustomizer(LogVisualizer.this.gui,
                        turnrundownChart);
            }

            @Override
            public void familiarColorizerPressed(
                    final TurnrundownGantt turnrundownChart) {
                new FamiliarUsageCustomizer(LogVisualizer.this.gui,
                        turnrundownChart);
            }
        });
        this.gui.addLogTab(logGUI);
    }

    /**
     * Creates KoL data files if they do not already exist in the file system.
     */
    public static void writeDataFilesToFileSystem() {
        final List<File> kolDataFiles = new ArrayList<>();
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "skills.txt"));
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "semirares.txt"));
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "badmoon.txt"));
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "areaNameMappings.txt"));
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "importantItems.txt"));
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "onetimeItems.txt"));
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "outfits.txt"));
        kolDataFiles.add(new File(UtilityConstants.ROOT_DIRECTORY
                + File.separator + UtilityConstants.KOL_DATA_DIRECTORY
                + "statsItems.txt"));
        for (final File f : kolDataFiles) {
            if (!f.exists()) {
                String tmpLine;
                try (final BufferedReader br = DataUtilities.getReader(
                        UtilityConstants.KOL_DATA_DIRECTORY, f.getName())) {
                    f.createNewFile();
                    try (final PrintWriter fileWriter = new PrintWriter(f)) {
                        while ((tmpLine = br.readLine()) != null) {
                            fileWriter.println(tmpLine);
                        }
                        fileWriter.close();
                    }
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void commandLineParsing(final String[] args) {
        final String mafiaLogsDirectoryPath;
        final String parsedLogsSavingDirectoryPath;
        if (args.length >= 3) {
            mafiaLogsDirectoryPath = args[1];
            parsedLogsSavingDirectoryPath = args[2];
        } else if (args.length == 2) {
            mafiaLogsDirectoryPath = args[1];
            parsedLogsSavingDirectoryPath = args[1];
        } else {
            mafiaLogsDirectoryPath = Settings
                    .getSettingString("Mafia logs location");
            parsedLogsSavingDirectoryPath = Settings
                    .getSettingString("Parsed logs saving location");
        }
        final File mafiaLogsDirectory = new File(mafiaLogsDirectoryPath);
        final File parsedLogsSavingDirectory = new File(
                parsedLogsSavingDirectoryPath);
        if (!mafiaLogsDirectory.exists() || !mafiaLogsDirectory.isDirectory()
                || !parsedLogsSavingDirectory.exists()
                || !parsedLogsSavingDirectory.isDirectory()) {
            System.out.println("Please specify only existing directories.");
            return;
        }
        final File[] mafiaLogs = mafiaLogsDirectory
                .listFiles(InternalMafiaLogParserDialog.MAFIA_LOG_FILTER);
        if (mafiaLogs.length == 0) {
            System.out
                    .println("The directory specified for mafia logs does not contain any mafia logs.");
            return;
        }
        // If the input seems to be correct, save the directories used.
        Settings.setSettingString("Mafia logs location", mafiaLogsDirectoryPath);
        Settings.setSettingString("Parsed logs saving location",
                parsedLogsSavingDirectoryPath);
        // Now, the actual parsing can start.
        try {
            System.out.println("Parsing, please wait.");
            final List<DataNumberPair<String>> errorFileList = LogsCreator
                    .createParsedLogs(mafiaLogs, parsedLogsSavingDirectory,
                            TextualLogVersion.TEXT_LOG);
            System.out.println("Parsing finished.\n\n");
            // If there were error logs, give the user feedback on them.
            if (!errorFileList.isEmpty()) {
                final StringBuilder str = new StringBuilder(100);
                str.append("There were problems parsing the following logs. Please check the underlaying mafia session logs to see\n"
                        + "if they contained any corrupted data or lines longer than 500 characters and try to remove any problems.\n\n\n");
                str.append("The given list lists the erroneous log name and turn number after which the error occurred in the mafia\n"
                        + "session log upon which the log is based on.\n\n");
                for (final DataNumberPair<String> dn : errorFileList) {
                    str.append(dn + "\n");
                }
                System.out.println(str);
            }
        } catch (final IOException e) {
            System.out
                    .println("There was a problem while running the parser. Please check whether the parsed logs were created.");
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        if ((args.length > 0)
                && (args[0].equals("-parse") || args[0].equals("-p"))) {
            LogVisualizer.commandLineParsing(args);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new LogVisualizer();
                }
            });
        }
    }
}
