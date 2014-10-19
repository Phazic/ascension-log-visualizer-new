package com.puttysoftware.updaterx;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class UpdateMessageWindow {
    // Fields
    private final JOptionPane optionPane;
    private final JDialog dialog;
    private final JScrollPane scrollPane;
    private static final int MAX_SIZE = 700;
    private static final int WORD_WRAP = 80;

    // Constructor
    public UpdateMessageWindow(final String message, final String title) {
        this.optionPane = new JOptionPane(MessageFixer.fix(message,
                UpdateMessageWindow.WORD_WRAP), JOptionPane.INFORMATION_MESSAGE);
        this.dialog = this.optionPane.createDialog(null, title);
        this.scrollPane = new JScrollPane(this.dialog.getContentPane());
        this.dialog.setContentPane(this.scrollPane);
        this.dialog.setAlwaysOnTop(true);
        this.dialog.setResizable(false);
        this.dialog.setLocationByPlatform(true);
        this.dialog.pack();
        int hsize = this.dialog.getWidth();
        int vsize = this.dialog.getHeight();
        if (hsize > UpdateMessageWindow.MAX_SIZE) {
            hsize = UpdateMessageWindow.MAX_SIZE;
        }
        if (vsize > UpdateMessageWindow.MAX_SIZE) {
            vsize = UpdateMessageWindow.MAX_SIZE;
        }
        this.dialog.setSize(hsize, vsize);
    }

    // Methods
    public void showMessage() {
        this.dialog.setVisible(true);
    }
}
