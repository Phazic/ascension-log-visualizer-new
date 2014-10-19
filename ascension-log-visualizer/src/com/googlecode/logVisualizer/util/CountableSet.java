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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class is a container for a collection of {@link Countable} instances
 * that allows no duplicate elements inside the collection based on the
 * comparison from the {@link Countable} interface. If an element is to be added
 * which is already in the collection (again, based on the comparison from the
 * {@link Countable} interface), it will not be added and instead the two
 * similar elements will be merged with each other using
 * {@link Countable#merge(Countable)}.
 * <p>
 * This reglementation is in ways similar to those of the {@link Set} interface,
 * but said interface is stricter than what is needed for this purpose here and
 * thus not used.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 *
 * @see Countable
 */
public final class CountableSet<T extends Countable<T>> {
    private final SortedMap<Comparable<?>, T> countablesMap;

    public CountableSet() {
        this.countablesMap = new TreeMap<>();
    }

    /**
     * Adds a deep copy of this element to the collection, but if the element
     * already exists inside the collection (based on the {@link Comparable}
     * returned by {@link Countable#getComparator()}), only merges the two
     * elements through the {@link Countable#merge(Countable)} method of the
     * Countable interface.
     *
     * @param element
     *            The element to add.
     */
    public void addElement(final T element) {
        final T tmp = this.countablesMap.get(element.getComparator());
        if (tmp != null) {
            tmp.merge(element);
        } else {
            this.countablesMap.put(element.getComparator(),
                    element.newInstance());
        }
    }

    /**
     * Note that this method doesn't set the reference of the internal
     * collection to the given collection, but only clears the old content and
     * copies over the elements of the given collection. While doing so, it
     * adheres to restrictions of this container class.
     * <p>
     * Thus the line {@code elements == countableList.getElements()} will always
     * be false.
     *
     * @param elements
     *            The elements to set.
     */
    public void setElements(final Collection<T> elements) {
        if (elements == null) {
            throw new NullPointerException("Collection must not be null.");
        }
        this.countablesMap.clear();
        for (final T t : elements) {
            this.addElement(t);
        }
    }

    /**
     * This method returns a collection containing all {@link Countable}
     * elements of this container class instance.
     * <p>
     * Note that the contents of this collection is directly backed by this
     * class, and thus one should be very careful when modifying its elements
     * especially concerning the restrictions imposed by this class (not
     * modifying them at all would be preferable).
     * <p>
     * Also, please note that this collection is read-only.
     *
     * @return The elements.
     */
    public Collection<T> getElements() {
        return Collections.unmodifiableCollection(this.countablesMap.values());
    }

    /**
     * In contrast to {@link CountableSet#getElements()}, this method will
     * return a deep copy of the internal collection. Thus, there will be no
     * problems with altering its elements in a way that would violate the
     * restrictions imposed by this class.
     * <p>
     * Of course, due to having to create deep copies of every element, this
     * method may perform considerably worse than
     * {@link CountableSet#getElements()}, since it has a run-time behaviour of
     * O(n) instead of O(1) and it is also depending on the elements's
     * {@link Countable} implementation.
     *
     * @return The elements.
     */
    public List<T> getElementsDeepCopy() {
        final List<T> listCopy = new ArrayList<>(this.countablesMap.size());
        for (final T t : this.countablesMap.values()) {
            listCopy.add(t.newInstance());
        }
        return listCopy;
    }

    /**
     * This method removes all elements from the internal collection.
     */
    public void clear() {
        this.countablesMap.clear();
    }

    /**
     * @return The number of elements this container class holds.
     */
    public int size() {
        return this.countablesMap.size();
    }

    /**
     * Checks the collection for whether it contains the given {@link Countable}
     * based on the {@link Comparable} returned by
     * {@link Countable#getComparator()}.
     *
     * @param t
     *            The {@link Countable} which should be checked on whether this
     *            collection contains it.
     * @return {@code true} if the given {@link Countable} is inside this
     *         collection, otherwise {@code false}.
     */
    public boolean contains(final T t) {
        return this.countablesMap.containsKey(t.getComparator());
    }

    @Override
    public boolean equals(final Object o) {
        if ((o != null) && (o instanceof CountableSet<?>)) {
            return this.getElements().equals(
                    ((CountableSet<?>) o).getElements());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 189;
        result = (31 * result) + this.getElements().hashCode();
        return result;
    }
}
