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
package com.googlecode.logVisualizer.parser.mafiaLogBlockParsers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.SingleTurn.TurnVersion;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.parser.lineParsers.EquipmentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MPGainLineParser.MPGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.MeatLineParser.MeatGainType;
import com.googlecode.logVisualizer.parser.lineParsers.MeatSpentLineParser;
import com.googlecode.logVisualizer.parser.lineParsers.StatLineParser;
import com.googlecode.logVisualizer.util.DataTablesHandler;

/**
 * A parser for the consumable used notation in mafia logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code use/eat/drink _amount_ _itemName_}
 * <p>
 * OR
 * <p>
 * {@code Buy and eat/drink _amount_ _itemName_ for _meatAmount_ Meat}
 * <p>
 * Further on, these lines can follow the described top line and will be parsed
 * (there may be other lines, but those will be ignored):
 * <p>
 * {@code You gain _amount_ _substatName_}
 * <p>
 * {@code You gain _amount_ Meat}
 * <p>
 * {@code You gain _amount_ Adventure/Adventures}
 * <p>
 * {@code You gain _amount_ Mana/Mojo/Muscularity Points}
 * <p>
 * {@code You lose _amount_ _substatName_}
 * <p>
 * {@code You lose _amount_ Meat}
 * <p>
 * {@code You lose _amount_ Adventure/Adventures}
 */
public final class ConsumableBlockParser implements LogBlockParser {
    private static final Pattern CONSUMABLE_BOUGHT_USED_CAPTURE_PATTERN = Pattern
            .compile("([\\w\\s]+) (\\d+) (.+) for \\d+ Meat");
    private static final Pattern CONSUMABLE_USED_CAPTURE_PATTERN = Pattern
            .compile("([\\w\\s]+) (\\d+) (.+)");
    private static final String FOOD_STRING = "eat";
    private static final String BOOZE_STRING = "drink";
    private static final String ADVENTURE_STRING = "Adventure";
    private static final String LOSE_STRING = "You lose";
    private static final String LLAMA_COCKROACH_ENCOUNTER_STRING = "Encounter: Form of...Cockroach!";
    private static final String COCKROACH_AREA_ENCOUNTER_NAME = "Form of...Cockroach!";
    private static final Set<String> STEEL_CONSUMABLES = new HashSet<>(
            Arrays.asList("steel margarita", "steel lasagna",
                    "steel-scented air freshener"));
    private final MPGainLineParser mpParser = new MPGainLineParser(
            MPGainType.CONSUMABLE);
    private final MeatLineParser meatGainParser = new MeatLineParser(
            MeatGainType.OTHER);
    private final MeatSpentLineParser meatSpentParser = new MeatSpentLineParser();
    private final EquipmentLineParser outfitChangeParser = new EquipmentLineParser();
    // Only use for Llama cockroach stats parsing.
    private final StatLineParser statsParser = new StatLineParser();
    // Only use for Llama cockroach mp gains parsing.
    private final MPGainLineParser mpParserLlama = new MPGainLineParser(
            MPGainType.ENCOUNTER);
    private final Matcher consumableBoughtMatcher = ConsumableBlockParser.CONSUMABLE_BOUGHT_USED_CAPTURE_PATTERN
            .matcher(UsefulPatterns.EMPTY_STRING);
    private final Matcher gainLoseMatcher = UsefulPatterns.GAIN_LOSE
            .matcher(UsefulPatterns.EMPTY_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseBlock(final List<String> block, final LogDataHolder logData) {
        // First, parse item name and amount used.
        final String consumptionLine = block.get(0);
        final Matcher result;
        try (final Scanner scanner = new Scanner(consumptionLine)) {
            if (this.consumableBoughtMatcher.reset(consumptionLine).matches()) {
                result = ConsumableBlockParser.CONSUMABLE_BOUGHT_USED_CAPTURE_PATTERN
                        .matcher(consumptionLine);
            } else {
                result = ConsumableBlockParser.CONSUMABLE_USED_CAPTURE_PATTERN
                        .matcher(consumptionLine);
            }
            result.find();
            final String usageIdentifier = result.group(1);
            final int amount = Integer.parseInt(result.group(2));
            final String itemName = result.group(3);
            int adventureGain = 0;
            Statgain consumableStatgain = new Statgain();
            scanner.close();
            // Amount equal or smaller than zero cannot nor should be further
            // processed.
            if (amount <= 0) {
                return;
            }
            for (int i = 1; i < block.size(); i++) {
                final String line = block.get(i);
                // Llama gongs going cockroach need special handling due to the
                // way
                // mafia logs the Cockroach path.
                if (line.equals(ConsumableBlockParser.LLAMA_COCKROACH_ENCOUNTER_STRING)) {
                    this.parseLlamaCockraochUsage(
                            block.subList(i, block.size()), logData);
                    break;
                }
                if (this.mpParser.parseLine(line, logData)
                        || this.meatGainParser.parseLine(line, logData)
                        || this.meatSpentParser.parseLine(line, logData)
                        || this.outfitChangeParser.parseLine(line, logData)) {
                    // Empty block, because the parsing has already happened if
                    // we
                    // get in here.
                } else if (this.gainLoseMatcher.reset(line).matches()) {
                    final Matcher m = UsefulPatterns.GAIN_LOSE_CAPTURE_PATTERN
                            .matcher(line);
                    m.find();
                    int gainAmount;
                    if (m.group(1).contains(UsefulPatterns.COMMA)) {
                        gainAmount = Integer.parseInt(m.group(1).replace(
                                UsefulPatterns.COMMA,
                                UsefulPatterns.EMPTY_STRING));
                    } else {
                        gainAmount = Integer.parseInt(m.group(1));
                    }
                    final String gainIdentifier = m.group(2);
                    scanner.close();
                    if (line.startsWith(ConsumableBlockParser.LOSE_STRING)) {
                        gainAmount *= -1;
                    }
                    if (gainIdentifier
                            .startsWith(ConsumableBlockParser.ADVENTURE_STRING)) {
                        adventureGain += gainAmount;
                    } else if (UsefulPatterns.MUSCLE_SUBSTAT_NAMES
                            .contains(gainIdentifier)) {
                        consumableStatgain = consumableStatgain.addStats(
                                gainAmount, 0, 0);
                    } else if (UsefulPatterns.MYST_SUBSTAT_NAMES
                            .contains(gainIdentifier)) {
                        consumableStatgain = consumableStatgain.addStats(0,
                                gainAmount, 0);
                    } else if (UsefulPatterns.MOXIE_SUBSTAT_NAMES
                            .contains(gainIdentifier)) {
                        consumableStatgain = consumableStatgain.addStats(0, 0,
                                gainAmount);
                    }
                }
            }
            // Only add consumable if there was some specific data to it or it
            // was
            // one of the Azazel quest rewards.
            if ((adventureGain != 0)
                    || !consumableStatgain.isAllStatsZero()
                    || ConsumableBlockParser.STEEL_CONSUMABLES
                            .contains(itemName)) {
                // Add consumable to the turn interval. While the Consumable
                // class
                // is mutable, no special actions need to be made because of it,
                // because the AbstractTurn class does this already internally.
                final TurnInterval currentInterval = logData.getTurnsSpent()
                        .last();
                final int currentTurn = currentInterval.getEndTurn();
                final Consumable tmpCon;
                if (usageIdentifier.contains(ConsumableBlockParser.FOOD_STRING)) {
                    tmpCon = Consumable.newFoodConsumable(itemName,
                            adventureGain, amount, currentTurn);
                } else if (usageIdentifier
                        .contains(ConsumableBlockParser.BOOZE_STRING)) {
                    tmpCon = Consumable.newBoozeConsumable(itemName,
                            adventureGain, amount, currentTurn);
                } else if ((DataTablesHandler.getSpleenHit(itemName) > 0)
                        && (adventureGain > 0)) {
                    tmpCon = Consumable.newSpleenConsumable(itemName,
                            adventureGain, amount, currentTurn);
                } else {
                    tmpCon = Consumable.newOtherConsumable(itemName,
                            adventureGain, amount, currentTurn);
                }
                tmpCon.setDayNumberOfUsage(logData.getLastDayChange()
                        .getDayNumber());
                tmpCon.setStatGain(consumableStatgain);
                currentInterval.addConsumableUsed(tmpCon);
            }
        }
    }

    private void parseLlamaCockraochUsage(final List<String> block,
            final LogDataHolder logData) {
        // The way this is implemented means that the cockroach stat gains
        // will be added to the third turn. Fixing this would however mean
        // even more hardcoding for it than is already done here and it
        // would be even less elegant, so it won't be done (for now). Mafia
        // might possibly fix its logging of these turns which is another
        // reason to not do it here.
        final int lastTurnNumber = logData.getTurnsSpent().last().getEndTurn();
        final EquipmentChange lastEquipment = logData.getLastEquipmentChange();
        final FamiliarChange lastFamiliar = logData.getLastFamiliarChange();
        final SingleTurn tmpTurn1 = new SingleTurn(
                ConsumableBlockParser.COCKROACH_AREA_ENCOUNTER_NAME,
                ConsumableBlockParser.COCKROACH_AREA_ENCOUNTER_NAME,
                lastTurnNumber + 1, lastEquipment, lastFamiliar);
        final SingleTurn tmpTurn2 = new SingleTurn(
                ConsumableBlockParser.COCKROACH_AREA_ENCOUNTER_NAME,
                ConsumableBlockParser.COCKROACH_AREA_ENCOUNTER_NAME,
                lastTurnNumber + 2, lastEquipment, lastFamiliar);
        final SingleTurn tmpTurn3 = new SingleTurn(
                ConsumableBlockParser.COCKROACH_AREA_ENCOUNTER_NAME,
                ConsumableBlockParser.COCKROACH_AREA_ENCOUNTER_NAME,
                lastTurnNumber + 3, lastEquipment, lastFamiliar);
        tmpTurn1.setTurnVersion(TurnVersion.OTHER);
        tmpTurn2.setTurnVersion(TurnVersion.OTHER);
        tmpTurn3.setTurnVersion(TurnVersion.OTHER);
        logData.addTurnSpent(tmpTurn1);
        logData.addTurnSpent(tmpTurn2);
        logData.addTurnSpent(tmpTurn3);
        // Parse lines of interest from cockroach path.
        for (final String line : block) {
            this.statsParser.parseLine(line, logData);
            this.mpParserLlama.parseLine(line, logData);
            this.outfitChangeParser.parseLine(line, logData);
        }
    }
}
