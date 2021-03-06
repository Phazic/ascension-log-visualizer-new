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

import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.Countable;

/**
 * A simple implementation of the Consumable class for food.
 *
 * @see Consumable
 */
final class FoodConsumable extends Consumable {
    private static final String CONSUMED_START_STRING = "Ate";

    FoodConsumable(final String name, final int adventureGain,
            final int amountUsed) {
        super(name, adventureGain, amountUsed);
    }

    FoodConsumable(final String name, final int adventureGain,
            final int amountUsed, final int turnNumberOfUsage) {
        super(name, adventureGain, amountUsed, turnNumberOfUsage);
    }

    /** {@inheritDoc} */
    @Override
    public ConsumableVersion getConsumableVersion() {
        return ConsumableVersion.FOOD;
    }

    /**
     * @return A deep copy of this object.
     * @see Countable
     */
    @Override
    public FoodConsumable newInstance() {
        final FoodConsumable consumable = this.getTurnNumberOfUsage() < 0 ? new FoodConsumable(
                this.getName(), this.getAdventureGain(), this.getAmount())
                : new FoodConsumable(this.getName(), this.getAdventureGain(),
                        this.getAmount(), this.getTurnNumberOfUsage());
        consumable.setDayNumberOfUsage(this.getDayNumberOfUsage());
        consumable.setStatGain(this.getStatGain());
        return consumable;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(50);
        str.append(FoodConsumable.CONSUMED_START_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getAmount());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getName());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(UsefulPatterns.ROUND_BRACKET_OPEN);
        str.append(this.getAdventureGain());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(Consumable.ADVENTURES_GAINED_STRING);
        str.append(UsefulPatterns.ROUND_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getStatGain().toString());
        return str.toString();
    }
}
