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

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.turnAction.Pull;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.LookAheadIterator;

final class PullsPanel extends JTabbedPane {
    /**
     *
     */
    private static final long serialVersionUID = 522820713091367471L;
    private static final String NEW_LINE = "\n";

    /**
     * @param logData
     *            The {@link LogDataHolder} with all the data of the ascension
     *            log.
     */
    PullsPanel(final LogDataHolder logData) {
        super(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        final LookAheadIterator<Pull> index = new LookAheadIterator<>(logData
                .getPulls().iterator());
        while (index.hasNext()) {
            final int currentDay = index.peek().getDayNumber();
            final JTextArea lister = new JTextArea();
            PullsPanel.printPull(lister, index.next());
            while (index.hasNext()
                    && (currentDay == index.peek().getDayNumber())) {
                PullsPanel.printPull(lister, index.next());
            }
            this.add("Day " + currentDay, new JScrollPane(lister));
        }
    }

    private static void printPull(final JTextArea lister, final Pull currentItem) {
        lister.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        lister.append(String.valueOf(currentItem.getTurnNumber()));
        lister.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        lister.append(UsefulPatterns.WHITE_SPACE);
        lister.append(String.valueOf(currentItem.getAmount()));
        lister.append(UsefulPatterns.WHITE_SPACE);
        lister.append(currentItem.getItemName());
        lister.append(PullsPanel.NEW_LINE);
    }
}
