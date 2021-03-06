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
package com.googlecode.logVisualizer.util;

/**
 * A basic implementation for the {@link Countable} interface. The only thing
 * left to implement is {@link Countable#getComparator()}.
 *
 * @see Countable
 */
public abstract class AbstractCountable<T extends AbstractCountable<?>>
        implements Countable<T> {
    private int amount;

    /**
     * @param amount
     *            The amount to set.
     */
    public AbstractCountable(final int amount) {
        this.amount = amount;
    }

    /**
     * @param amount
     *            The amount to set.
     * @see Countable#setAmount(int)
     */
    @Override
    public void setAmount(final int amount) {
        this.amount = amount;
    }

    /**
     * @return The amount.
     * @see Countable#getAmount()
     */
    @Override
    public int getAmount() {
        return this.amount;
    }

    /**
     * Adds the amount of the given Countable to this Countable.
     * <p>
     * This implementation simply adds the amount of the given Countable and
     * this Countable together and then sets the new amount of this Countable by
     * calling {@link #setAmount(int)}.
     *
     * @param c
     *            The Countable instance to be merged.
     * @throws NullPointerException
     *             if c is {@code null}
     * @see Countable#merge(Countable)
     */
    @Override
    public void merge(final T t) {
        this.setAmount(this.amount + t.getAmount());
    }

    /**
     * @return The difference between this Countable's amount and the amount of
     *         the given Countable.
     */
    @Override
    public int compareTo(final T t) {
        return this.amount - t.getAmount();
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof AbstractCountable<?>) {
                return ((AbstractCountable<?>) o).getAmount() == this.amount;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 1337;
        result = (31 * result) + this.amount;
        return result;
    }
}
