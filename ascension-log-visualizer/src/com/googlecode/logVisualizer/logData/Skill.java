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

import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.AbstractCountable;
import com.googlecode.logVisualizer.util.Countable;
import com.googlecode.logVisualizer.util.DataTablesHandler;

/**
 * This class is a representation of a skill. It is intended to be used where
 * ever a skill is used and thus is able to hold all the data on the skill that
 * might be useful such as name, MP cost, amount of casts and so on.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class Skill extends AbstractCountable<Skill> {
    private static final String CAST_START_STRING = "Cast ";
    private final String name;
    private int mpCost;
    private int turnNumberOfCast = -1;

    /**
     * Constructs a new skill. Note that MP costs are calculated automatically.
     *
     * @param name
     *            The name of the skill to set.
     * @param amountOfCasts
     *            The amount of casts of this skill to set.
     * @throws IllegalArgumentException
     *             if amountOfCasts is below 0
     */
    public Skill(final String name, final int amountOfCasts) {
        super(amountOfCasts);
        if (name == null) {
            throw new NullPointerException("Skill name must not be null.");
        }
        this.name = name;
        this.setAmount(amountOfCasts);
    }

    /**
     * Constructs a new skill. Note that MP costs are calculated automatically.
     *
     * @param name
     *            The name of the skill to set.
     * @param amountOfCasts
     *            The amount of casts of this skill to set.
     * @param turnNumberOfCast
     *            The turn number this skill was casted on to set.
     * @throws IllegalArgumentException
     *             if amountOfCasts is below 0; if turnNumberOfCast is below 0
     */
    public Skill(final String name, final int amountOfCasts,
            final int turnNumberOfCast) {
        this(name, amountOfCasts);
        this.setTurnNumberOfCast(turnNumberOfCast);
    }

    /**
     * @param amountOfCasts
     *            The new amount of casts of this skill.
     * @throws IllegalArgumentException
     *             if amountOfCasts is below 0
     * @see Countable
     */
    @Override
    public void setAmount(final int amountOfCasts) {
        if (amountOfCasts < 0) {
            throw new IllegalArgumentException("Amount of casts below 0.");
        }
        super.setAmount(amountOfCasts);
        this.mpCost = DataTablesHandler.getSkillMPCost(this.name)
                * amountOfCasts;
    }

    /**
     * @return The name of this skill.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The total MP cost.
     */
    public int getMpCost() {
        return this.mpCost;
    }

    /**
     * @param turnNumberOfCast
     *            The turn number this skill was casted on to set.
     * @throws IllegalArgumentException
     *             if turnNumberOfCast is below 0
     */
    public void setTurnNumberOfCast(final int turnNumberOfCast) {
        if (turnNumberOfCast < 0) {
            throw new IllegalArgumentException("Turn number below 0.");
        }
        this.turnNumberOfCast = turnNumberOfCast;
    }

    /**
     * @return The turn number this skill was casted on.
     */
    public int getTurnNumberOfCast() {
        return this.turnNumberOfCast;
    }

    /**
     * @see Countable
     */
    @Override
    public void merge(final Skill s) {
        super.merge(s);
        if (s.getTurnNumberOfCast() < this.turnNumberOfCast) {
            this.turnNumberOfCast = s.getTurnNumberOfCast();
        }
    }

    /**
     * @return The name of this skill.
     * @see Countable
     */
    @Override
    public Comparable<String> getComparator() {
        return this.name;
    }

    /**
     * @return A deep copy of this object.
     * @see Countable
     */
    @Override
    public Skill newInstance() {
        return this.turnNumberOfCast < 0 ? new Skill(this.name,
                this.getAmount()) : new Skill(this.name, this.getAmount(),
                this.turnNumberOfCast);
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(40);
        str.append(Skill.CAST_START_STRING);
        str.append(this.getAmount());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.name);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (super.equals(o) && (o instanceof Skill)) {
            return (((Skill) o).getTurnNumberOfCast() == this.turnNumberOfCast)
                    && this.name.equals(((Skill) o).getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 30;
        result = (31 * result) + super.hashCode();
        result = (31 * result) + this.turnNumberOfCast;
        result = (31 * result) + this.name.hashCode();
        return result;
    }
}
