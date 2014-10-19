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
package com.googlecode.logVisualizer.logData;

/**
 * A container class to store mp gains of all sorts.
 */
public final class MPGain {
    private int encounterMPGain;
    private int starfishMPGain;
    private int restingMPGain;
    private int outOfEncounterMPGain;
    private int consumableMPGain;

    /**
     * @return The MP gains during encounters. This does not include starfish mp
     *         gains or mp gains from resting.
     */
    public int getEncounterMPGain() {
        return this.encounterMPGain;
    }

    /**
     * @param encounterMPGain
     *            The encounter mp gain to set. This should not include starfish
     *            mp gains or mp gains from resting.
     */
    public void setEncounterMPGain(final int encounterMPGain) {
        this.encounterMPGain = encounterMPGain;
    }

    /**
     * @param encounterMPGain
     *            The encounter mp gain to add. This should not include starfish
     *            mp gains or mp gains from resting.
     */
    public void addEncounterMPGain(final int encounterMPGain) {
        this.encounterMPGain += encounterMPGain;
    }

    /**
     * @return The MP gains from starfish familiars.
     */
    public int getStarfishMPGain() {
        return this.starfishMPGain;
    }

    /**
     * @param starfishMPGain
     *            The starfish mp gain to set.
     */
    public void setStarfishMPGain(final int starfishMPGain) {
        this.starfishMPGain = starfishMPGain;
    }

    /**
     * @param starfishMPGain
     *            The starfish mp gain to add.
     */
    public void addStarfishMPGain(final int starfishMPGain) {
        this.starfishMPGain += starfishMPGain;
    }

    /**
     * @return The mp gains from resting.
     */
    public int getRestingMPGain() {
        return this.restingMPGain;
    }

    /**
     * @param restingMPGain
     *            The resting mp gain to set.
     */
    public void setRestingMPGain(final int restingMPGain) {
        this.restingMPGain = restingMPGain;
    }

    /**
     * @param restingMPGain
     *            The resting mp gain to add.
     */
    public void addRestingMPGain(final int restingMPGain) {
        this.restingMPGain += restingMPGain;
    }

    /**
     * @return The out-of-encounter mp gains.
     */
    public int getOutOfEncounterMPGain() {
        return this.outOfEncounterMPGain;
    }

    /**
     * @param outOfEncounterMPGain
     *            The out-of-encounter mp gain to set.
     */
    public void setOutOfEncounterMPGain(final int outOfEncounterMPGain) {
        this.outOfEncounterMPGain = outOfEncounterMPGain;
    }

    /**
     * @param outOfEncounterMPGain
     *            The out-of-encounter mp gain to add.
     */
    public void addOutOfEncounterMPGain(final int outOfEncounterMPGain) {
        this.outOfEncounterMPGain += outOfEncounterMPGain;
    }

    /**
     * @return The consumable mp gains.
     */
    public int getConsumableMPGain() {
        return this.consumableMPGain;
    }

    /**
     * @param consumableMPGain
     *            The consumable mp gain to set.
     */
    public void setConsumableMPGain(final int consumableMPGain) {
        this.consumableMPGain = consumableMPGain;
    }

    /**
     * @param consumableMPGain
     *            The consumable mp gain to add.
     */
    public void addConsumableMPGain(final int consumableMPGain) {
        this.consumableMPGain += consumableMPGain;
    }

    /**
     * @return The total mp gains.
     */
    public int getTotalMPGains() {
        return this.encounterMPGain + this.consumableMPGain
                + this.outOfEncounterMPGain + this.restingMPGain
                + this.starfishMPGain;
    }

    /**
     * @param mpGains
     *            The mp gains to set.
     */
    public void setMPGains(final MPGain mpGains) {
        this.encounterMPGain = mpGains.getEncounterMPGain();
        this.consumableMPGain = mpGains.getConsumableMPGain();
        this.outOfEncounterMPGain = mpGains.getOutOfEncounterMPGain();
        this.restingMPGain = mpGains.getRestingMPGain();
        this.starfishMPGain = mpGains.getStarfishMPGain();
    }

    /**
     * @param mpGains
     *            The mp gains to add.
     */
    public void addMPGains(final MPGain mpGains) {
        this.encounterMPGain += mpGains.getEncounterMPGain();
        this.consumableMPGain += mpGains.getConsumableMPGain();
        this.outOfEncounterMPGain += mpGains.getOutOfEncounterMPGain();
        this.restingMPGain += mpGains.getRestingMPGain();
        this.starfishMPGain += mpGains.getStarfishMPGain();
    }

    @Override
    public int hashCode() {
        int result = 743;
        result = (31 * result) + this.encounterMPGain;
        result = (31 * result) + this.consumableMPGain;
        result = (31 * result) + this.outOfEncounterMPGain;
        result = (31 * result) + this.restingMPGain;
        result = (31 * result) + this.starfishMPGain;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj != null) && (obj instanceof MPGain)) {
            final MPGain other = (MPGain) obj;
            return (this.encounterMPGain == other.encounterMPGain)
                    && (this.consumableMPGain == other.consumableMPGain)
                    && (this.outOfEncounterMPGain == other.outOfEncounterMPGain)
                    && (this.restingMPGain == other.restingMPGain)
                    && (this.starfishMPGain == other.starfishMPGain);
        }
        return false;
    }
}
