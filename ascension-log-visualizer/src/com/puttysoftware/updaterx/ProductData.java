package com.puttysoftware.updaterx;

import java.net.MalformedURLException;
import java.net.URL;

public class ProductData {
    // Fields
    private URL updateURL;
    private URL blurbURL;
    private URL updateFile;
    private String updatePath;
    private boolean autoStart;
    private String rDNSCompanyName;
    private String companyName;
    private String productName;
    private int majorVersion;
    private int minorVersion;
    private int bugfixVersion;
    private int codeVersion;
    private int prereleaseVersion;
    public static final int CODE_NIGHTLY = 0;
    public static final int CODE_ALPHA = 1;
    public static final int CODE_BETA = 2;
    public static final int CODE_CANDIDATE = 3;
    public static final int CODE_STABLE = 4;
    public static final int CODE_MATURE = 5;

    // Constructors
    public ProductData() {
        // Do nothing
    }

    public ProductData(final String update, final String blurb,
            final String companyMac, final String company,
            final String product, final int major, final int minor,
            final int bugfix, final int code, final int beta) {
        String rt;
        if (code == ProductData.CODE_NIGHTLY) {
            rt = "nightly_";
        } else if (code == ProductData.CODE_ALPHA) {
            rt = "alpha_";
        } else if (code == ProductData.CODE_BETA) {
            rt = "beta_";
        } else if (code == ProductData.CODE_CANDIDATE) {
            rt = "rc_";
        } else if (code == ProductData.CODE_STABLE) {
            rt = "stable_";
        } else {
            rt = "mature_";
        }
        final String updatetxt = "version.txt";
        final String blurbtxt = "blurb.txt";
        try {
            this.updateURL = new URL(update + rt + updatetxt);
        } catch (final MalformedURLException mu1) {
            // Ignore
        }
        try {
            this.blurbURL = new URL(blurb + rt + blurbtxt);
        } catch (final MalformedURLException mu1) {
            // Ignore
        }
        this.updateFile = null;
        this.updatePath = null;
        this.autoStart = false;
        this.rDNSCompanyName = companyMac;
        this.companyName = company;
        this.productName = product;
        this.majorVersion = major;
        this.minorVersion = minor;
        this.bugfixVersion = bugfix;
        this.codeVersion = code;
        this.prereleaseVersion = beta;
    }

    public ProductData(final String update, final String blurb,
            final String file, final String path,
            final boolean performAutoStart, final String companyMac,
            final String company, final String product, final int major,
            final int minor, final int bugfix, final int code, final int beta) {
        String rt;
        if (code == ProductData.CODE_NIGHTLY) {
            rt = "nightly_";
        } else if (code == ProductData.CODE_ALPHA) {
            rt = "alpha_";
        } else if (code == ProductData.CODE_BETA) {
            rt = "beta_";
        } else if (code == ProductData.CODE_CANDIDATE) {
            rt = "rc_";
        } else if (code == ProductData.CODE_STABLE) {
            rt = "stable_";
        } else {
            rt = "mature_";
        }
        final String updatetxt = "version.txt";
        final String blurbtxt = "blurb.txt";
        try {
            this.updateURL = new URL(update + rt + updatetxt);
        } catch (final MalformedURLException mu1) {
            // Ignore
        }
        try {
            this.blurbURL = new URL(blurb + rt + blurbtxt);
        } catch (final MalformedURLException mu1) {
            // Ignore
        }
        try {
            this.updateFile = new URL(file);
        } catch (final MalformedURLException mu1) {
            // Ignore
        }
        this.updatePath = path;
        this.autoStart = performAutoStart;
        this.rDNSCompanyName = companyMac;
        this.companyName = company;
        this.productName = product;
        this.majorVersion = major;
        this.minorVersion = minor;
        this.bugfixVersion = bugfix;
        this.codeVersion = code;
        this.prereleaseVersion = beta;
    }

    public URL getUpdateFile() {
        return this.updateFile;
    }

    public void setUpdateFile(final URL newUpdateFile) {
        this.updateFile = newUpdateFile;
    }

    public String getUpdatePath() {
        return this.updatePath;
    }

    public void setUpdatePath(final String newUpdatePath) {
        this.updatePath = newUpdatePath;
    }

    // Methods
    /**
     * @return the updateURL
     */
    public URL getUpdateURL() {
        return this.updateURL;
    }

    /**
     * @param newUpdateURL
     *            the updateURL to set
     */
    public void setUpdateURL(final URL newUpdateURL) {
        this.updateURL = newUpdateURL;
    }

    /**
     * @return the blurbURL
     */
    public URL getBlurbURL() {
        return this.blurbURL;
    }

    /**
     * @param newBlurbURL
     *            the blurbURL to set
     */
    public void setBlurbURL(final URL newBlurbURL) {
        this.blurbURL = newBlurbURL;
    }

    /**
     * @return the auto start status
     */
    public boolean autoStart() {
        return this.autoStart;
    }

    /**
     * @param performAutoStart
     *            the autoStart to set
     */
    public void setAutoStart(final boolean performAutoStart) {
        this.autoStart = performAutoStart;
    }

    /**
     * @return the rDNSCompanyName
     */
    public String getrDNSCompanyName() {
        return this.rDNSCompanyName;
    }

    /**
     * @param newRDNSCompanyName
     *            the rDNSCompanyName to set
     */
    public void setrDNSCompanyName(final String newRDNSCompanyName) {
        this.rDNSCompanyName = newRDNSCompanyName;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return this.companyName;
    }

    /**
     * @param newCompanyName
     *            the companyName to set
     */
    public void setCompanyName(final String newCompanyName) {
        this.companyName = newCompanyName;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return this.productName;
    }

    /**
     * @param newProductName
     *            the productName to set
     */
    public void setProductName(final String newProductName) {
        this.productName = newProductName;
    }

    /**
     * @return the majorVersion
     */
    public int getMajorVersion() {
        return this.majorVersion;
    }

    /**
     * @param newMajorVersion
     *            the majorVersion to set
     */
    public void setMajorVersion(final int newMajorVersion) {
        this.majorVersion = newMajorVersion;
    }

    /**
     * @return the minorVersion
     */
    public int getMinorVersion() {
        return this.minorVersion;
    }

    /**
     * @param newMinorVersion
     *            the minorVersion to set
     */
    public void setMinorVersion(final int newMinorVersion) {
        this.minorVersion = newMinorVersion;
    }

    /**
     * @return the bugfixVersion
     */
    public int getBugfixVersion() {
        return this.bugfixVersion;
    }

    /**
     * @param newBugfixVersion
     *            the bugfixVersion to set
     */
    public void setBugfixVersion(final int newBugfixVersion) {
        this.bugfixVersion = newBugfixVersion;
    }

    /**
     * @return the codeVersion
     */
    public int getCodeVersion() {
        return this.codeVersion;
    }

    /**
     * @param newCodeVersion
     *            the codeVersion to set
     */
    public void setCodeVersion(final int newCodeVersion) {
        this.codeVersion = newCodeVersion;
    }

    /**
     * @return the betaVersion
     */
    public int getPrereleaseVersion() {
        return this.prereleaseVersion;
    }

    /**
     * @param newPrereleaseVersion
     *            the prereleaseVersion to set
     */
    public void setPrereleaseVersion(final int newPrereleaseVersion) {
        this.prereleaseVersion = newPrereleaseVersion;
    }
}
