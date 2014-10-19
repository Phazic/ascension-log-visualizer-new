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
package com.googlecode.logVisualizer.chart.perDayConsumption;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.jfree.data.category.DefaultCategoryDataset;

import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.logSummary.ConsumptionSummary.ConsumptionDayStats;
import com.googlecode.logVisualizer.util.DataTablesHandler;

final class ConsumptionDataset extends DefaultCategoryDataset {
    /**
     * 
     */
    private static final long serialVersionUID = 3893864950692096107L;
    // NumberFormat isn't thread-safe, but from what I could gather, as long as
    // one doesn't change the state of a NumberFormat object and only calls the
    // format() methods, everything should be OK.
    private final NumberFormat FORMATTER = new DecimalFormat("#0.00");
    private final Map<String, Consumable> consumables = new HashMap<>();
    private final String foodCategoryString;
    private final String boozeCategoryString;
    private final String spleenCategoryString;

    ConsumptionDataset(final ConsumptionDayStats consumptionStats) {
        super();
        this.foodCategoryString = "Food; "
                + consumptionStats.getTotalTurnsFromFood()
                + " adv/"
                + consumptionStats.getTotalFullnessHit()
                + " full ("
                + this.FORMATTER.format((consumptionStats
                        .getTotalTurnsFromFood() * 1.0)
                        / consumptionStats.getTotalFullnessHit()) + " avg)";
        this.boozeCategoryString = "Booze; "
                + consumptionStats.getTotalTurnsFromBooze()
                + " adv/"
                + consumptionStats.getTotalDrunkennessHit()
                + " drunk ("
                + this.FORMATTER.format((consumptionStats
                        .getTotalTurnsFromBooze() * 1.0)
                        / consumptionStats.getTotalDrunkennessHit()) + " avg)";
        this.spleenCategoryString = "Spleen; "
                + consumptionStats.getTotalTurnsFromOther()
                + " adv/"
                + consumptionStats.getTotalSpleenHit()
                + " spleen ("
                + this.FORMATTER.format((consumptionStats
                        .getTotalTurnsFromOther() * 1.0)
                        / consumptionStats.getTotalSpleenHit()) + " avg)";
        // Dummy values added and removed again to establish the same category
        // order in all charts. There may possibly be a simpler way to do it.
        this.addValue(0, "", this.foodCategoryString);
        this.addValue(0, "", this.boozeCategoryString);
        this.addValue(0, "", this.spleenCategoryString);
        this.removeRow("");
    }

    void addConsumable(final Consumable c) {
        this.consumables.put(c.getName(), c);
        switch (c.getConsumableVersion()) {
        case FOOD:
            this.addValue(
                    DataTablesHandler.getFullnessHit(c.getName())
                            * c.getAmount(), c.getName(),
                    this.foodCategoryString);
            break;
        case BOOZE:
            this.addValue(
                    DataTablesHandler.getDrunkennessHit(c.getName())
                            * c.getAmount(), c.getName(),
                    this.boozeCategoryString);
            break;
        default:
            this.addValue(
                    DataTablesHandler.getSpleenHit(c.getName()) * c.getAmount(),
                    c.getName(), this.spleenCategoryString);
        }
    }

    void addLeftoverOrganHits(final ConsumptionDayStats consumptionStats) {
        if (consumptionStats.getTotalFullnessHit() < 15) {
            this.addValue(15 - consumptionStats.getTotalFullnessHit(),
                    "Nothing", this.foodCategoryString);
        }
        if (consumptionStats.getTotalDrunkennessHit() < 15) {
            this.addValue(15 - consumptionStats.getTotalDrunkennessHit(),
                    "Nothing", this.boozeCategoryString);
        }
        if (consumptionStats.getTotalSpleenHit() < 15) {
            this.addValue(15 - consumptionStats.getTotalSpleenHit(), "Nothing",
                    this.spleenCategoryString);
        }
    }

    Consumable getConsumable(final String consumableName) {
        return this.consumables.get(consumableName);
    }
}
