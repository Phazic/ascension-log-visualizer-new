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
package com.googlecode.logVisualizer.gui.notetaker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.parser.LogsCreator;
import com.googlecode.logVisualizer.util.LookAheadIterator;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator.TextualLogVersion;

/**
 * This class is ascension log notes editor, that gives the user a basic
 * interface to manage log notes.
 */
public final class Notetaker extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = -8185868433565028732L;
    private final LogDataHolder log;
    private final JButton saveButton;
    private final TurnIntervalMenuList turnIntervalMenu;
    private final JTextArea notesArea;
    private TurnIntervalContainer activeTurnInterval;

    /**
     * Will show a dialog to let the user choose which turncounts should be
     * included inside the Notetaker and then show the actual Notetaker
     * interface based on that decision.
     */
    public static void showNotetaker(final LogDataHolder log) {
        final JComboBox<LogInterval> selectionBox = new JComboBox<>();
        selectionBox.setPreferredSize(new Dimension(400, selectionBox
                .getPreferredSize().height));
        // Populate the selection combo box with all possible intervals.
        selectionBox
                .addItem(new LogInterval("Full log", -1, Integer.MAX_VALUE));
        final LookAheadIterator<DayChange> index = new LookAheadIterator<>(log
                .getDayChanges().iterator());
        while (index.hasNext()) {
            final DayChange dc = index.next();
            final int nextDayChange = index.peek() != null ? index.peek()
                    .getTurnNumber() : Integer.MAX_VALUE;
            selectionBox.addItem(new LogInterval("Day " + dc.getDayNumber(), dc
                    .getTurnNumber(), nextDayChange));
        }
        selectionBox.setSelectedIndex(0);
        // Show the interval selection dialog.
        JOptionPane
                .showMessageDialog(
                        null,
                        selectionBox,
                        "Select what interval that should be displayed in the Notetaker",
                        JOptionPane.QUESTION_MESSAGE);
        // Create and show the actual Notetaker interface with the chosen
        // interval.
        final LogInterval selection = (LogInterval) selectionBox
                .getSelectedItem();
        if (selection.getName().equals("Full log")) {
            new Notetaker(log);
        } else {
            new Notetaker(log.getSubIntervalLogData(selection.getStartTurn(),
                    selection.getEndTurn()));
        }
    }

    /**
     * Creates the notes editor frame.
     *
     * @param log
     *            The log data whose notes should be managed.
     */
    Notetaker(final LogDataHolder log) {
        super("Notetaker for " + log.getLogName());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout(5, 25));
        this.log = log;
        this.saveButton = new JButton("Save log to file");
        this.turnIntervalMenu = new TurnIntervalMenuList(log);
        this.notesArea = new JTextArea();
        this.notesArea.setLineWrap(true);
        this.notesArea.setWrapStyleWord(true);
        this.getContentPane().add(this.createNotePanel(), BorderLayout.CENTER);
        this.getContentPane().add(this.createButtonPanel(), BorderLayout.SOUTH);
        this.addListeners();
        this.turnIntervalMenu.setSelectedIndex(0);
        this.setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    private JComponent createNotePanel() {
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(this.turnIntervalMenu));
        splitPane.setBottomComponent(new JScrollPane(this.notesArea));
        splitPane.setDividerLocation(400);
        return splitPane;
    }

    private JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 200, 0));
        final JButton closeButton = new JButton("Close window");
        this.saveButton.setPreferredSize(new Dimension(0, 40));
        closeButton.setPreferredSize(new Dimension(0, 40));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (Notetaker.this.activeTurnInterval != null) {
                    Notetaker.this.activeTurnInterval
                            .setNotes(Notetaker.this.notesArea.getText());
                }
                Notetaker.this.dispose();
            }
        });
        buttonPanel.add(this.saveButton);
        buttonPanel.add(closeButton);
        return buttonPanel;
    }

    private void addListeners() {
        this.turnIntervalMenu
                .addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(final ListSelectionEvent lse) {
                        if (!lse.getValueIsAdjusting()) {
                            if (Notetaker.this.activeTurnInterval != null) {
                                Notetaker.this.activeTurnInterval
                                        .setNotes(Notetaker.this.notesArea
                                                .getText());
                            }
                            Notetaker.this.activeTurnInterval = Notetaker.this.turnIntervalMenu
                                    .getCurrentlySelectedTurnInterval();
                            Notetaker.this.notesArea
                                    .setText(Notetaker.this.activeTurnInterval
                                            .getNotes());
                        }
                    }
                });
        this.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (Notetaker.this.activeTurnInterval != null) {
                    Notetaker.this.activeTurnInterval
                            .setNotes(Notetaker.this.notesArea.getText());
                }
                new SaveDialog();
            }
        });
    }

    /**
     * ListCellRenderer for turn intervals, because the need to be able to show
     * multi-line strings as texts with multiple lines.
     */
    private static final class TurnIntervalCellRenderer extends JTextArea
            implements ListCellRenderer<TurnIntervalContainer> {
        /**
         *
         */
        private static final long serialVersionUID = -5038545398904224614L;

        TurnIntervalCellRenderer() {
            this.setLineWrap(true);
            this.setWrapStyleWord(true);
            this.setEditable(false);
            this.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                final JList<? extends TurnIntervalContainer> list,
                final TurnIntervalContainer value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {
            this.setText(value.getTurnInterval().toString());
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            return this;
        }
    }

    /**
     * Just a little helper class make instantiation of the turn interval menu a
     * little nicer.
     */
    private static final class TurnIntervalMenuList extends
            JList<TurnIntervalContainer> {
        /**
         *
         */
        private static final long serialVersionUID = 2607220827265758012L;

        /**
         * Creates the turn interval menu.
         *
         * @param log
         *            The log data whose notes should be managed.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        TurnIntervalMenuList(final LogDataHolder log) {
            super(new DefaultListModel<TurnIntervalContainer>());
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            this.setCellRenderer(new TurnIntervalCellRenderer());
            final Iterator<String> turnRundownListIndex = TextLogCreator
                    .getTurnRundownList(log).iterator();
            for (final TurnInterval ti : log.getTurnsSpent()) {
                ((DefaultListModel) this.getModel())
                        .addElement(new TurnIntervalContainer(ti,
                                turnRundownListIndex.next()));
            }
        }

        @SuppressWarnings("rawtypes")
        TurnIntervalContainer getCurrentlySelectedTurnInterval() {
            if (this.isSelectionEmpty()) {
                throw new IllegalStateException(
                        "No turn interval is currently selected.");
            }
            return (TurnIntervalContainer) ((DefaultListModel) this.getModel())
                    .get(this.getSelectedIndex());
        }
    }

    /**
     * The dialog that is seen when one wants to save the log to a file.
     */
    private final class SaveDialog extends JDialog {
        /**
         *
         */
        private static final long serialVersionUID = -2257321026657963976L;
        private final JTextField directoryLocationField = new JTextField(
                Settings.getSettingString("Parsed logs saving location"));
        private TextualLogVersion logVersion = TextualLogVersion.TEXT_LOG;

        /**
         * Creates and shows the save dialog in the centre of the screen. Please
         * note that this dialog is also modal on the notes editor frame.
         */
        SaveDialog() {
            super(Notetaker.this, true);
            this.setTitle("Choose saving directory for the ascension log");
            this.setLayout(new BorderLayout(0, 10));
            this.getContentPane().add(this.createLogVersionChooserPanel(),
                    BorderLayout.NORTH);
            this.getContentPane().add(this.createDirectoryFinderPanel(),
                    BorderLayout.CENTER);
            this.getContentPane().add(this.createButtonPanel(),
                    BorderLayout.SOUTH);
            this.pack();
            this.setSize(500, this.getSize().height);
            RefineryUtilities.centerFrameOnScreen(this);
            this.setVisible(true);
        }

        private JPanel createLogVersionChooserPanel() {
            final JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
            final JRadioButton textButton = new JRadioButton("Text", true);
            final JRadioButton htmlButton = new JRadioButton("HTML", false);
            final JRadioButton bbcodeButton = new JRadioButton("BBCode", false);
            final ButtonGroup group = new ButtonGroup();
            group.add(textButton);
            group.add(htmlButton);
            group.add(bbcodeButton);
            final ChangeListener listener = new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    if (htmlButton.isSelected()) {
                        SaveDialog.this.logVersion = TextualLogVersion.HTML_LOG;
                    } else if (bbcodeButton.isSelected()) {
                        SaveDialog.this.logVersion = TextualLogVersion.BBCODE_LOG;
                    } else {
                        SaveDialog.this.logVersion = TextualLogVersion.TEXT_LOG;
                    }
                }
            };
            textButton.addChangeListener(listener);
            htmlButton.addChangeListener(listener);
            bbcodeButton.addChangeListener(listener);
            panel.add(textButton);
            panel.add(htmlButton);
            panel.add(bbcodeButton);
            return panel;
        }

        private JPanel createDirectoryFinderPanel() {
            final JPanel panel = new JPanel(new GridBagLayout());
            final JButton directoryChooserButton = new JButton("Find Directory");
            GridBagConstraints gbc;
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(10, 10, 5, 0);
            panel.add(this.directoryLocationField, gbc);
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(10, 25, 5, 10);
            panel.add(directoryChooserButton, gbc);
            File logsDirectory = new File(this.directoryLocationField.getText());
            if (!logsDirectory.exists()) {
                logsDirectory = null;
            }
            final JFileChooser directoryChooser = new JFileChooser(
                    logsDirectory);
            directoryChooser
                    .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final int state = directoryChooser.showOpenDialog(null);
                    if (state == JFileChooser.APPROVE_OPTION) {
                        SaveDialog.this.directoryLocationField
                                .setText(directoryChooser.getSelectedFile()
                                        .getAbsolutePath());
                    }
                }
            });
            return panel;
        }

        private JPanel createButtonPanel() {
            final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 100, 0));
            final JButton closeButton = new JButton("Cancel");
            final JButton saveButton = new JButton("OK");
            saveButton.setPreferredSize(new Dimension(0, 30));
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    try {
                        String filePath = SaveDialog.this.directoryLocationField
                                .getText();
                        if (!filePath.endsWith(File.separator)) {
                            filePath += File.separator;
                        }
                        final File logsDest;
                        if (SaveDialog.this.logVersion == TextualLogVersion.HTML_LOG) {
                            logsDest = new File(
                                    filePath
                                            + LogsCreator
                                                    .getParsedLogNameFromCondensedMafiaLog(Notetaker.this.log
                                                            .getLogName())
                                            + ".html");
                        } else {
                            logsDest = new File(
                                    filePath
                                            + LogsCreator
                                                    .getParsedLogNameFromCondensedMafiaLog(Notetaker.this.log
                                                            .getLogName())
                                            + ".txt");
                        }
                        if (logsDest.exists()) {
                            logsDest.delete();
                        }
                        logsDest.createNewFile();
                        TextLogCreator.saveTextualLogToFile(Notetaker.this.log,
                                logsDest, SaveDialog.this.logVersion);
                        Settings.setSettingString(
                                "Parsed logs saving location",
                                SaveDialog.this.directoryLocationField
                                        .getText());
                    } catch (final IOException e1) {
                        JOptionPane.showMessageDialog(null,
                                "A problem occurred while creating the log.",
                                "Error occurred", JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                    }
                    SaveDialog.this.dispose();
                }
            });
            closeButton.setPreferredSize(new Dimension(0, 30));
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    SaveDialog.this.dispose();
                }
            });
            buttonPanel.add(saveButton);
            buttonPanel.add(closeButton);
            return buttonPanel;
        }
    }

    private static final class LogInterval {
        private final String name;
        private final int startTurn;
        private final int endTurn;

        LogInterval(final String name, final int startTurn, final int endTurn) {
            if (name == null) {
                throw new IllegalArgumentException("The name must not be null.");
            }
            this.name = name;
            this.startTurn = startTurn;
            this.endTurn = endTurn;
        }

        String getName() {
            return this.name;
        }

        int getStartTurn() {
            return this.startTurn;
        }

        int getEndTurn() {
            return this.endTurn;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 8421;
            result = (prime * result) + this.startTurn;
            result = (prime * result) + this.endTurn;
            result = (prime * result) + this.name.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof LogInterval)) {
                return false;
            }
            final LogInterval other = (LogInterval) obj;
            if (this.startTurn != other.startTurn) {
                return false;
            }
            if (this.endTurn != other.endTurn) {
                return false;
            }
            if (this.name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!this.name.equals(other.name)) {
                return false;
            }
            return true;
        }
    }
}
