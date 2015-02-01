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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import net.java.swingfx.waitwithstyle.PerformanceInfiniteProgressPanel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;

/**
 * A dialog to give the possibility to use the AFH MafiaLog Parser from inside
 * the Ascension Log Visualizer as long as the command line of the machine which
 * is running this program can correctly handle the command {@code perl}.
 * (Basically, this means that there has to be a Perl Runtime Environment
 * installed on the given machine)
 */
final class ExternalMafiaLogParserDialog extends JDialog {
    /**
     *
     */
    private static final long serialVersionUID = 6892809291961419988L;
    private static final FileFilter PERL_SCRIPT_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".pl");
        }

        @Override
        public String getDescription() {
            return "Perl Script";
        }
    };
    private final ActionListener runAFHParserAction = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            if (!ExternalMafiaLogParserDialog.this.fileLocationField.getText()
                    .equals("")
                    && !ExternalMafiaLogParserDialog.this.argumentsField
                            .getText().equals("")
                    && !ExternalMafiaLogParserDialog.this.userNameField
                            .getText().equals("")) {
                ExternalMafiaLogParserDialog.this.setWaitingForProcessEnd(true);
                ExternalMafiaLogParserDialog.this.runAFHParser();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Please fill out all text fields.", "Missing input",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };
    private JTextField fileLocationField;
    private JTextField argumentsField;
    private JTextField userNameField;

    ExternalMafiaLogParserDialog(final JFrame owner) {
        super(owner, true);
        this.setLayout(new BorderLayout(5, 10));
        this.setTitle("External Mafia Log Parser");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setGlassPane(new PerformanceInfiniteProgressPanel());
        final JButton runButton = new JButton("Run parser");
        final JButton cancelButton = new JButton("Cancel");
        runButton.addActionListener(this.runAFHParserAction);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ExternalMafiaLogParserDialog.this.dispose();
            }
        });
        final JPanel afhParserPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        afhParserPanel.add(this.createAFHParserLocationPanel());
        afhParserPanel.add(this.createAFHParserOptionsPanel());
        this.add(afhParserPanel, BorderLayout.NORTH);
        this.add(
                new JLabel(
                        "<html>Please refer to the AFH MafiaLog Parser documention on how to use that parser."
                                + "<p>Note that the AFH MafiaLog Parser is not distributed together with this program.</html>"),
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

    private JPanel createAFHParserLocationPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory
                .createTitledBorder("AFH MafiaLog Parser location"));
        this.fileLocationField = new JTextField(
                Settings.getSettingString("AFH Parser location"));
        final JButton fileChooserButton = new JButton("Find File");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(this.fileLocationField, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(fileChooserButton, gbc);
        this.fileLocationField.addActionListener(this.runAFHParserAction);
        File mafiaLogsDirectory = new File(
                Settings.getSettingString("Mafia logs location"));
        if (!mafiaLogsDirectory.exists()) {
            mafiaLogsDirectory = null;
        }
        final JFileChooser logChooser = new JFileChooser(mafiaLogsDirectory);
        logChooser
                .setFileFilter(ExternalMafiaLogParserDialog.PERL_SCRIPT_FILTER);
        fileChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int state = logChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    ExternalMafiaLogParserDialog.this.fileLocationField
                            .setText(logChooser.getSelectedFile()
                                    .getAbsolutePath());
                }
            }
        });
        return panel;
    }

    private JPanel createAFHParserOptionsPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory
                .createTitledBorder("AFH MafiaLog Parser options"));
        this.argumentsField = new JTextField(
                Settings.getSettingString("AFH Parser arguments"));
        this.userNameField = new JTextField(
                Settings.getSettingString("AFH Parser user name"));
        final JLabel argumentsLabel = new JLabel("Arguments:");
        final JLabel userNameLabel = new JLabel("User Name:");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(argumentsLabel, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 0);
        panel.add(userNameLabel, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(this.argumentsField, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(this.userNameField, gbc);
        this.argumentsField.addActionListener(this.runAFHParserAction);
        this.userNameField.addActionListener(this.runAFHParserAction);
        return panel;
    }

    /**
     * @param isProcessNotDone
     *            A flag showing whether the process has ended or not.
     */
    void setWaitingForProcessEnd(final boolean isProcessNotDone) {
        this.getGlassPane().setVisible(isProcessNotDone);
    }

    /**
     * Runs the parser with the data from the TextFields of the GUI.
     */
    void runAFHParser() {
        // Save the used text field inputs.
        Settings.setSettingString("AFH Parser location",
                this.fileLocationField.getText());
        Settings.setSettingString("AFH Parser arguments",
                this.argumentsField.getText());
        Settings.setSettingString("AFH Parser user name",
                this.userNameField.getText());
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final File workingDirectory = new File(
                            ExternalMafiaLogParserDialog.this.fileLocationField
                                    .getText()).getParentFile();
                    final Process process = Runtime
                            .getRuntime()
                            .exec("perl "
                                    + ExternalMafiaLogParserDialog.this.fileLocationField
                                            .getText()
                                    + " "
                                    + ExternalMafiaLogParserDialog.this.argumentsField
                                            .getText()
                                    + " "
                                    + ExternalMafiaLogParserDialog.this.userNameField
                                            .getText(), null, workingDirectory);
                    try (final BufferedReader br = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        final ParserOutputViewer outputViewer = new ParserOutputViewer();
                        ExternalMafiaLogParserDialog.this.toFront();
                        String tmp;
                        while ((tmp = br.readLine()) != null) {
                            outputViewer.addParserOutput(tmp);
                        }
                        br.close();
                        ExternalMafiaLogParserDialog.this.dispose();
                        outputViewer.toFront();
                    }
                } catch (final IOException e) {
                    ExternalMafiaLogParserDialog.this
                            .setWaitingForProcessEnd(false);
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "There was a problem while running the parser. Please check whether the parsed logs were created.",
                                    "Problem occurred",
                                    JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }

    /**
     * A small class to display the output of the parser inside another frame.
     */
    private static final class ParserOutputViewer extends JFrame {
        /**
         *
         */
        private static final long serialVersionUID = -5574892142460463233L;
        private static final String NEW_LINE = "\n";
        private final JTextArea parserOutput;

        ParserOutputViewer() {
            super("Parser output");
            this.setLayout(new GridLayout(1, 0));
            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.parserOutput = new JTextArea();
            this.parserOutput.setLineWrap(true);
            this.parserOutput.setWrapStyleWord(true);
            this.add(new JScrollPane(this.parserOutput));
            this.pack();
            this.setSize(640, 480);
            RefineryUtilities.centerFrameOnScreen(this);
            this.setVisible(true);
        }

        void addParserOutput(final String s) {
            this.parserOutput.append(s);
            this.parserOutput.append(ParserOutputViewer.NEW_LINE);
        }
    }
}
