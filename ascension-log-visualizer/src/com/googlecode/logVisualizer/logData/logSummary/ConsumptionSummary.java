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
package com.googlecode.logVisualizer.logData.logSummary;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.util.CountableSet;
import com.googlecode.logVisualizer.util.DataTablesHandler;

/**
 * This class is a consumption data holder. It orders consumption by day and has
 * various summaries available.
 */
public final class ConsumptionSummary {
    private final SortedSet<ConsumptionDayStats> dayStatistics = new TreeSet<>();

    /**
     * Constructs a ConsumptionSummary instance.
     *
     * @param consumables
     *            A collection containing all the consumables used during an
     *            ascension.
     * @param dayChanges
     *            A collection containing all day changes of an ascension.
     */
    ConsumptionSummary(final Collection<Consumable> consumables,
            final Collection<DayChange> dayChanges) {
        for (final DayChange dc : dayChanges) {
            final ConsumptionDayStats dayStats = new ConsumptionDayStats(
                    dc.getDayNumber());
            for (final Consumable c : consumables) {
                if (c.getDayNumberOfUsage() == dc.getDayNumber()) {
                    dayStats.addConsumable(c);
                }
            }
            this.dayStatistics.add(dayStats);
        }
    }

    /**
     * @return A read-only sorted set of all the consumption summaries by day.
     */
    public SortedSet<ConsumptionDayStats> getDayStatistics() {
        return Collections.unmodifiableSortedSet(this.dayStatistics);
    }

    /**
     * @return The total amount of turns gained from food.
     */
    public int getTotalTurnsFromFood() {
        int totalTurnsFromFood = 0;
        for (final ConsumptionDayStats cds : this.dayStatistics) {
            totalTurnsFromFood += cds.getTotalTurnsFromFood();
        }
        return totalTurnsFromFood;
    }

    /**
     * @return The total amount of turns gained from booze.
     */
    public int getTotalTurnsFromBooze() {
        int totalTurnsFromBooze = 0;
        for (final ConsumptionDayStats cds : this.dayStatistics) {
            totalTurnsFromBooze += cds.getTotalTurnsFromBooze();
        }
        return totalTurnsFromBooze;
    }

    /**
     * @return The total amount of turns gained from other consumables.
     */
    public int getTotalTurnsFromOther() {
        int totalTurnsFromOther = 0;
        for (final ConsumptionDayStats cds : this.dayStatistics) {
            totalTurnsFromOther += cds.getTotalTurnsFromOther();
        }
        return totalTurnsFromOther;
    }

    /**
     * @return The total amount of substats from food collected during an
     *         ascension.
     */
    public Statgain getFoodConsumablesStatgains() {
        Statgain stats = new Statgain();
        for (final ConsumptionDayStats cds : this.dayStatistics) {
            stats = stats.addStats(cds.getFoodConsumablesStatgains());
        }
        return stats;
    }

    /**
     * @return The total amount of substats from booze collected during an
     *         ascension.
     */
    public Statgain getBoozeConsumablesStatgains() {
        Statgain stats = new Statgain();
        for (final ConsumptionDayStats cds : this.dayStatistics) {
            stats = stats.addStats(cds.getBoozeConsumablesStatgains());
        }
        return stats;
    }

    /**
     * @return The total amount of substats from other consumables collected
     *         during an ascension.
     */
    public Statgain getUsedConsumablesStatgains() {
        Statgain stats = new Statgain();
        for (final ConsumptionDayStats cds : this.dayStatistics) {
            stats = stats.addStats(cds.getUsedConsumablesStatgains());
        }
        return stats;
    }

    /**
     * @return The total amount of substats from all possible consumables
     *         collected during an ascension.
     */
    public Statgain getTotalConsumablesStatgains() {
        Statgain stats = new Statgain();
        for (final ConsumptionDayStats cds : this.dayStatistics) {
            stats = stats.addStats(cds.getTotalConsumablesStatgains());
        }
        return stats;
    }

    /**
     * A container class able to hold various consumption data of an ascension
     * day.
     */
    public static final class ConsumptionDayStats implements
            Comparable<ConsumptionDayStats> {
        private final int dayNumber;
        private final CountableSet<Consumable> consumablesUsed = new CountableSet<>();
        private int totalTurnsFromFood;
        private int totalTurnsFromBooze;
        private int totalTurnsFromOther;
        private int totalFullnessHit;
        private int totalDrunkennessHit;
        private int totalSpleenHit;
        private Statgain foodConsumablesStatgains = new Statgain();
        private Statgain boozeConsumablesStatgains = new Statgain();
        private Statgain usedConsumablesStatgains = new Statgain();
        private Statgain totalConsumablesStatgains = new Statgain();

        ConsumptionDayStats(final int dayNumber) {
            this.dayNumber = dayNumber;
        }

        void addConsumable(final Consumable c) {
            this.consumablesUsed.addElement(c);
            this.totalConsumablesStatgains = this.totalConsumablesStatgains
                    .addStats(c.getStatGain());
            switch (c.getConsumableVersion()) {
            case FOOD:
                this.totalTurnsFromFood += c.getAdventureGain();
                this.totalFullnessHit += DataTablesHandler.getFullnessHit(c
                        .getName()) * c.getAmount();
                this.foodConsumablesStatgains = this.foodConsumablesStatgains
                        .addStats(c.getStatGain());
                break;
            case BOOZE:
                this.totalTurnsFromBooze += c.getAdventureGain();
                this.totalDrunkennessHit += DataTablesHandler
                        .getDrunkennessHit(c.getName()) * c.getAmount();
                this.boozeConsumablesStatgains = this.boozeConsumablesStatgains
                        .addStats(c.getStatGain());
                break;
            default:
                this.totalTurnsFromOther += c.getAdventureGain();
                this.totalSpleenHit += DataTablesHandler.getSpleenHit(c
                        .getName()) * c.getAmount();
                this.usedConsumablesStatgains = this.usedConsumablesStatgains
                        .addStats(c.getStatGain());
            }
        }

        /**
         * @return The day number of this consumption summary.
         */
        public int getDayNumber() {
            return this.dayNumber;
        }

        /**
         * @return A list of all consumables used during the day.
         */
        public Collection<Consumable> getConsumablesUsed() {
            return this.consumablesUsed.getElements();
        }

        /**
         * @return The total amount of turns gained from food.
         */
        public int getTotalTurnsFromFood() {
            return this.totalTurnsFromFood;
        }

        /**
         * @return The total amount of turns gained from booze.
         */
        public int getTotalTurnsFromBooze() {
            return this.totalTurnsFromBooze;
        }

        /**
         * @return The total amount of turns gained from other consumables.
         */
        public int getTotalTurnsFromOther() {
            return this.totalTurnsFromOther;
        }

        /**
         * @return The total amount of fullness used during an ascension day.
         */
        public int getTotalFullnessHit() {
            return this.totalFullnessHit;
        }

        /**
         * @return The total amount of drunkenness used during an ascension day.
         */
        public int getTotalDrunkennessHit() {
            return this.totalDrunkennessHit;
        }

        /**
         * @return The total amount of spleen used during an ascension day.
         */
        public int getTotalSpleenHit() {
            return this.totalSpleenHit;
        }

        /**
         * @return The total amount of substats from food collected during an
         *         ascension day.
         */
        public Statgain getFoodConsumablesStatgains() {
            return this.foodConsumablesStatgains;
        }

        /**
         * @return The total amount of substats from booze collected during an
         *         ascension day.
         */
        public Statgain getBoozeConsumablesStatgains() {
            return this.boozeConsumablesStatgains;
        }

        /**
         * @return The total amount of substats from other consumables collected
         *         during an ascension day.
         */
        public Statgain getUsedConsumablesStatgains() {
            return this.usedConsumablesStatgains;
        }

        /**
         * @return The total amount of substats from all possible consumables
         *         collected during an ascension day.
         */
        public Statgain getTotalConsumablesStatgains() {
            return this.totalConsumablesStatgains;
        }

        /**
         * Compares this ConsumptionDayStats instances day number with that of
         * the given ConsumptionDayStats instance.
         *
         * @see Comparable
         */
        @Override
        public int compareTo(final ConsumptionDayStats o) {
            return this.dayNumber - o.getDayNumber();
        }
    }
}
