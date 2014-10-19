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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.logVisualizer.logData.MeatGain;
import com.googlecode.logVisualizer.util.DataNumberPair;

/**
 * Data class for meat data summaries by level.
 */
public final class MeatSummary {
    private final Map<Integer, MeatGain> levelMeatData = new HashMap<>();

    /**
     * Adds the given meat data to the given level.
     *
     * @param levelNumber
     *            The level number.
     * @param meatData
     *            The meat data to add to the given level.
     */
    public void addLevelMeatData(final int levelNumber, final MeatGain meatData) {
        final Integer lvl = Integer.valueOf(levelNumber);
        if (this.levelMeatData.containsKey(lvl)) {
            final MeatGain newMeatData = this.levelMeatData.get(lvl)
                    .addMeatData(meatData);
            this.levelMeatData.put(lvl, newMeatData);
        } else {
            this.levelMeatData.put(lvl, meatData);
        }
    }

    /**
     * @param levelNumber
     *            The level number of the wanted meat data.
     * @return The meat data of the given level.
     */
    public MeatGain getLevelMeatData(final int levelNumber) {
        return this.levelMeatData.get(levelNumber);
    }

    /**
     * @return A sorted list of the meat data of all levels.
     */
    public List<DataNumberPair<MeatGain>> getAllLevelsMeatData() {
        final List<DataNumberPair<MeatGain>> result = new ArrayList<>(
                this.levelMeatData.size());
        for (final Integer i : this.levelMeatData.keySet()) {
            result.add(DataNumberPair.of(this.levelMeatData.get(i), i));
        }
        Collections.sort(result);
        return result;
    }
}
