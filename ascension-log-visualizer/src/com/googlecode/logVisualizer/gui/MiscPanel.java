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
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataNumberPair;

final class MiscPanel extends JTabbedPane {
    /**
     * 
     */
    private static final long serialVersionUID = -3618978649327096924L;
    private static final String NEW_LINE = "\n";

    /**
     * @param logData
     *            The {@link LogDataHolder} with all the data of the ascension
     *            log.
     */
    MiscPanel(final LogDataHolder logData) {
        super();
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.addTab("Semirares", MiscPanel.createSummaryPanel(logData
                .getLogSummary().getSemirares()));
        this.addTab("Bad Moon", MiscPanel.createSummaryPanel(logData
                .getLogSummary().getBadmoonAdventures()));
        this.addTab("Hunted Combats", MiscPanel.createSummaryPanel(logData
                .getLogSummary().getHuntedCombats()));
        this.addTab("Yellow Rays", MiscPanel
                .createSummaryPanel(logData.getLogSummary()
                        .getDisintegratedCombats()));
        this.addTab("Spooky Putty Usage",
                MiscPanel.createSpookyPuttySummaryPanel(logData));
        this.addTab("Lost Combats",
                MiscPanel.createSummaryPanel(logData.getLostCombats()));
    }

    private static JPanel createSummaryPanel(
            final Collection<DataNumberPair<String>> summaryData) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JTextArea lister = new JTextArea();
        for (final DataNumberPair<String> dn : summaryData) {
            lister.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            lister.append(dn.getNumber().toString());
            lister.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            lister.append(UsefulPatterns.WHITE_SPACE);
            lister.append(dn.getData());
            lister.append(MiscPanel.NEW_LINE);
        }
        panel.add(new JScrollPane(lister), BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createSpookyPuttySummaryPanel(
            final LogDataHolder logData) {
        // Spooky Putty Monster
        final JPanel panel = new JPanel(new BorderLayout());
        final JTextArea lister = new JTextArea();
        TurnInterval prevTi = null;
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            if (ti.getAreaName().equals("Spooky Putty Monster")) {
                // If the single turns collection is empty (for example because
                // base data was from a pre-parsed log), give the name of the
                // previous adventuring area as a guess so the user has at least
                // some kind of feedback, otherwise use the existing encounter
                // names.
                if (ti.getTurns().isEmpty()) {
                    if (prevTi != null) {
                        final String description = "possibly a monster from "
                                + prevTi.getAreaName();
                        for (int i = ti.getStartTurn(); i <= ti.getEndTurn(); i++) {
                            MiscPanel.addPuttyMonsterLine(lister, i,
                                    description);
                        }
                    }
                } else {
                    for (final SingleTurn st : ti.getTurns()) {
                        MiscPanel.addPuttyMonsterLine(lister,
                                st.getTurnNumber(), st.getEncounterName());
                    }
                }
            }
            prevTi = ti;
        }
        panel.add(new JScrollPane(lister), BorderLayout.CENTER);
        return panel;
    }

    private static final void addPuttyMonsterLine(final JTextArea lister,
            final int turnNumber, final String description) {
        lister.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        lister.append(String.valueOf(turnNumber));
        lister.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        lister.append(UsefulPatterns.WHITE_SPACE);
        lister.append(description);
        lister.append(MiscPanel.NEW_LINE);
    }
}