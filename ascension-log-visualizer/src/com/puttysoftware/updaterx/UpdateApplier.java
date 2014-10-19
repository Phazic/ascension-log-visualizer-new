package com.puttysoftware.updaterx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateApplier {
    public static void downloadAndApplyUpdate(final String path,
            final String programName, final String versionSuffix,
            final URL jarfile, final String blurb, final String oldVersionSuffix)
            throws IOException {
        // Detect platform
        final String platform = System.getProperty("os.name");
        final boolean mac = platform.contains("Mac");
        final boolean windows = platform.contains("Windows");
        final boolean linux = !mac && !windows;
        // Set output file name
        final String outFileName = programName + "_" + versionSuffix + ".jar";
        final String oldFileName = programName + "_" + oldVersionSuffix
                + ".jar";
        final URLConnection conn = jarfile.openConnection();
        final int len = conn.getContentLength();
        boolean stagerExists = false;
        if (linux) {
            if (new File(path + File.separator + "UpdateStager.jar").exists()) {
                stagerExists = true;
            }
        } else {
            if (new File("UpdateStager.jar").exists()) {
                stagerExists = true;
            }
        }
        if (!stagerExists) {
            // Download stager
            final URLConnection sconn = new URL(
                    "http://download.puttysoftware.com/stager/UpdateStager.jar")
                    .openConnection();
            final int slen = conn.getContentLength();
            if (linux) {
                try (InputStream jarin = sconn.getInputStream();
                        FileOutputStream jarout = new FileOutputStream(path
                                + File.separator + "UpdateStager.jar")) {
                    final UDLProgressTask udlpt = new UDLProgressTask(slen,
                            jarin, jarout);
                    udlpt.start();
                    try {
                        udlpt.join();
                    } catch (final InterruptedException e) {
                        // Ignore
                    }
                    stagerExists = true;
                } catch (final IOException ioe) {
                    // Ignore
                }
            } else {
                try (InputStream jarin = sconn.getInputStream();
                        FileOutputStream jarout = new FileOutputStream(
                                "UpdateStager.jar")) {
                    final UDLProgressTask udlpt = new UDLProgressTask(slen,
                            jarin, jarout);
                    udlpt.start();
                    try {
                        udlpt.join();
                    } catch (final InterruptedException e) {
                        // Ignore
                    }
                    stagerExists = true;
                } catch (final IOException ioe) {
                    // Ignore
                }
            }
        }
        // Download update
        if (linux) {
            try (InputStream jarin = conn.getInputStream();
                    FileOutputStream jarout = new FileOutputStream(path
                            + File.separator + outFileName)) {
                final UDLProgressTask udlpt = new UDLProgressTask(len, jarin,
                        jarout);
                udlpt.start();
                // Display release notes, if nonempty
                if (!blurb.isEmpty()) {
                    new UpdateMessageWindow(blurb, programName
                            + " Release Notes").showMessage();
                }
                try {
                    udlpt.join();
                } catch (final InterruptedException e) {
                    // Ignore
                }
            } catch (final IOException ioe) {
                throw ioe;
            }
        } else {
            try (InputStream jarin = conn.getInputStream();
                    FileOutputStream jarout = new FileOutputStream(outFileName)) {
                final UDLProgressTask udlpt = new UDLProgressTask(len, jarin,
                        jarout);
                udlpt.start();
                // Display release notes, if nonempty
                if (!blurb.isEmpty()) {
                    new UpdateMessageWindow(blurb, programName
                            + " Release Notes").showMessage();
                }
                try {
                    udlpt.join();
                } catch (final InterruptedException e) {
                    // Ignore
                }
            } catch (final IOException ioe) {
                throw ioe;
            }
        }
        if (stagerExists) {
            // Start stager
            if (linux) {
                final ProcessBuilder pb = new ProcessBuilder("java", "-jar",
                        path + File.separator + "UpdateStager.jar", path
                                + File.separator + oldFileName, path
                                + File.separator + outFileName);
                pb.start();
            } else if (mac) {
                if (new File(
                        "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java")
                        .canExecute()) {
                    final ProcessBuilder pb = new ProcessBuilder(
                            "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java",
                            "-jar", "UpdateStager.jar", oldFileName,
                            outFileName);
                    pb.start();
                } else {
                    final ProcessBuilder pb = new ProcessBuilder("java",
                            "-jar", "UpdateStager.jar", oldFileName,
                            outFileName);
                    pb.start();
                }
            } else {
                final ProcessBuilder pb = new ProcessBuilder("java", "-jar",
                        path + File.separator + "UpdateStager.jar",
                        oldFileName, outFileName);
                pb.start();
            }
        } else {
            // Start program
            if (linux) {
                final ProcessBuilder pb = new ProcessBuilder("java", "-jar",
                        path + File.separator + outFileName);
                pb.start();
            } else if (mac) {
                if (new File(
                        "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java")
                        .canExecute()) {
                    final ProcessBuilder pb = new ProcessBuilder(
                            "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java",
                            "-jar", outFileName);
                    pb.start();
                } else {
                    final ProcessBuilder pb = new ProcessBuilder("java",
                            "-jar", outFileName);
                    pb.start();
                }
            } else {
                final ProcessBuilder pb = new ProcessBuilder("java", "-jar",
                        outFileName);
                pb.start();
            }
        }
        // Get out of here
        System.exit(0);
    }
}
