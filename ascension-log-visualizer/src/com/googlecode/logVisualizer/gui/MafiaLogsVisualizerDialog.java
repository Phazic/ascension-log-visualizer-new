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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.spellcast.utilities.UtilityConstants;
import net.java.swingfx.waitwithstyle.PerformanceInfiniteProgressPanel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.parser.LogsCreator;

/**
 * A dialog to select mafia logs for direct visualisation without having to
 * first turn them into parsed ascension logs.
 * <p>
 * This class only gives controls to select mafia logs for visualisation, it
 * does not parse those logs itself. Such a task has to be handled by another
 * class. The selected condensed mafia logs are delegated from this class
 * through the {@link MafiaLogLoaderListener} instance which is needed for the
 * constructor.
 * <p>
 * Note that the mafia log delegation through the {@link MafiaLogLoaderListener}
 * interface must be able to handle concurrent method calls from this class.
 */
final class MafiaLogsVisualizerDialog extends JDialog {
    /**
     *
     */
    private static final long serialVersionUID = 3804936802182556332L;
    private static final FilenameFilter MAFIA_LOG_FILTER = new FilenameFilter() {
        private final Matcher mafiaLogMatcher = Pattern.compile(
                ".*_\\d+\\.txt$").matcher("");
        private final String preparsedLogPartialFileString = "_ascend";

        @Override
        public boolean accept(final File dir, final String name) {
            return this.mafiaLogMatcher.reset(name).matches()
                    && !name.contains(this.preparsedLogPartialFileString);
        }
    };
    private static final List<File> EMPTY_MAFIA_LOGS_LIST = new ArrayList<>(1);
    private final JTextField mafiaLogsDirectoryField;
    private final JTable visualizableMafiaLogsTable;
    private final JCheckBox toggleAllBox;
    private final MafiaLogLoaderListener mafiaLogLoaderListener;

    /**
     * Constructs the object.
     * <p>
     * Note that the mafia log delegation through the
     * {@link MafiaLogLoaderListener} interface must be able to handle
     * concurrent method calls from this class.
     *
     * @param owner
     *            The owner of this dialog.
     * @param mafiaLogLoaderListener
     *            The interface through which the selected condensed mafia logs
     *            are delegated to another class.
     * @throws NullPointerException
     *             if owner is {@code null}; if mafiaLogLoaderListener is
     *             {@code null}
     */
    MafiaLogsVisualizerDialog(final JFrame owner,
            final MafiaLogLoaderListener mafiaLogLoaderListener) {
        super(owner, true);
        if (mafiaLogLoaderListener == null) {
            throw new NullPointerException(
                    "mafiaLogLoaderListener must not be null.");
        }
        this.setLayout(new BorderLayout(5, 10));
        this.setTitle("Mafia Logs Parser");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setGlassPane(new PerformanceInfiniteProgressPanel());
        this.mafiaLogLoaderListener = mafiaLogLoaderListener;
        this.visualizableMafiaLogsTable = new JTable(new MafiaLogsTableModel(
                MafiaLogsVisualizerDialog.EMPTY_MAFIA_LOGS_LIST));
        this.toggleAllBox = new JCheckBox("Visualize all logs");
        this.toggleAllBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                if (MafiaLogsVisualizerDialog.this.toggleAllBox.hasFocus()) {
                    ((MafiaLogsTableModel) MafiaLogsVisualizerDialog.this.visualizableMafiaLogsTable
                            .getModel())
                            .setVisualizeAll(MafiaLogsVisualizerDialog.this.toggleAllBox
                                    .isSelected());
                }
            }
        });
        this.mafiaLogsDirectoryField = new JTextField(
                Settings.getSettingString("Mafia logs location"));
        this.mafiaLogsDirectoryField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MafiaLogsVisualizerDialog.this.createMafiaLogsTable();
            }
        });
        this.createMafiaLogsTable();
        final JButton runButton = new JButton("Run parser");
        final JButton cancelButton = new JButton("Cancel");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (((MafiaLogsTableModel) MafiaLogsVisualizerDialog.this.visualizableMafiaLogsTable
                        .getModel()).isVisualizationsOccur()) {
                    MafiaLogsVisualizerDialog.this
                            .setWaitingForComputationEnd(true);
                    MafiaLogsVisualizerDialog.this.runParser();
                } else {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "There are no mafia logs selected to be visualized.",
                                    "Nothing to visualize",
                                    JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MafiaLogsVisualizerDialog.this.dispose();
            }
        });
        this.add(this.createMafiaLogsDirectoryFinderPanel(), BorderLayout.NORTH);
        this.add(this.createMafiaLogsTablePanel(), BorderLayout.CENTER);
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        buttonPanel.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(runButton);
        buttonPanel.add(cancelButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
        final Dimension currentSize = this.getSize();
        if (currentSize.height < 500) {
            currentSize.height = 500;
            this.setSize(currentSize);
        } else if (currentSize.height > 700) {
            currentSize.height = 700;
            this.setSize(currentSize);
        }
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    private JPanel createMafiaLogsDirectoryFinderPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Mafia logs location"));
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
        panel.add(this.mafiaLogsDirectoryField, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 25, 5, 10);
        panel.add(directoryChooserButton, gbc);
        File mafiaLogsDirectory = new File(
                Settings.getSettingString("Mafia logs location"));
        if (!mafiaLogsDirectory.exists()) {
            mafiaLogsDirectory = null;
        }
        final JFileChooser directoryChooser = new JFileChooser(
                mafiaLogsDirectory);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int state = directoryChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    MafiaLogsVisualizerDialog.this.mafiaLogsDirectoryField
                            .setText(directoryChooser.getSelectedFile()
                                    .getAbsolutePath());
                    MafiaLogsVisualizerDialog.this.createMafiaLogsTable();
                }
            }
        });
        return panel;
    }

    private JPanel createMafiaLogsTablePanel() {
        final JPanel panel = new JPanel(new BorderLayout(5, 10));
        final JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 5, 0);
        bottomPanel.add(this.toggleAllBox, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 5, 5, 5);
        bottomPanel
                .add(new JLabel(
                        "<html>Note that depending on the number of mafia logs selected and the amount and contents in those logs"
                                + "<p>the parsing process may take a while to finish."
                                + "<p><p>Also, note that it is not advisable to have much more than 10 mafia log charts open at the same time.</html>"),
                        gbc);
        final JScrollPane scrollPane = new JScrollPane(
                this.visualizableMafiaLogsTable);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    void createMafiaLogsTable() {
        final File mafiaLogsDirectory = new File(
                this.mafiaLogsDirectoryField.getText());
        if (!mafiaLogsDirectory.exists() || !mafiaLogsDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(null,
                    "Please only specify existing directories.",
                    "Problem occurred", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final File[] mafiaLogs = mafiaLogsDirectory
                .listFiles(MafiaLogsVisualizerDialog.MAFIA_LOG_FILTER);
        if (mafiaLogs.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "The specified directory does not contain any mafia logs.",
                    "Problem occurred", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // If the input seems to be correct, save the directory used.
        Settings.setSettingString("Mafia logs location",
                this.mafiaLogsDirectoryField.getText());
        // In case there are still some logs in the temporary data directory
        // delete all of its contents.
        for (final File f : UtilityConstants.TEMP_LOCATION.listFiles()) {
            if (!f.isDirectory()) {
                f.delete();
            }
        }
        // Start the actual computation.
        this.setWaitingForComputationEnd(true);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final File[] condensedMafiaLogs = LogsCreator
                            .createCondensedMafiaLogs(mafiaLogs);
                    ((MafiaLogsTableModel) MafiaLogsVisualizerDialog.this.visualizableMafiaLogsTable
                            .getModel()).setMafiaLogs(Arrays
                            .asList(condensedMafiaLogs));
                    MafiaLogsVisualizerDialog.this.toggleAllBox
                            .setSelected(false);
                } catch (final IOException e) {
                    e.printStackTrace();
                } finally {
                    MafiaLogsVisualizerDialog.this
                            .setWaitingForComputationEnd(false);
                }
            }
        });
        executor.shutdown();
    }

    /**
     * @param isComputationNotDone
     *            A flag showing whether the computation has ended or not.
     */
    void setWaitingForComputationEnd(final boolean isComputationNotDone) {
        this.getGlassPane().setVisible(isComputationNotDone);
    }

    void runParser() {
        final Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 4 Threads per CPU should be a high enough number to not slow
                // the computation too much down by scheduler overhead while
                // still making use of threaded computing.
                final ExecutorService executor = Executors
                        .newFixedThreadPool(Runtime.getRuntime()
                                .availableProcessors() * 4);
                for (final File f : ((MafiaLogsTableModel) MafiaLogsVisualizerDialog.this.visualizableMafiaLogsTable
                        .getModel()).getVisualizableMafiaLogs()) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            MafiaLogsVisualizerDialog.this.mafiaLogLoaderListener
                                    .visualizeMafiaLog(f);
                        }
                    });
                }
                // Wait for all threads to finish.
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                MafiaLogsVisualizerDialog.this.dispose();
            }
        });
        workerThread.start();
    }

    @Override
    public void dispose() {
        // Delete any leftover temporary data.
        for (final File f : UtilityConstants.TEMP_LOCATION.listFiles()) {
            if (!f.isDirectory()) {
                f.delete();
            }
        }
        super.dispose();
    }

    /**
     * TableModel used by the JTable which handles the selection of mafia logs
     * which should be visualised.
     */
    private static final class MafiaLogsTableModel extends AbstractTableModel {
        /**
         *
         */
        private static final long serialVersionUID = 4115093485840189667L;
        private static final String[] columnNames = { "Mafia log",
                "Should be visualized?" };
        private List<File> mafiaLogs;
        private List<Boolean> visualizables;

        MafiaLogsTableModel(final Collection<File> mafiaLogs) {
            this.mafiaLogs = new ArrayList<>(mafiaLogs);
            this.visualizables = new ArrayList<>(mafiaLogs.size() + 1);
            for (int i = 0, j = mafiaLogs.size(); i < j; i++) {
                this.visualizables.add(false);
            }
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            if (columnIndex == 1) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex,
                final int columnIndex) {
            this.visualizables.set(rowIndex, (Boolean) aValue);
        }

        @Override
        public String getColumnName(final int column) {
            return MafiaLogsTableModel.columnNames[column];
        }

        @Override
        public int getColumnCount() {
            return MafiaLogsTableModel.columnNames.length;
        }

        @Override
        public int getRowCount() {
            return this.mafiaLogs.size();
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            return columnIndex == 0 ? this.mafiaLogs.get(rowIndex).getName()
                    .replace(".txt", "") : this.visualizables.get(rowIndex);
        }

        /**
         * Sets the contents of this model to the given collection of mafia
         * logs.
         *
         * @param mafiaLogs
         *            A collection of condensed mafia logs.
         */
        void setMafiaLogs(final Collection<File> mafiaLogs) {
            this.mafiaLogs = new ArrayList<>(mafiaLogs);
            this.visualizables = new ArrayList<>(mafiaLogs.size());
            for (int i = 0, j = mafiaLogs.size(); i < j; i++) {
                this.visualizables.add(false);
            }
            this.fireTableDataChanged();
        }

        /**
         * @return The mafia logs which should be visualised.
         */
        List<File> getVisualizableMafiaLogs() {
            final List<File> visualizableMafiaLogs = new ArrayList<>(
                    this.mafiaLogs.size());
            for (int i = 0, j = this.visualizables.size(); i < j; i++) {
                if (this.visualizables.get(i)) {
                    visualizableMafiaLogs.add(this.mafiaLogs.get(i));
                }
            }
            return visualizableMafiaLogs;
        }

        /**
         * @param isVisualizeAll
         *            A flag showing whether all mafia logs inside this model
         *            should be visualised or not.
         */
        void setVisualizeAll(final boolean isVisualizeAll) {
            Collections.fill(this.visualizables, isVisualizeAll);
            this.fireTableDataChanged();
        }

        /**
         * @return {@code true} in case at least one mafia log from inside this
         *         model should be visualised.
         */
        boolean isVisualizationsOccur() {
            return this.visualizables.size() > 0 ? this.visualizables
                    .contains(true) : false;
        }
    }

    /**
     * Interface used to delegate the visualisation of mafia logs to the place
     * where it is actually handled (which is not inside the
     * {@link MafiaLogsVisualizerDialog} class).
     */
    public static interface MafiaLogLoaderListener {
        public void visualizeMafiaLog(final File mafiaLog);
    }
}
