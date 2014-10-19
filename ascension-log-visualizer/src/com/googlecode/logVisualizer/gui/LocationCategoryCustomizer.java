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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.chart.turnrundownGantt.TurnAreaCategory;
import com.googlecode.logVisualizer.chart.turnrundownGantt.TurnrundownGantt;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.util.CategoryViewFileHandler;

public final class LocationCategoryCustomizer extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = -5853302439038344217L;
    private static final FileFilter CATEGORY_VIEW_FILES = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.isDirectory()
                    || f.getName().toLowerCase().endsWith(".cvw");
        }

        @Override
        public String getDescription() {
            return "Category Views";
        }
    };
    private final TurnrundownGantt turnrundownChart;
    private final JFileChooser viewChooser;
    private JSplitPane splitter;
    private JButton deleteCategory;
    private JButton addLoation;
    private JButton removeArea;
    private JButton addCategory;
    private JButton updateChart;
    private JButton loadCategoryView;
    private JButton saveCategoryView;
    private JTextField categoryName;
    private JComboBox<TurnAreaCategory> categoryList;
    private JList<String> categoryInventory;
    private JList<String> areas;
    private JList<String> areasAddList;

    /**
     * @param owner
     *            The JFrame which owns this dialog.
     * @param turnrundownChart
     *            The turnrundown gantt chart on which certain actions can be
     *            performed.
     */
    public LocationCategoryCustomizer(final JFrame owner,
            final TurnrundownGantt turnrundownChart) {
        super(owner, true);
        this.turnrundownChart = turnrundownChart;
        this.viewChooser = new JFileChooser();
        this.viewChooser
                .setFileSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.viewChooser
                .setFileFilter(LocationCategoryCustomizer.CATEGORY_VIEW_FILES);
        this.setLayout(new GridBagLayout());
        this.addGUIElements();
        this.addActions();
        this.addToolTips();
        this.updateData();
        this.pack();
        this.setTitle("Area categories customization");
        RefineryUtilities.centerFrameOnScreen(this);
        this.splitter.setDividerLocation(0.6);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private void addGUIElements() {
        this.splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.updateChart = new JButton("Update Chart");
        this.loadCategoryView = new JButton("Load Category View");
        this.saveCategoryView = new JButton("Save Category View");
        final JPanel categoryCreaterPane = new JPanel(new GridLayout(1, 0, 20,
                5));
        GridBagConstraints gbc;
        categoryCreaterPane.add(this.createAreaAddingPane());
        categoryCreaterPane.add(this.createCategoryAddingPane());
        this.splitter.setTopComponent(categoryCreaterPane);
        this.splitter.setBottomComponent(this.createCategoryPane());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        this.add(this.splitter, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(25, 5, 5, 0);
        this.add(this.loadCategoryView, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(25, 5, 5, 5);
        this.add(this.saveCategoryView, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipadx = 150;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(25, 0, 5, 5);
        this.add(this.updateChart, gbc);
    }

    private JPanel createCategoryPane() {
        final JPanel categoryPane = new JPanel(new GridBagLayout());
        this.categoryInventory = new JList<>(new DefaultListModel<>());
        this.deleteCategory = new JButton("Delete Category");
        this.categoryList = new JComboBox<>();
        GridBagConstraints gbc;
        this.categoryInventory
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 0, 5, 0);
        categoryPane.add(new JScrollPane(this.categoryInventory), gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        categoryPane.add(this.deleteCategory, gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        categoryPane.add(this.categoryList, gbc);
        return categoryPane;
    }

    private JPanel createAreaAddingPane() {
        final JPanel areaAddingPane = new JPanel(new GridBagLayout());
        this.areas = new JList<>(new DefaultListModel<>());
        this.addLoation = new JButton("Add Area");
        GridBagConstraints gbc;
        this.areas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 5, 0);
        areaAddingPane.add(new JScrollPane(this.areas), gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        areaAddingPane.add(this.addLoation, gbc);
        return areaAddingPane;
    }

    private JPanel createCategoryAddingPane() {
        final JPanel categoryAddingPane = new JPanel(new GridBagLayout());
        this.areasAddList = new JList<>(new DefaultListModel<>());
        this.categoryName = new JTextField();
        this.addCategory = new JButton("Create Category");
        this.removeArea = new JButton("Remove Area");
        final JLabel categoryNameL = new JLabel("Category name:");
        GridBagConstraints gbc;
        this.areasAddList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);
        categoryAddingPane.add(new JScrollPane(this.areasAddList), gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipadx = 150;
        gbc.anchor = GridBagConstraints.EAST;
        categoryAddingPane.add(this.categoryName, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        categoryAddingPane.add(categoryNameL, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.EAST;
        categoryAddingPane.add(this.addCategory, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        categoryAddingPane.add(this.removeArea, gbc);
        return categoryAddingPane;
    }

    private void addActions() {
        this.categoryList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (LocationCategoryCustomizer.this.categoryList
                        .getSelectedItem() != null) {
                    ((DefaultListModel<String>) LocationCategoryCustomizer.this.categoryInventory
                            .getModel()).removeAllElements();
                    for (final String s : LocationCategoryCustomizer.this.categoryList
                            .getItemAt(
                                    LocationCategoryCustomizer.this.categoryList
                                            .getSelectedIndex())
                            .getTurnAreaNames()) {
                        ((DefaultListModel<String>) LocationCategoryCustomizer.this.categoryInventory
                                .getModel()).addElement(s);
                    }
                }
            }
        });
        this.addLoation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (!LocationCategoryCustomizer.this.areas.isSelectionEmpty()) {
                    ((DefaultListModel<String>) LocationCategoryCustomizer.this.areasAddList
                            .getModel())
                            .addElement(LocationCategoryCustomizer.this.areas
                                    .getSelectedValue());
                    ((DefaultListModel<String>) LocationCategoryCustomizer.this.areas
                            .getModel())
                            .remove(LocationCategoryCustomizer.this.areas
                                    .getSelectedIndex());
                    LocationCategoryCustomizer.this.areas.setSelectedIndex(0);
                    LocationCategoryCustomizer.this.areasAddList
                            .setSelectedIndex(0);
                }
            }
        });
        this.areas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (!LocationCategoryCustomizer.this.areas
                            .isSelectionEmpty()) {
                        ((DefaultListModel<String>) LocationCategoryCustomizer.this.areasAddList
                                .getModel())
                                .addElement(LocationCategoryCustomizer.this.areas
                                        .getSelectedValue());
                        ((DefaultListModel<String>) LocationCategoryCustomizer.this.areas
                                .getModel())
                                .remove(LocationCategoryCustomizer.this.areas
                                        .getSelectedIndex());
                        LocationCategoryCustomizer.this.areas
                                .setSelectedIndex(0);
                        LocationCategoryCustomizer.this.areasAddList
                                .setSelectedIndex(0);
                    }
                }
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
            }

            @Override
            public void mouseExited(final MouseEvent e) {
            }

            @Override
            public void mousePressed(final MouseEvent e) {
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
            }
        });
        this.removeArea.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (!LocationCategoryCustomizer.this.areasAddList
                        .isSelectionEmpty()) {
                    LocationCategoryCustomizer.this
                            .addLocation(LocationCategoryCustomizer.this.areasAddList
                                    .getSelectedValue());
                    ((DefaultListModel<String>) LocationCategoryCustomizer.this.areasAddList
                            .getModel())
                            .remove(LocationCategoryCustomizer.this.areasAddList
                                    .getSelectedIndex());
                    LocationCategoryCustomizer.this.areas.setSelectedIndex(0);
                    LocationCategoryCustomizer.this.areasAddList
                            .setSelectedIndex(0);
                }
            }
        });
        this.areasAddList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (!LocationCategoryCustomizer.this.areasAddList
                            .isSelectionEmpty()) {
                        LocationCategoryCustomizer.this
                                .addLocation(LocationCategoryCustomizer.this.areasAddList
                                        .getSelectedValue());
                        ((DefaultListModel<String>) LocationCategoryCustomizer.this.areasAddList
                                .getModel())
                                .remove(LocationCategoryCustomizer.this.areasAddList
                                        .getSelectedIndex());
                        LocationCategoryCustomizer.this.areas
                                .setSelectedIndex(0);
                        LocationCategoryCustomizer.this.areasAddList
                                .setSelectedIndex(0);
                    }
                }
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
            }

            @Override
            public void mouseExited(final MouseEvent e) {
            }

            @Override
            public void mousePressed(final MouseEvent e) {
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
            }
        });
        this.addCategory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                LocationCategoryCustomizer.this.addCategory();
            }
        });
        this.categoryName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    LocationCategoryCustomizer.this.addCategory();
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
            }

            @Override
            public void keyTyped(final KeyEvent e) {
            }
        });
        this.deleteCategory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (LocationCategoryCustomizer.this.categoryList
                        .getSelectedItem() != null) {
                    LocationCategoryCustomizer.this.turnrundownChart
                            .getCategories()
                            .remove(LocationCategoryCustomizer.this.categoryList
                                    .getSelectedIndex());
                    LocationCategoryCustomizer.this.updateData();
                }
            }
        });
        this.updateChart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                LocationCategoryCustomizer.this.dispose();
            }
        });
        this.loadCategoryView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int state = LocationCategoryCustomizer.this.viewChooser
                        .showOpenDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    try {
                        LocationCategoryCustomizer.this.turnrundownChart.setCategories(CategoryViewFileHandler
                                .parseOutCategories(LocationCategoryCustomizer.this.viewChooser
                                        .getSelectedFile()));
                        LocationCategoryCustomizer.this.updateData();
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        this.saveCategoryView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int state = LocationCategoryCustomizer.this.viewChooser
                        .showSaveDialog(null);
                if (state == JFileChooser.APPROVE_OPTION) {
                    try {
                        CategoryViewFileHandler
                                .createCategoryViewFile(
                                        LocationCategoryCustomizer.this.turnrundownChart
                                                .getCategories(),
                                        LocationCategoryCustomizer.this.viewChooser
                                                .getSelectedFile());
                    } catch (final IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void addToolTips() {
        this.deleteCategory
                .setToolTipText("Delete the currently selected category");
        this.addLoation
                .setToolTipText("Add an area to the current category in construction");
        this.removeArea
                .setToolTipText("Remove an area from the current category in construction");
        this.addCategory
                .setToolTipText("<html>Saves the category in the category list. Note that you"
                        + "<p>need to name a category before you can save it.</p></html>");
        this.updateChart
                .setToolTipText("<html>Closes this window and updates the turn rundown gantt"
                        + "<p>chart with the new category configuration.</p></html>");
        this.loadCategoryView
                .setToolTipText("Load a previously saved category configuration");
        this.saveCategoryView
                .setToolTipText("Save the current category configuration to a file for future use");
        this.categoryName
                .setToolTipText("<html>Name of the current category in construction. Note that you"
                        + "<p>need to name a category before you can save it.</p></html>");
        this.categoryList.setToolTipText("List of categories currently used");
        this.categoryInventory
                .setToolTipText("List of all areas inside the currently selected category");
        this.areas
                .setToolTipText("<html>All uncategorized areas. Every location in here will"
                        + "<p>be its own category in the turn rundown gantt chart.</p></html>");
        this.areasAddList
                .setToolTipText("List of all areas inside the category currently in construction");
    }

    void updateData() {
        ((DefaultListModel<String>) this.areas.getModel()).removeAllElements();
        ((DefaultListModel<String>) this.areasAddList.getModel())
                .removeAllElements();
        ((DefaultListModel<String>) this.categoryInventory.getModel())
                .removeAllElements();
        this.categoryName.setText("");
        this.updateCategoryList();
        final List<String> areaNames = new ArrayList<>(200);
        for (final TurnInterval ti : this.turnrundownChart.getLogData()
                .getTurnsSpent()) {
            final boolean isInLocationList = areaNames.contains(ti
                    .getAreaName());
            if (!isInLocationList && !this.isInCategories(ti.getAreaName())) {
                areaNames.add(ti.getAreaName());
            }
        }
        Collections.sort(areaNames);
        for (final String s : areaNames) {
            ((DefaultListModel<String>) this.areas.getModel()).addElement(s);
        }
        this.areas.setSelectedIndex(0);
    }

    private void updateCategoryList() {
        this.categoryList.removeAllItems();
        for (final TurnAreaCategory tlc : this.turnrundownChart.getCategories()) {
            this.categoryList.addItem(tlc);
        }
    }

    private boolean isInCategories(final String area) {
        for (int i = 0; i < this.categoryList.getItemCount(); i++) {
            for (final String s : this.categoryList.getItemAt(i)
                    .getTurnAreaNames()) {
                if (area.startsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    void addLocation(final String areaName) {
        final List<String> areaNames = new ArrayList<>(200);
        for (int i = 0; i < ((DefaultListModel<String>) this.areas.getModel())
                .getSize(); i++) {
            areaNames.add(((DefaultListModel<String>) this.areas.getModel())
                    .get(i));
        }
        areaNames.add(areaName);
        Collections.sort(areaNames);
        ((DefaultListModel<String>) this.areas.getModel()).removeAllElements();
        for (final String s : areaNames) {
            ((DefaultListModel<String>) this.areas.getModel()).addElement(s);
        }
    }

    void addCategory() {
        if (!this.areasAddList.isSelectionEmpty()
                && !this.categoryName.getText().equals("")) {
            final TurnAreaCategory tac = new TurnAreaCategory(
                    this.categoryName.getText());
            for (int i = 0; i < ((DefaultListModel<String>) this.areasAddList
                    .getModel()).getSize(); i++) {
                tac.addTurnAreaName(((DefaultListModel<String>) this.areasAddList
                        .getModel()).get(i));
            }
            this.turnrundownChart.addCategory(tac);
            this.updateData();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        this.turnrundownChart.updateChart();
    }
}
