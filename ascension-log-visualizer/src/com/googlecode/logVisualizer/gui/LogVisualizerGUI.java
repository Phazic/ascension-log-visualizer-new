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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.LogVisualizer;
import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.gui.MafiaLogsVisualizerDialog.MafiaLogLoaderListener;
import com.googlecode.logVisualizer.gui.notetaker.Notetaker;
import com.sun.java.forums.CloseableTabbedPane;
import com.sun.java.forums.CloseableTabbedPaneListener;

public final class LogVisualizerGUI extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = -418177126259480514L;
    private static final FileFilter ASCENSION_LOG_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.isDirectory()
                    || f.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return "Logs";
        }
    };
    private final JMenu removeMenu;
    private final CloseableTabbedPane logsPane;

    public LogVisualizerGUI(final LogLoaderListener logLoaderlistener) {
        super("Ascension Log Visualizer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (logLoaderlistener == null) {
            throw new NullPointerException(
                    "The LogLoaderListener must not be null.");
        }
        File mafiaLogsDirectory = new File(
                Settings.getSettingString("Mafia logs location"));
        if (!mafiaLogsDirectory.exists()) {
            mafiaLogsDirectory = null;
        }
        final JFileChooser logChooser = new JFileChooser(mafiaLogsDirectory);
        logChooser.setFileFilter(LogVisualizerGUI.ASCENSION_LOG_FILTER);
        this.logsPane = new CloseableTabbedPane();
        this.logsPane
                .addCloseableTabbedPaneListener(new CloseableTabbedPaneListener() {
                    @Override
                    public boolean closeTab(final int tabIndexToClose) {
                        LogVisualizerGUI.this.removeMenu
                                .remove(tabIndexToClose);
                        return true;
                    }
                });
        this.removeMenu = new JMenu("Remove tab");
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        final JMenu extraMenu = new JMenu("Extra");
        final JMenu helpMenu = new JMenu("Help");
        final JCheckBoxMenuItem ascensionCountingMenu = new JCheckBoxMenuItem(
                "Using old ascension day/turn counting",
                Settings.getSettingBoolean("Using old ascension counting"));
        ascensionCountingMenu.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                Settings.setSettingBoolean("Using old ascension counting",
                        ascensionCountingMenu.isSelected());
            }
        });
        final JCheckBoxMenuItem mafiaNotesParsingMenu = new JCheckBoxMenuItem(
                "Include mafia log notes",
                Settings.getSettingBoolean("Include mafia log notes"));
        mafiaNotesParsingMenu.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                Settings.setSettingBoolean("Include mafia log notes",
                        mafiaNotesParsingMenu.isSelected());
            }
        });
        fileMenu.add(new AbstractAction("Parse mafia logs") {
            /**
             *
             */
            private static final long serialVersionUID = -8099108496174386627L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                new InternalMafiaLogParserDialog(LogVisualizerGUI.this);
            }
        });
        fileMenu.add(new AbstractAction("Parse mafia logs with external parser") {
            /**
             *
             */
            private static final long serialVersionUID = -6161395126767129212L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                new ExternalMafiaLogParserDialog(LogVisualizerGUI.this);
            }
        });
        fileMenu.add(new AbstractAction("Visualize mafia logs") {
            /**
             *
             */
            private static final long serialVersionUID = -8065069608212184976L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                new MafiaLogsVisualizerDialog(LogVisualizerGUI.this,
                        new MafiaLogLoaderListener() {
                            @Override
                            public void visualizeMafiaLog(final File mafiaLog) {
                                logLoaderlistener.loadMafiaLog(mafiaLog);
                            }
                        });
            }
        });
        fileMenu.add(new AbstractAction("Visualize preparsed ascension log") {
            /**
             *
             */
            private static final long serialVersionUID = 2099269550515741590L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final int state = logChooser.showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    LogVisualizerGUI.this.setCursor(Cursor
                            .getPredefinedCursor(Cursor.WAIT_CURSOR));
                    logLoaderlistener.loadPreparsedLog(logChooser
                            .getSelectedFile());
                    LogVisualizerGUI.this.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(this.removeMenu);
        fileMenu.add(new AbstractAction("Remove all tabs") {
            /**
             *
             */
            private static final long serialVersionUID = -400140760030793011L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                LogVisualizerGUI.this.logsPane.removeAll();
                LogVisualizerGUI.this.removeMenu.removeAll();
                // Try to explicitly reclaim memory previously used by the
                // removed LogGUIs. This method doesn't necessarily do the
                // garbage collecting right now, but one should at least
                // try.
                // This is done because multiple LogGUIs take up quite a bit
                // of memory and when the memory is mostly used up the
                // garbage collector starts multiple cycles, slowing down
                // the program in the process. This is an attempt to move
                // the garbage collection to a part of the program that is
                // not that performance critical.
                System.gc();
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(ascensionCountingMenu);
        fileMenu.add(mafiaNotesParsingMenu);
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("Exit") {
            /**
             *
             */
            private static final long serialVersionUID = 7683584500758006370L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                System.exit(0);
            }
        });
        extraMenu.add(new AbstractAction("Notetaker") {
            /**
             *
             */
            private static final long serialVersionUID = 2509280333171401161L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (LogVisualizerGUI.this.logsPane.getTabCount() == 0) {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "There has to be a log loaded to start the Notetaker with.",
                                    "Problem occurred",
                                    JOptionPane.WARNING_MESSAGE);
                } else {
                    Notetaker
                            .showNotetaker(((LogGUI) LogVisualizerGUI.this.logsPane
                                    .getSelectedComponent()).getLogData());
                }
            }
        });
        extraMenu.add(new AbstractAction("Detailed Log Viewer") {
            /**
             *
             */
            private static final long serialVersionUID = 6559022868043570062L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (LogVisualizerGUI.this.logsPane.getTabCount() == 0) {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "There has to be a log loaded to start the Detailed Log Viewer with.",
                                    "Problem occurred",
                                    JOptionPane.WARNING_MESSAGE);
                } else {
                    new DetailedLogViewer(
                            ((LogGUI) LogVisualizerGUI.this.logsPane
                                    .getSelectedComponent()).getLogData());
                }
            }
        });
        extraMenu.addSeparator();
        extraMenu.add(new AbstractAction("Look&Feel changer") {
            /**
             *
             */
            private static final long serialVersionUID = 4898339895515379067L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                new LafChangerDialog(LogVisualizerGUI.this);
            }
        });
        extraMenu.add(new AbstractAction("Recreate data files") {
            /**
             *
             */
            private static final long serialVersionUID = 8579675109205513963L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final int state = JOptionPane
                        .showConfirmDialog(
                                null,
                                "<html>You may have to restart the program before changes will take effect."
                                        + "<p>All manual changes to the data files you may have done will be overwritten."
                                        + "<p>Continue?</html>",
                                "Manual changes will be overwritten",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                if (state == JOptionPane.OK_OPTION) {
                    for (final File file : UtilityConstants.KOL_DATA_LOCATION
                            .listFiles()) {
                        if (!file.isDirectory()) {
                            file.delete();
                        }
                    }
                    LogVisualizer.writeDataFilesToFileSystem();
                }
            }
        });
        final JCheckBoxMenuItem updatesCheckMenu = new JCheckBoxMenuItem(
                "Automatically check for newer versions",
                Settings.getSettingBoolean("Check Updates"));
        updatesCheckMenu.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                Settings.setSettingBoolean("Check Updates",
                        updatesCheckMenu.isSelected());
            }
        });
        helpMenu.add(updatesCheckMenu);
        helpMenu.add(new AbstractAction("About Licenses") {
            /**
             *
             */
            private static final long serialVersionUID = -6947607989277824569L;

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                new LicenseViewer(LogVisualizerGUI.this);
            }
        });
        helpMenu.addSeparator();
        helpMenu.add("Version: " + Settings.getSettingString("Version"));
        menuBar.add(fileMenu);
        menuBar.add(extraMenu);
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);
        this.add(this.logsPane);
    }

    /**
     * Adds the LogGUI to the main tabbed pane. Also adds an item to the remove
     * menu.
     *
     * @param logPanel
     *            The log panel to be added to the tabbed log pane.
     */
    public void addLogTab(final LogGUI logPanel) {
        this.logsPane.add(logPanel.getLogName(), logPanel);
        this.removeMenu.add(new RemoveMenuItem(logPanel.getLogName()));
    }

    void removeLogTab(final int tabIndex) {
        this.logsPane.remove(tabIndex);
        this.removeMenu.remove(tabIndex);
        // Try to explicitly reclaim memory previously used by the removed
        // LogGUI. This method doesn't necessarily do the garbage collecting
        // right now, but one should at least try.
        // This is done because multiple LogGUIs take up quite a bit of memory
        // and when the memory is mostly used up the garbage collector starts
        // multiple cycles, slowing down the program in the process. This is an
        // attempt to move the garbage collection to a part of the program that
        // is not that performance critical.
        System.gc();
    }

    /**
     * A class to handle removing of log tabs through the frame menu bar.
     */
    private final class RemoveMenuItem extends JMenuItem {
        /**
         *
         */
        private static final long serialVersionUID = 8813862187228366655L;

        RemoveMenuItem(final String tabName) {
            super(tabName);
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    int index = 0;
                    for (int i = 0; i < LogVisualizerGUI.this.logsPane
                            .getTabCount(); i++) {
                        if (LogVisualizerGUI.this.logsPane.getTitleAt(i)
                                .equals(RemoveMenuItem.this.getText())) {
                            index = i;
                        }
                    }
                    LogVisualizerGUI.this.removeLogTab(index);
                }
            });
        }
    }

    /**
     * A listener to handle parsing and showing of ascension logs.
     */
    public interface LogLoaderListener {
        /**
         * @param file
         *            A mafia ascension log.
         */
        public void loadMafiaLog(File file);

        /**
         * @param file
         *            A preparsed ascension log.
         */
        public void loadPreparsedLog(File file);
    }
}
