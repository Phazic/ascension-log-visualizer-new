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
package com.googlecode.logVisualizer.util.textualLogs;

import java.util.Locale;

import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * Helper class used to help handle the +STAT BREAKDOWN summary, namely counting
 * the stats gained from specific items.
 */
final class StatgiverItem {
    private static final String TAB = "\t";
    private final String itemName;
    private final String itemNameLower;
    private final double perTurnStatgain;
    private double lvl1Stats;
    private double lvl2Stats;
    private double lvl3Stats;
    private double lvl4Stats;
    private double lvl5Stats;
    private double lvl6Stats;
    private double lvl7Stats;
    private double lvl8Stats;
    private double lvl9Stats;
    private double lvl10Stats;
    private double lvl11Stats;
    private double lvl12Stats;

    StatgiverItem(final String itemName, final double perTurnStatgain) {
        this.itemName = itemName;
        this.itemNameLower = itemName.toLowerCase(Locale.ENGLISH);
        this.perTurnStatgain = perTurnStatgain;
    }

    String getItemName() {
        // The ALV internally saves item names only in lower case.
        return this.itemNameLower;
    }

    void incrementLvlStatgain(final int levelNumber,
            final int numberOfIncrements) {
        for (int i = 0; i < numberOfIncrements; i++) {
            switch (levelNumber) {
            case 1:
                this.lvl1Stats += this.perTurnStatgain;
                break;
            case 2:
                this.lvl2Stats += this.perTurnStatgain;
                break;
            case 3:
                this.lvl3Stats += this.perTurnStatgain;
                break;
            case 4:
                this.lvl4Stats += this.perTurnStatgain;
                break;
            case 5:
                this.lvl5Stats += this.perTurnStatgain;
                break;
            case 6:
                this.lvl6Stats += this.perTurnStatgain;
                break;
            case 7:
                this.lvl7Stats += this.perTurnStatgain;
                break;
            case 8:
                this.lvl8Stats += this.perTurnStatgain;
                break;
            case 9:
                this.lvl9Stats += this.perTurnStatgain;
                break;
            case 10:
                this.lvl10Stats += this.perTurnStatgain;
                break;
            case 11:
                this.lvl11Stats += this.perTurnStatgain;
                break;
            case 12:
                this.lvl12Stats += this.perTurnStatgain;
                break;
            default:
                break;
            }
        }
    }

    int getLvl1Stats() {
        return (int) this.lvl1Stats;
    }

    int getLvl2Stats() {
        return (int) this.lvl2Stats;
    }

    int getLvl3Stats() {
        return (int) this.lvl3Stats;
    }

    int getLvl4Stats() {
        return (int) this.lvl4Stats;
    }

    int getLvl5Stats() {
        return (int) this.lvl5Stats;
    }

    int getLvl6Stats() {
        return (int) this.lvl6Stats;
    }

    int getLvl7Stats() {
        return (int) this.lvl7Stats;
    }

    int getLvl8Stats() {
        return (int) this.lvl8Stats;
    }

    int getLvl9Stats() {
        return (int) this.lvl9Stats;
    }

    int getLvl10Stats() {
        return (int) this.lvl10Stats;
    }

    int getLvl11Stats() {
        return (int) this.lvl11Stats;
    }

    int getLvl12Stats() {
        return (int) this.lvl12Stats;
    }

    int getTotalStats() {
        return (int) (this.lvl1Stats + this.lvl2Stats + this.lvl3Stats
                + this.lvl4Stats + this.lvl5Stats + this.lvl6Stats
                + this.lvl7Stats + this.lvl8Stats + this.lvl9Stats
                + this.lvl10Stats + this.lvl11Stats + this.lvl12Stats);
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(200);
        str.append(this.itemName);
        str.append(UsefulPatterns.COLON);
        for (int i = this.itemName.length(); i < 55; i++) {
            str.append(UsefulPatterns.WHITE_SPACE);
        }
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl1Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl2Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl3Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl4Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl5Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl6Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl7Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl8Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl9Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl10Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl11Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getLvl12Stats());
        str.append(StatgiverItem.TAB);
        str.append(this.getTotalStats());
        return str.toString();
    }
}
