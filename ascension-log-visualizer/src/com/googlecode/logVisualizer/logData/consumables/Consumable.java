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
package com.googlecode.logVisualizer.logData.consumables;

import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.util.AbstractCountable;
import com.googlecode.logVisualizer.util.Countable;

/**
 * A representation of a consumable item. This class should be used whenever an
 * item is used and/or consumed. All necessary data should be able to be stored
 * inside an instance of this class.
 * <p>
 * This class is instanced by calling its static factory methods. There are
 * different versions for Food, Booze, Spleen and other consumables.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public abstract class Consumable extends AbstractCountable<Consumable> {
    static final String ADVENTURES_GAINED_STRING = "adventures gained";
    private final String name;
    private int adventureGain;
    private int turnNumberOfUsage = -1;
    private int dayNumberOfUsage = -1;
    private Statgain statGain = new Statgain();
    private final ConsumableComparator comparator = new ConsumableComparator();

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set. A value below 0
     *            isn't a valid parameter.
     * @param amountUsed
     *            The amount used of this consumable to set. A value below 1
     *            isn't a valid parameter.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    Consumable(final String name, final int adventureGain, final int amountUsed) {
        super(amountUsed);
        if (name == null) {
            throw new NullPointerException("Skill name must not be null.");
        }
        if (adventureGain < 0) {
            throw new IllegalArgumentException("Adventure gain below 0.");
        }
        if (amountUsed < 1) {
            throw new IllegalArgumentException("Amount used below 1.");
        }
        this.name = name;
        this.adventureGain = adventureGain;
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set. A value below 0
     *            isn't a valid parameter.
     * @param amountUsed
     *            The amount used of this consumable to set. A value below 1
     *            isn't a valid parameter.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set. A value below 0
     *            isn't a valid parameter.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    Consumable(final String name, final int adventureGain,
            final int amountUsed, final int turnNumberOfUsage) {
        this(name, adventureGain, amountUsed);
        if (turnNumberOfUsage < 0) {
            throw new IllegalArgumentException("Turn number below 0.");
        }
        this.turnNumberOfUsage = turnNumberOfUsage;
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newFoodConsumable(final String name,
            final int adventureGain, final int amountUsed) {
        return new FoodConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newFoodConsumable(final String name,
            final int adventureGain, final int amountUsed,
            final int turnNumberOfUsage) {
        return new FoodConsumable(name, adventureGain, amountUsed,
                turnNumberOfUsage);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newBoozeConsumable(final String name,
            final int adventureGain, final int amountUsed) {
        return new BoozeConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newBoozeConsumable(final String name,
            final int adventureGain, final int amountUsed,
            final int turnNumberOfUsage) {
        return new BoozeConsumable(name, adventureGain, amountUsed,
                turnNumberOfUsage);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newSpleenConsumable(final String name,
            final int adventureGain, final int amountUsed) {
        return new SpleenConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newSpleenConsumable(final String name,
            final int adventureGain, final int amountUsed,
            final int turnNumberOfUsage) {
        return new SpleenConsumable(name, adventureGain, amountUsed,
                turnNumberOfUsage);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1
     */
    public static Consumable newOtherConsumable(final String name,
            final int adventureGain, final int amountUsed) {
        return new OtherConsumable(name, adventureGain, amountUsed);
    }

    /**
     * @param name
     *            The name of this consumable to set.
     * @param adventureGain
     *            The adventure gain of this consumable to set.
     * @param amountUsed
     *            The amount used of this consumable to set.
     * @param turnNumberOfUsage
     *            The turn this consumable was used on to set.
     * @return A new Consumable with the specified parameters.
     * @throws IllegalArgumentException
     *             if adventureGain is below 0; if amountUsed is below 1; if
     *             turnNumberOfUsage is below 0
     */
    public static Consumable newOtherConsumable(final String name,
            final int adventureGain, final int amountUsed,
            final int turnNumberOfUsage) {
        return new OtherConsumable(name, adventureGain, amountUsed,
                turnNumberOfUsage);
    }

    /**
     * @return The {@link ConsumableVersion} of this consumable.
     */
    public abstract ConsumableVersion getConsumableVersion();

    /**
     * @return The name of this consumable.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The adventure gain of this consumable.
     */
    public int getAdventureGain() {
        return this.adventureGain;
    }

    /**
     * @param amountUsed
     *            The amount used of this consumable to set. A value below 1
     *            isn't a valid parameter.
     * @see Countable
     * @throws IllegalArgumentException
     *             if amountUsed is below 1
     */
    @Override
    public void setAmount(final int amountUsed) {
        if (amountUsed < 1) {
            throw new IllegalArgumentException("Amount used below 1.");
        }
        super.setAmount(amountUsed);
    }

    /**
     * @return The turn number of when this consumable was used. If no value has
     *         been specified, -1 will be return.
     */
    public int getTurnNumberOfUsage() {
        return this.turnNumberOfUsage;
    }

    /**
     * @param dayNumberOfUsage
     *            The day number of when this consumable was used to set.
     * @throws IllegalArgumentException
     *             if dayNumberOfUsage is below 1
     */
    public void setDayNumberOfUsage(final int dayNumberOfUsage) {
        if (dayNumberOfUsage < 1) {
            throw new IllegalArgumentException(
                    "Day number must not be below 1.");
        }
        this.dayNumberOfUsage = dayNumberOfUsage;
    }

    /**
     * @return The day number of when this consumable was used. If no value has
     *         been specified, -1 will be return.
     */
    public int getDayNumberOfUsage() {
        return this.dayNumberOfUsage;
    }

    /**
     * @param statGain
     *            The stat gain of this consumable to set.
     */
    public void setStatGain(final Statgain statGain) {
        this.statGain = statGain;
    }

    /**
     * @return The stat gain of this consumable.
     */
    public Statgain getStatGain() {
        return this.statGain;
    }

    /**
     * @return The name of this consumable and the day it was used on.
     * @see Countable
     */
    @Override
    public ConsumableComparator getComparator() {
        return this.comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final Consumable c) {
        super.merge(c);
        this.adventureGain += c.getAdventureGain();
        this.statGain = this.statGain.addStats(c.getStatGain());
    }

    @Override
    public boolean equals(final Object o) {
        if (super.equals(o) && (o instanceof Consumable)) {
            final Consumable c = (Consumable) o;
            return (c.getAdventureGain() == this.adventureGain)
                    && (c.getTurnNumberOfUsage() == this.turnNumberOfUsage)
                    && (c.getDayNumberOfUsage() == this.dayNumberOfUsage)
                    && (c.getConsumableVersion() == this.getConsumableVersion())
                    && this.statGain.equals(c.getStatGain())
                    && this.name.equals(c.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 1701;
        result = (31 * result) + this.adventureGain;
        result = (31 * result) + this.turnNumberOfUsage;
        result = (31 * result) + this.dayNumberOfUsage;
        result = (31 * result) + this.getConsumableVersion().hashCode();
        result = (31 * result) + this.statGain.hashCode();
        result = (31 * result) + this.name.hashCode();
        return result;
    }

    /**
     * Helper class to handle comparator implementation of the {@link Countable}
     * interface.
     */
    private class ConsumableComparator implements
            Comparable<ConsumableComparator> {
        public ConsumableComparator() {
            // TODO Auto-generated constructor stub
        }

        private String getName() {
            return Consumable.this.name;
        }

        private int getDayNumber() {
            return Consumable.this.dayNumberOfUsage;
        }

        @Override
        public int compareTo(final ConsumableComparator o) {
            final int tmp = Consumable.this.name.compareTo(o.getName());
            return tmp != 0 ? tmp : Consumable.this.dayNumberOfUsage
                    - o.getDayNumber();
        }
    }

    /**
     * A simple enumeration for various consumable types.
     */
    public static enum ConsumableVersion {
        FOOD, BOOZE, SPLEEN, OTHER;
    }
}
