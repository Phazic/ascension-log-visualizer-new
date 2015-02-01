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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.java.swingfx.waitwithstyle.PerformanceInfiniteProgressPanel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.parser.LogsCreator;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator.TextualLogVersion;

/**
 * Dialog giving all controls necessary to parse mafia logs into ascension logs
 * using the internal parser of the Ascension Log Visualizer.
 */
public final class InternalMafiaLogParserDialog extends JDialog {
    /**
     *
     */
    private static final long serialVersionUID = 8624191051351970813L;
    public static final FilenameFilter MAFIA_LOG_FILTER = new FilenameFilter() {
        private final Matcher mafiaLogMatcher = Pattern.compile(
                ".*_\\d+\\.txt$").matcher("");
        private final String preparsedLogPartialFileString = "_ascend";

        @Override
        public boolean accept(final File dir, final String name) {
            return this.mafiaLogMatcher.reset(name).matches()
                    && !name.contains(this.preparsedLogPartialFileString);
        }
    };
    private final ActionListener runParserAction = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            if (!InternalMafiaLogParserDialog.this.mafiaLogsDirectoryField
                    .getText().equals("")
                    && !InternalMafiaLogParserDialog.this.parsedLogsSavingDirectoryField
                            .getText().equals("")) {
                InternalMafiaLogParserDialog.this
                        .setWaitingForComputationEnd(true);
                InternalMafiaLogParserDialog.this.runParser();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Please fill out all text fields.", "Missing input",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };
    private final JTextField mafiaLogsDirectoryField;
    private final JTextField parsedLogsSavingDirectoryField;
    private final JFileChooser directoryChooser;

    InternalMafiaLogParserDialog(final JFrame owner) {
        super(owner, true);
        this.setLayout(new BorderLayout(5, 10));
        this.setTitle("Mafia Logs Parser");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setGlassPane(new PerformanceInfiniteProgressPanel());
        this.mafiaLogsDirectoryField = new JTextField(
                Settings.getSettingString("Mafia logs location"));
        this.parsedLogsSavingDirectoryField = new JTextField(
                Settings.getSettingString("Parsed logs saving location"));
        File mafiaLogsDirectory = new File(
                this.mafiaLogsDirectoryField.getText());
        if (!mafiaLogsDirectory.exists()) {
            mafiaLogsDirectory = null;
        }
        this.directoryChooser = new JFileChooser(mafiaLogsDirectory);
        this.directoryChooser
                .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final JButton runButton = new JButton("Run parser");
        final JButton cancelButton = new JButton("Cancel");
        runButton.addActionListener(this.runParserAction);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                InternalMafiaLogParserDialog.this.dispose();
            }
        });
        final JPanel parserPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        parserPanel.add(this.createDirectoryFinderPanel(
                this.mafiaLogsDirectoryField, "Mafia logs directory location"));
        parserPanel.add(this.createDirectoryFinderPanel(
                this.parsedLogsSavingDirectoryField,
                "Parsed logs saving destination"));
        this.add(parserPanel, BorderLayout.NORTH);
        this.add(
                new JLabel(
                        "<html>Note that all ascensions contained inside the given mafia logs directory will be parsed out."
                                + "<p>That process may take a while depending on the amount and contents of the mafia logs.</html>"),
                BorderLayout.CENTER);
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        buttonPanel.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(runButton);
        buttonPanel.add(cancelButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    private JPanel createDirectoryFinderPanel(
            final JTextField directoryLocationField, final String description) {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(description));
        final JButton directoryChooserButton = new JButton("Find Directory");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(directoryLocationField, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(directoryChooserButton, gbc);
        directoryLocationField.addActionListener(this.runParserAction);
        directoryChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int state = InternalMafiaLogParserDialog.this.directoryChooser
                        .showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    directoryLocationField
                            .setText(InternalMafiaLogParserDialog.this.directoryChooser
                                    .getSelectedFile().getAbsolutePath());
                }
            }
        });
        return panel;
    }

    /**
     * @param isComputationNotDone
     *            A flag showing whether the computation has ended or not.
     */
    void setWaitingForComputationEnd(final boolean isComputationNotDone) {
        this.getGlassPane().setVisible(isComputationNotDone);
    }

    /**
     * Runs the parser with the data from the TextFields of the GUI.
     */
    void runParser() {
        final File mafiaLogsDirectory = new File(
                this.mafiaLogsDirectoryField.getText());
        final File parsedLogsSavingDirectory = new File(
                this.parsedLogsSavingDirectoryField.getText());
        if (!mafiaLogsDirectory.exists() || !mafiaLogsDirectory.isDirectory()
                || !parsedLogsSavingDirectory.exists()
                || !parsedLogsSavingDirectory.isDirectory()) {
            this.setWaitingForComputationEnd(false);
            JOptionPane.showMessageDialog(null,
                    "Please only specify existing directories.",
                    "Problem occurred", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final File[] mafiaLogs = mafiaLogsDirectory
                .listFiles(InternalMafiaLogParserDialog.MAFIA_LOG_FILTER);
        if (mafiaLogs.length == 0) {
            this.setWaitingForComputationEnd(false);
            JOptionPane
                    .showMessageDialog(
                            null,
                            "The directory specified for mafia logs does not contain any mafia logs.",
                            "Problem occurred", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // If the input seems to be correct, save the directories used.
        Settings.setSettingString("Mafia logs location",
                this.mafiaLogsDirectoryField.getText());
        Settings.setSettingString("Parsed logs saving location",
                this.parsedLogsSavingDirectoryField.getText());
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<DataNumberPair<String>> errorFileList = LogsCreator
                            .createParsedLogs(mafiaLogs,
                                    parsedLogsSavingDirectory,
                                    TextualLogVersion.TEXT_LOG);
                    // If there were error logs, give the user feedback on them.
                    if (!errorFileList.isEmpty()) {
                        final String errorPreface = "<html>There were problems parsing the following logs. Please check the underlaying mafia session logs to see<br>"
                                + "if they contained any corrupted data or lines longer than 500 characters and try to remove any problems.<br><br><br>"
                                + "The given list lists the erroneous ascension and turn number after which the error occurred in the mafia<br>"
                                + "session logs upon which the ascension is based on.<br><br></html>";
                        final JPanel panel = new JPanel(new BorderLayout());
                        panel.add(new JLabel(errorPreface), BorderLayout.NORTH);
                        panel.add(
                                new JScrollPane(InternalMafiaLogParserDialog
                                        .createErrorLogTable(errorFileList)),
                                BorderLayout.CENTER);
                        JOptionPane.showMessageDialog(null, panel,
                                "Error occurred", JOptionPane.ERROR_MESSAGE);
                    }
                    InternalMafiaLogParserDialog.this.dispose();
                } catch (final IOException e) {
                    InternalMafiaLogParserDialog.this
                            .setWaitingForComputationEnd(false);
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "There was a problem while running the parser. Please check whether the parsed logs were created.",
                                    "Error occurred", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }

    static JTable createErrorLogTable(
            final List<DataNumberPair<String>> errorFileList) {
        final String[] columNames = { "Player name", "Ascension start date",
                "Turn number" };
        final String[][] rowData = new String[errorFileList.size()][3];
        for (int i = 0; i < errorFileList.size(); i++) {
            final String[] logData = InternalMafiaLogParserDialog
                    .parseLogName(errorFileList.get(i).getData());
            rowData[i][0] = logData[0];
            rowData[i][1] = logData[1];
            rowData[i][2] = errorFileList.get(i).getNumber().toString();
        }
        return new JTable(rowData, columNames);
    }

    private static String[] parseLogName(final String logName) {
        final String[] tmp = logName.split("_ascend|.txt");
        final String[] result = { tmp[0], tmp[1] };
        return result;
    }
}
