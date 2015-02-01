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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.googlecode.logVisualizer.chart.turnrundownGantt.FamiliarColor;
import com.googlecode.logVisualizer.chart.turnrundownGantt.FamiliarColor.Colors;

/**
 * A very basic implementation of a table model which is able to handle the
 * familiar colourisation of ascension logs.
 */
final class FamiliarCustomizationTableModel extends AbstractTableModel {
    /**
     *
     */
    private static final long serialVersionUID = -1221006585852064568L;
    private static final String[] COLUMN_NAMES = { "Familiar", "Color used" };
    private List<FamiliarColor> familiars;

    FamiliarCustomizationTableModel(final Collection<FamiliarColor> familiars) {
        super();
        this.familiars = new ArrayList<>(familiars);
    }

    void addFamiliar(final FamiliarColor familiar) {
        if (this.familiars == null) {
            this.familiars = new ArrayList<>();
        }
        this.familiars.add(familiar);
    }

    void setFamiliars(final List<FamiliarColor> familiars) {
        this.familiars = familiars;
    }

    List<FamiliarColor> getFamiliars() {
        return this.familiars;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
            final int columnIndex) {
        this.familiars.get(rowIndex).setColor((Colors) aValue);
    }

    @Override
    public String getColumnName(final int column) {
        return FamiliarCustomizationTableModel.COLUMN_NAMES[column];
    }

    @Override
    public int getColumnCount() {
        return FamiliarCustomizationTableModel.COLUMN_NAMES.length;
    }

    @Override
    public int getRowCount() {
        return this.familiars.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        return columnIndex == 0 ? this.familiars.get(rowIndex)
                .getFamiliarName() : this.familiars.get(rowIndex).getColor();
    }
}
