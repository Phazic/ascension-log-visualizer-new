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

import java.util.HashSet;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.AbstractTurn;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator;
import com.googlecode.logVisualizer.util.textualLogs.TextLogCreator.TextualLogVersion;

/**
 * A useful and mostly self-contained class that can be used to view a more
 * detailed report of an ascension log.
 */
final class DetailedLogViewer extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -1426856289964754334L;
    private final JEditorPane editorPane = new JEditorPane();
    private final String htmlLog;

    /**
     * Constructs and opens a frame with a more detailed view of the given
     * ascension log.
     */
    DetailedLogViewer(final LogDataHolder logData) {
        super("DetailedLogViewer");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (logData == null) {
            throw new NullPointerException(
                    "The log data holder must not be null.");
        }
        this.htmlLog = TextLogCreator.getTextualLog(logData,
                TextualLogVersion.HTML_LOG);
        final JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.editorPane.setEditable(false);
        this.editorPane.setContentType("text/html");
        splitter.setLeftComponent(new JScrollPane(this
                .createTurnRundownTree(logData)));
        splitter.setRightComponent(new JScrollPane(this.editorPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        splitter.setDividerLocation(300);
        this.setContentPane(splitter);
        this.setSize(800, 600);
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    /**
     * Creates the turn rundown tree menu to navigate between all turns.
     */
    private JTree createTurnRundownTree(final LogDataHolder logData) {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                logData.getLogName());
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            final DefaultMutableTreeNode tiRoot = new DefaultMutableTreeNode(
                    new TurnIntervalContainer(ti));
            for (final SingleTurn st : ti.getTurns()) {
                tiRoot.add(new DefaultMutableTreeNode(new SingleTurnContainer(
                        st)));
            }
            root.add(tiRoot);
        }
        final JTree tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(final TreeSelectionEvent e) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();
                if (node != null) {
                    final Object nodeContents = node.getUserObject();
                    if (nodeContents instanceof TurnContainer) {
                        DetailedLogViewer.this.editorPane.setText(DetailedLogViewer
                                .createInformationString((TurnContainer) nodeContents));
                    } else {
                        DetailedLogViewer.this.editorPane
                                .setText(DetailedLogViewer.this.htmlLog);
                    }
                    DetailedLogViewer.this.editorPane.setCaretPosition(0);
                }
            }
        });
        tree.setSelectionInterval(0, 0);
        return tree;
    }

    static String createInformationString(final TurnContainer tc) {
        return tc.getTurnObject() instanceof TurnInterval ? DetailedLogViewer
                .createTurnIntervalInfoString((TurnInterval) tc.getTurnObject())
                : DetailedLogViewer.createSingleTurnInfoString((SingleTurn) tc
                        .getTurnObject());
    }

    private static String createTurnIntervalInfoString(final TurnInterval ti) {
        final StringBuilder str = new StringBuilder(2000);
        str.append("<html>");
        // Caption
        str.append("<h1>");
        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        if (ti.getTotalTurns() > 1) {
            str.append(ti.getStartTurn() + 1);
            str.append(UsefulPatterns.MINUS);
        }
        str.append(ti.getEndTurn());
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(ti.getAreaName());
        str.append("</h1><p>");
        // General data
        str.append("<h2>");
        str.append("General Data");
        str.append("</h2><br>");
        str.append("Meat gained inside encounters: "
                + ti.getEncounterMeatGain() + "<br>");
        str.append("Meat gained outside encounters: " + ti.getOtherMeatGain()
                + "<br>");
        str.append("Meat spent: " + ti.getMeatSpent() + "<br>");
        str.append("Stat gains: " + ti.getStatGain() + "<br>");
        str.append("Stat gains (including consumables): "
                + ti.getTotalStatGain() + "<br>");
        str.append("Free runaways: " + ti.getFreeRunaways() + "<br>");
        int mpCosts = 0;
        for (final Skill s : ti.getSkillsCast()) {
            mpCosts += s.getMpCost();
        }
        str.append("MP spent: " + mpCosts + "<br>");
        final Set<String> usedFamiliars = new HashSet<>();
        for (final SingleTurn st : ti.getTurns()) {
            usedFamiliars.add(st.getUsedFamiliar().getFamiliarName());
        }
        str.append("Used familiars: ");
        for (final String s : usedFamiliars) {
            str.append(s + ", ");
        }
        str.replace(str.length() - 2, str.length(), "");
        str.append("<br>");
        str.append("Number of encounters: " + ti.getTotalTurns() + "<br>");
        str.append("Number of dropped items: " + ti.getDroppedItems().size()
                + "<br>");
        str.append("Number of consumables used: "
                + ti.getConsumablesUsed().size() + "<br>");
        str.append("Number of skills cast: " + ti.getSkillsCast().size());
        str.append("<p>");
        // Encounter list
        str.append("<h2>");
        str.append("Encounters");
        str.append("</h2><br>");
        for (final SingleTurn st : ti.getTurns()) {
            str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            str.append(st.getTurnNumber());
            str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(st.getEncounterName());
            str.append("<br>");
        }
        str.append("<p>");
        str.append(DetailedLogViewer.createDetailedTurnInfoString(ti));
        str.append("</html>");
        return str.toString();
    }

    private static String createSingleTurnInfoString(final SingleTurn st) {
        final StringBuilder str = new StringBuilder(1000);
        str.append("<html>");
        // Caption
        str.append("<h1>");
        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        str.append(st.getTurnNumber());
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(st.getEncounterName());
        str.append("</h1><p>");
        // General data
        str.append("<h2>");
        str.append("General Data");
        str.append("</h2><br>");
        str.append("Meat gained inside the encounter: "
                + st.getEncounterMeatGain() + "<br>");
        str.append("Meat gained outside the encounter: "
                + st.getOtherMeatGain() + "<br>");
        str.append("Meat spent: " + st.getMeatSpent() + "<br>");
        str.append("Stat gains: " + st.getStatGain() + "<br>");
        str.append("Stat gains (including consumables): "
                + st.getTotalStatGain() + "<br>");
        int mpCosts = 0;
        for (final Skill s : st.getSkillsCast()) {
            mpCosts += s.getMpCost();
        }
        str.append("MP spent: " + mpCosts + "<br>");
        str.append("Familiar: " + st.getUsedFamiliar().getFamiliarName()
                + "<br>");
        str.append("Number of dropped items: " + st.getDroppedItems().size()
                + "<br>");
        str.append("Number of consumables used: "
                + st.getConsumablesUsed().size() + "<br>");
        str.append("Number of skills cast: " + st.getSkillsCast().size());
        str.append("<p>");
        // Equipment
        str.append("<h2>");
        str.append("Equipment");
        str.append("</h2><br>");
        final EquipmentChange equip = st.getUsedEquipment();
        str.append("Hat: " + equip.getHat() + "<br>");
        str.append("Weapon: " + equip.getWeapon() + "<br>");
        str.append("Offhand: " + equip.getOffhand() + "<br>");
        str.append("Shirt: " + equip.getShirt() + "<br>");
        str.append("Pants: " + equip.getPants() + "<br>");
        str.append("Acc1: " + equip.getAcc1() + "<br>");
        str.append("Acc2: " + equip.getAcc2() + "<br>");
        str.append("Acc2: " + equip.getAcc3() + "<br>");
        str.append("Familiar equip: " + equip.getFamEquip());
        str.append("<p>");
        str.append(DetailedLogViewer.createDetailedTurnInfoString(st));
        str.append("</html>");
        return str.toString();
    }

    private static String createDetailedTurnInfoString(final AbstractTurn t) {
        final StringBuilder str = new StringBuilder(1000);
        // Dropped items
        str.append("<h2>");
        str.append("Dropped Items");
        str.append("</h2><br>");
        for (final Item i : t.getDroppedItems()) {
            str.append(i + "<br>");
        }
        str.append("<p>");
        // Consumables used
        str.append("<h2>");
        str.append("Consumables Used");
        str.append("</h2><br>");
        for (final Consumable c : t.getConsumablesUsed()) {
            str.append(c + "<br>");
        }
        str.append("<p>");
        // Skills cast
        str.append("<h2>");
        str.append("Skills Cast");
        str.append("</h2><br>");
        for (final Skill s : t.getSkillsCast()) {
            str.append(s.getAmount());
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(s.getName());
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(UsefulPatterns.ROUND_BRACKET_OPEN);
            str.append(s.getMpCost());
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append("MP");
            str.append(UsefulPatterns.ROUND_BRACKET_CLOSE);
            str.append("<br>");
        }
        str.append("<p>");
        // MP summary
        final MPGain mpGains = t.getMPGain();
        str.append("<h2>");
        str.append("MP Gains");
        str.append("</h2><br>");
        str.append("Total mp gained: " + mpGains.getTotalMPGains() + "<br><br>");
        str.append("Inside Encounters: " + mpGains.getEncounterMPGain()
                + "<br>");
        str.append("Starfish Familiars: " + mpGains.getStarfishMPGain()
                + "<br>");
        str.append("Resting: " + mpGains.getRestingMPGain() + "<br>");
        str.append("Outside Encounters: " + mpGains.getOutOfEncounterMPGain()
                + "<br>");
        str.append("Consumables: " + mpGains.getConsumableMPGain() + "<br>");
        str.append("<p>");
        return str.toString();
    }

    private static interface TurnContainer {
        AbstractTurn getTurnObject();
    }

    private static class TurnIntervalContainer implements TurnContainer {
        private final TurnInterval ti;

        TurnIntervalContainer(final TurnInterval ti) {
            this.ti = ti;
        }

        @Override
        public AbstractTurn getTurnObject() {
            return this.ti;
        }

        @Override
        public String toString() {
            final StringBuilder str = new StringBuilder(50);
            str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            if (this.ti.getTotalTurns() > 1) {
                str.append(this.ti.getStartTurn() + 1);
                str.append(UsefulPatterns.MINUS);
            }
            str.append(this.ti.getEndTurn());
            str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(this.ti.getAreaName());
            return str.toString();
        }
    }

    private static class SingleTurnContainer implements TurnContainer {
        private final SingleTurn st;

        SingleTurnContainer(final SingleTurn st) {
            this.st = st;
        }

        @Override
        public AbstractTurn getTurnObject() {
            return this.st;
        }

        @Override
        public String toString() {
            final StringBuilder str = new StringBuilder(40);
            str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
            str.append(this.st.getTurnNumber());
            str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(this.st.getEncounterName());
            return str.toString();
        }
    }
}
