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
 * Every object implementing this interface has to have an value of amount to it
 * and it has be comparable to other instances of this interface using
 * {@link #getComparator()}, while the comparison should not have anything to do
 * with the amount of this object.
 * <p>
 * For the most part, this interface is merely a means to use the class
 * {@link CountableSet} so refer to that class for deciding whether you want to
 * use this interface or not.
 *
 * @see CountableSet
 */
public interface Countable<T extends Countable<?>> extends Comparable<T> {
    /**
     * @param amount
     *            The amount to set.
     */
    public void setAmount(int amount);

    /**
     * @return The amount.
     */
    public int getAmount();

    /**
     * @return The object used for comparing two different Countable instances.
     */
    public Comparable<?> getComparator();

    /**
     * Merges the given Countable instance with this Countable instance. The
     * given Countable instance is not altered by this method.
     * <p>
     * Note that the object returned by {@link #getComparator()} before a merge
     * must be equal to the object returned afterwards.
     *
     * @param c
     *            The Countable instance to be merged.
     */
    public void merge(T t);

    /**
     * @return A deep copy of this Countable instance.
     */
    public T newInstance();
}
