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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import org.jfree.ui.RefineryUtilities;

import com.googlecode.logVisualizer.Settings;

/**
 * An options dialog to change the used Look&Feel of the Ascension Log
 * Visualizer.
 */
final class LafChangerDialog extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = -8154189324762430685L;
    private final JComboBox<String> lafLister;

    LafChangerDialog(final JFrame owner) {
        super(owner, true);
        this.setLayout(new BorderLayout(0, 10));
        this.setTitle("Look&Feel changer");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.lafLister = new JComboBox<>();
        this.lafLister.setEditable(false);
        this.addLafs();
        final JButton okButton = new JButton("OK");
        final JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                LafChangerDialog.this.changeUsedLaf();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                LafChangerDialog.this.dispose();
            }
        });
        this.add(this.lafLister, BorderLayout.NORTH);
        this.add(
                new JLabel(
                        "Note that changes to the Look&Feel will only take effect after a restart of the program."),
                BorderLayout.CENTER);
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        buttonPanel.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    private void addLafs() {
        for (final LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
            this.lafLister.addItem(lafi.getName());
        }
        this.lafLister
                .setSelectedItem(Settings.getSettingString("LookAndFeel"));
    }

    void changeUsedLaf() {
        Settings.setSettingString("LookAndFeel",
                (String) this.lafLister.getSelectedItem());
        this.dispose();
    }
}
