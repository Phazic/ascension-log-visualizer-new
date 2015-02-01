package com.puttysoftware.updaterx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.puttysoftware.commondialogs.CommonDialogs;

public class UpdateChecker {
    /**
     * @author wrldwzrd89
     * @version 8.0.0
     */
    // Fields and Constants
    private final ProductData prod;
    private boolean cachedDB;
    private final String cacheFile;
    private final String cacheTTLFile;
    private long cacheTTL;
    private final boolean prereleaseMode;
    private static final String MAC_CACHE_PREFIX = "HOME";
    private static final String WIN_CACHE_PREFIX = "APPDATA";
    private static final String UNIX_CACHE_PREFIX = "HOME";
    private static final String MAC_CACHE_DIR = "/Library/Caches/";
    private static final String WIN_CACHE_DIR = "\\Caches\\";
    private static final String UNIX_CACHE_DIR = "/caches/";
    private static final long CACHE_TTL_3_HOURS = 10800000L;
    private static final long CACHE_TTL_6_HOURS = 21600000L;
    private static final long CACHE_TTL_12_HOURS = 43200000L;
    private static final long CACHE_TTL_1_DAY = 86400000L;
    private static final long CACHE_TTL_2_DAYS = 172800000L;
    private static final long CACHE_TTL_4_DAYS = 345600000L;
    private static final long CACHE_TTL_1_WEEK = 604800000L;
    public static final long[] TTL_VALUES = new long[] {
            UpdateChecker.CACHE_TTL_3_HOURS, UpdateChecker.CACHE_TTL_6_HOURS,
            UpdateChecker.CACHE_TTL_12_HOURS, UpdateChecker.CACHE_TTL_1_DAY,
            UpdateChecker.CACHE_TTL_2_DAYS, UpdateChecker.CACHE_TTL_4_DAYS,
            UpdateChecker.CACHE_TTL_1_WEEK };

    // Constructors
    public UpdateChecker(final ProductData data) {
        this.prod = data;
        this.cachedDB = false;
        final int code = this.prod.getCodeVersion();
        this.prereleaseMode = code < ProductData.CODE_STABLE;
        String rt;
        if (code == ProductData.CODE_NIGHTLY) {
            rt = "nightly";
        } else if (code == ProductData.CODE_ALPHA) {
            rt = "alpha";
        } else if (code == ProductData.CODE_BETA) {
            rt = "beta";
        } else if (code == ProductData.CODE_CANDIDATE) {
            rt = "rc";
        } else if (code == ProductData.CODE_STABLE) {
            rt = "stable";
        } else {
            rt = "mature";
        }
        this.cacheFile = this.prod.getProductName() + "_" + rt + "updatecache";
        this.cacheTTLFile = this.prod.getProductName() + "_" + rt + "updatettl";
        this.cacheTTL = UpdateChecker.CACHE_TTL_1_DAY;
    }

    public UpdateChecker(final ProductData data, final long TTL) {
        this.prod = data;
        this.cachedDB = false;
        final int code = this.prod.getCodeVersion();
        this.prereleaseMode = code < ProductData.CODE_STABLE;
        String rt;
        if (code == ProductData.CODE_NIGHTLY) {
            rt = "nightly";
        } else if (code == ProductData.CODE_ALPHA) {
            rt = "alpha";
        } else if (code == ProductData.CODE_BETA) {
            rt = "beta";
        } else if (code == ProductData.CODE_CANDIDATE) {
            rt = "rc";
        } else if (code == ProductData.CODE_STABLE) {
            rt = "stable";
        } else {
            rt = "mature";
        }
        this.cacheFile = this.prod.getProductName() + "_" + rt + "updatecache";
        this.cacheTTLFile = this.prod.getProductName() + "_" + rt + "updatettl";
        this.cacheTTL = TTL;
    }

    // Methods
    public void setTTL(final long TTL) {
        this.cacheTTL = TTL;
    }

    public void checkForUpdates() {
        this.checkForUpdatesInternal();
    }

    public void checkForUpdatesAtStartup() {
        this.checkForUpdatesInternal();
    }

    private void checkForUpdatesInternal() {
        if (this.prod.getUpdateURL() == null) {
            CommonDialogs.showErrorDialog(
                    "An internal error occurred while checking for updates.",
                    "Update Error");
        } else {
            int newVersionMajor = this.prod.getMajorVersion();
            int newVersionMinor = this.prod.getMinorVersion();
            int newVersionBugfix = this.prod.getBugfixVersion();
            final int newVersionCode = this.prod.getCodeVersion();
            int newVersionPrerelease = this.prod.getPrereleaseVersion();
            try {
                // Check the cache for validity
                this.isCacheGood();
                // If the update DB isn't cached...
                if (!this.cachedDB) {
                    // Generate the cache
                    this.generateCache();
                }
                // Read entries from the cached update DB
                String inputLine;
                try (BufferedReader in = new BufferedReader(new FileReader(
                        this.getCacheFile()))) {
                    inputLine = in.readLine();
                    newVersionMajor = Integer.parseInt(inputLine);
                    inputLine = in.readLine();
                    newVersionMinor = Integer.parseInt(inputLine);
                    inputLine = in.readLine();
                    newVersionBugfix = Integer.parseInt(inputLine);
                    if (this.prereleaseMode) {
                        inputLine = in.readLine();
                        newVersionPrerelease = Integer.parseInt(inputLine);
                    }
                } catch (final NumberFormatException nf) {
                    CommonDialogs
                            .showErrorDialog(
                                    "An internal error occurred while checking for updates.",
                                    "Update Error");
                }
                String blurb = "";
                // Compare current version to most recent one
                if (newVersionMajor > this.prod.getMajorVersion()) {
                    // Major update available
                    blurb = this.readBlurb();
                    this.showUpdatesAvailableMessage(newVersionMajor,
                            newVersionMinor, newVersionBugfix, blurb);
                } else if ((newVersionMajor == this.prod.getMajorVersion())
                        && (newVersionMinor > this.prod.getMinorVersion())) {
                    // Minor update available
                    blurb = this.readBlurb();
                    this.showUpdatesAvailableMessage(newVersionMajor,
                            newVersionMinor, newVersionBugfix, blurb);
                } else if ((newVersionMajor == this.prod.getMajorVersion())
                        && (newVersionMinor == this.prod.getMinorVersion())
                        && (newVersionBugfix > this.prod.getBugfixVersion())) {
                    // Bug fix update available
                    blurb = this.readBlurb();
                    this.showUpdatesAvailableMessage(newVersionMajor,
                            newVersionMinor, newVersionBugfix, blurb);
                } else if ((newVersionMajor == this.prod.getMajorVersion())
                        && (newVersionMinor == this.prod.getMinorVersion())
                        && (newVersionBugfix == this.prod.getBugfixVersion())
                        && (newVersionCode > this.prod.getCodeVersion())) {
                    // Release type update available
                    blurb = this.readBlurb();
                    this.showUpdatesAvailableMessage(newVersionMajor,
                            newVersionMinor, newVersionBugfix, newVersionCode,
                            newVersionPrerelease, blurb);
                } else if ((newVersionMajor == this.prod.getMajorVersion())
                        && (newVersionMinor == this.prod.getMinorVersion())
                        && (newVersionBugfix == this.prod.getBugfixVersion())
                        && (newVersionCode == this.prod.getCodeVersion())
                        && (newVersionPrerelease > this.prod
                                .getPrereleaseVersion())) {
                    // Pre-release update available
                    blurb = this.readBlurb();
                    this.showUpdatesAvailableMessage(newVersionMajor,
                            newVersionMinor, newVersionBugfix, newVersionCode,
                            newVersionPrerelease, blurb);
                }
            } catch (final IOException ie) {
                CommonDialogs
                        .showErrorDialog(
                                "Unable to contact the update site.\n"
                                        + "Make sure you are connected to the Internet,\n"
                                        + "then try again.", "Update Error");
            }
        }
    }

    private void showUpdatesAvailableMessage(final int major, final int minor,
            final int bugfix, final String blurb) {
        final String oldVersionString = Integer.toString(this.prod
                .getMajorVersion())
                + "."
                + Integer.toString(this.prod.getMinorVersion())
                + "."
                + Integer.toString(this.prod.getBugfixVersion());
        final String newVersionString = Integer.toString(major) + "."
                + Integer.toString(minor) + "." + Integer.toString(bugfix);
        CommonDialogs.showTitledDialog("Version " + newVersionString
                + " is available.\nYou have version " + oldVersionString + ".",
                "Update Available");
        if (this.prod.autoStart()) {
            // Apply update
            try {
                UpdateApplier.downloadAndApplyUpdate(this.prod.getUpdatePath(),
                        this.prod.getProductName(), newVersionString,
                        this.prod.getUpdateFile(), blurb, oldVersionString);
            } catch (final IOException e) {
                CommonDialogs
                        .showErrorDialog(
                                "Unable to automatically apply the update.\n"
                                        + "Make sure you are connected to the Internet,\n"
                                        + "then try again.", "Update Error");
            }
        }
    }

    private void showUpdatesAvailableMessage(final int major, final int minor,
            final int bugfix, final int code, final int beta, final String blurb) {
        String rt, crt;
        if (code == ProductData.CODE_NIGHTLY) {
            rt = "-N";
            crt = "Nightly ";
        } else if (code == ProductData.CODE_ALPHA) {
            rt = "-alpha";
            crt = "Alpha ";
        } else if (code == ProductData.CODE_BETA) {
            rt = "-beta";
            crt = "Beta ";
        } else if (code == ProductData.CODE_CANDIDATE) {
            rt = "-rc";
            crt = "RC ";
        } else if (code == ProductData.CODE_STABLE) {
            rt = ".";
            crt = "";
        } else {
            rt = ".";
            crt = "";
        }
        final String oldVersionString = Integer.toString(this.prod
                .getMajorVersion())
                + "."
                + Integer.toString(this.prod.getMinorVersion())
                + "."
                + Integer.toString(this.prod.getBugfixVersion())
                + rt
                + Integer.toString(this.prod.getPrereleaseVersion());
        final String newVersionString = Integer.toString(major) + "."
                + Integer.toString(minor) + "." + Integer.toString(bugfix) + rt
                + Integer.toString(beta);
        CommonDialogs.showTitledDialog("Version " + newVersionString
                + " is available.\nYou have version " + oldVersionString + ".",
                crt + "Update Available");
        if (this.prod.autoStart()) {
            // Apply update
            try {
                UpdateApplier.downloadAndApplyUpdate(this.prod.getUpdatePath(),
                        this.prod.getProductName(), newVersionString,
                        this.prod.getUpdateFile(), blurb, oldVersionString);
            } catch (final IOException e) {
                CommonDialogs
                        .showErrorDialog(
                                "Unable to automatically apply the update.\n"
                                        + "Make sure you are connected to the Internet,\n"
                                        + "then try again.", "Update Error");
            }
        }
    }

    private String readBlurb() {
        // Attempt to read blurb
        final StringBuilder blurbBuilder = new StringBuilder();
        String blurb = null;
        String line = "";
        try (BufferedReader blurbReader = new BufferedReader(
                new InputStreamReader(this.prod.getBlurbURL().openStream()))) {
            line = blurbReader.readLine();
            while (line != null) {
                blurbBuilder.append(line);
                blurbBuilder.append("\n");
                line = blurbReader.readLine();
            }
            blurb = blurbBuilder.toString();
            blurbReader.close();
        } catch (final IOException ie) {
            blurb = "";
        }
        return blurb;
    }

    private static String getCachePrefix() {
        final String osName = System.getProperty("os.name");
        if (osName.indexOf("Mac OS X") != -1) {
            // Mac OS X
            return System.getenv(UpdateChecker.MAC_CACHE_PREFIX);
        } else if (osName.indexOf("Windows") != -1) {
            // Windows
            return System.getenv(UpdateChecker.WIN_CACHE_PREFIX);
        } else {
            // Other - assume UNIX-like
            return System.getenv(UpdateChecker.UNIX_CACHE_PREFIX);
        }
    }

    private String getCacheDirectory() {
        final String osName = System.getProperty("os.name");
        if (osName.indexOf("Mac OS X") != -1) {
            // Mac OS X
            return UpdateChecker.MAC_CACHE_DIR + "/"
                    + this.prod.getrDNSCompanyName() + "/";
        } else if (osName.indexOf("Windows") != -1) {
            // Windows
            return "\\" + this.prod.getCompanyName() + "\\"
                    + this.prod.getProductName() + UpdateChecker.WIN_CACHE_DIR;
        } else {
            // Other - assume UNIX-like
            return "/." + this.prod.getProductName().toLowerCase()
                    + UpdateChecker.UNIX_CACHE_DIR;
        }
    }

    private File getCacheFile() {
        final StringBuilder b = new StringBuilder();
        b.append(UpdateChecker.getCachePrefix());
        b.append(this.getCacheDirectory());
        b.append(this.cacheFile);
        return new File(b.toString());
    }

    private File getCacheTTLFile() {
        final StringBuilder b = new StringBuilder();
        b.append(UpdateChecker.getCachePrefix());
        b.append(this.getCacheDirectory());
        b.append(this.cacheTTLFile);
        return new File(b.toString());
    }

    private boolean generateCache() {
        final File cache = this.getCacheFile();
        // Create the needed subdirectories, if they don't already exist
        final File cacheParent = new File(this.getCacheFile().getParent());
        if (!cacheParent.exists()) {
            final boolean success = cacheParent.mkdirs();
            if (!success) {
                return false;
            }
        }
        final String ttl = Long.toString(System.currentTimeMillis()
                + this.cacheTTL);
        final File ttlFile = this.getCacheTTLFile();
        // Cache the update DB to a file
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                this.prod.getUpdateURL().openStream()));
                BufferedWriter out = new BufferedWriter(new FileWriter(cache));
                BufferedWriter outTTL = new BufferedWriter(new FileWriter(
                        ttlFile))) {
            String inputLine = "";
            while (inputLine != null) {
                inputLine = in.readLine();
                if (inputLine != null) {
                    out.write(inputLine + "\n", 0, inputLine.length() + 1);
                }
            }
            in.close();
            out.close();
            outTTL.write(ttl, 0, ttl.length());
            outTTL.close();
        } catch (final IOException ioe) {
            return false;
        }
        this.cachedDB = true;
        return true;
    }

    private void isCacheGood() {
        final long curr = System.currentTimeMillis();
        final File ttlFile = this.getCacheTTLFile();
        try (BufferedReader inTTL = new BufferedReader(new FileReader(ttlFile))) {
            final String test = inTTL.readLine();
            inTTL.close();
            final long stored = Long.parseLong(test);
            this.cachedDB = curr <= stored;
        } catch (final IOException io) {
            this.cachedDB = false;
        } catch (final NumberFormatException nfe) {
            this.cachedDB = false;
        }
    }
}
