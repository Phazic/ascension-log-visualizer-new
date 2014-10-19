package com.puttysoftware.updaterx;

import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class UDLProgressTask extends Thread {
    // Fields
    private final JFrame progressFrame;
    private final JProgressBar progressBar;
    private final InputStream jarin;
    private final FileOutputStream jarout;
    private final int dlmax;
    private Exception dlErr;

    // Constructors
    public UDLProgressTask(final int max, final InputStream in,
            final FileOutputStream out) {
        this.dlmax = max;
        this.jarin = in;
        this.jarout = out;
        this.progressFrame = new JFrame("Downloading Update...");
        this.progressBar = new JProgressBar();
        this.progressFrame.getContentPane().add(this.progressBar);
        this.progressFrame.setAlwaysOnTop(true);
        this.progressFrame.setResizable(false);
        if (max == -1) {
            this.progressBar.setIndeterminate(true);
        }
        this.progressFrame.pack();
    }

    @Override
    public void run() {
        this.progressFrame.setVisible(true);
        try {
            final byte[] buf = new byte[1024];
            int counter = 0;
            int bcounter = 0;
            while (bcounter != -1) {
                bcounter = this.jarin.read(buf, 0, buf.length);
                if (bcounter != -1) {
                    this.jarout.write(buf, 0, bcounter);
                    counter += bcounter;
                    if (this.dlmax != -1) {
                        final int oldVal = this.progressBar.getValue();
                        final int newVal = counter * 100 / this.dlmax;
                        if (oldVal != newVal) {
                            this.progressBar.setValue(newVal);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            this.dlErr = e;
        }
        this.progressFrame.setVisible(false);
    }

    public Exception dlError() {
        return this.dlErr;
    }
}
