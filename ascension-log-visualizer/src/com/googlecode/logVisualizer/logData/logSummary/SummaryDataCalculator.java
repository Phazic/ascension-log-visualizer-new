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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.LogDataHolder.CharacterClass;
import com.googlecode.logVisualizer.logData.LogDataHolder.ParsedLogClass;
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.SingleTurn.TurnVersion;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.TurnInterval.FreeRunaways;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.util.CountableSet;
import com.googlecode.logVisualizer.util.DataCounter;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.DataTablesHandler;
import com.googlecode.logVisualizer.util.LookAheadIterator;

/**
 * A calculator for various summaries of an ascension log. This class makes use
 * of pretty much all features of the {@link TurnInterval} class, thus if not
 * all possibilities of the {@link TurnInterval} class are used, because for
 * example the source data from which the turn interval was created didn't
 * contain the information, this calculator might not be able to create some of
 * its summaries. These cases should be pretty obvious though. If for example
 * the turn interval does not contain a record of every single turn, it cannot
 * make calculations which are based on such data.
 * <p>
 * Note that this class is immutable, while some of its members may be mutable.
 * This has to be taken into account while using this class.
 */
final class SummaryDataCalculator {
    private static final Skill OLFACTION = new Skill("transcendent olfaction",
            1);
    private static final Consumable ODOR_EXTRACTOR = Consumable
            .newOtherConsumable("Odor Extractor", 0, 1);
    private static final String GUILD_CHALLENGE = "Guild Challenge";
    private static final String ENCHANTED_BARBELL = "enchanted barbell";
    private static final String CONCENTRATED_MAGICALNESS_PILL = "concentrated magicalness pill";
    private static final String GIANT_MOXIE_WEED = "giant moxie weed";
    private static final Map<Integer, Integer> LEVEL_STAT_BOARDERS_MAP;
    static {
        LEVEL_STAT_BOARDERS_MAP = new HashMap<>(45);
        // Sets the stat boarders from level 1 to 35.
        SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP.put(1, 0);
        for (int i = 2; i <= 35; i++) {
            SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP.put(i,
                    ((i - 1) * (i - 1)) + 4);
        }
        // Day number has to be set to the same value as is used by the internal
        // mafia parser, otherwise this object won't match with the Odor
        // Extractors logged by the aforementioned parser.
        SummaryDataCalculator.ODOR_EXTRACTOR
                .setDayNumberOfUsage(Integer.MAX_VALUE);
    }
    private final CountableSet<Consumable> consumablesUsed = new CountableSet<>();
    private final CountableSet<Item> droppedItems = new CountableSet<>();
    private final CountableSet<Skill> skillsCast = new CountableSet<>();
    private final DataCounter<String> turnsPerArea = new DataCounter<>(
            200);
    private final DataCounter<String> familiarUsage = new DataCounter<>();
    private final List<LevelData> levels = new ArrayList<>(15);
    private final List<DataNumberPair<String>> huntedCombats = new ArrayList<>();
    private final List<DataNumberPair<String>> disintegratedCombats = new ArrayList<>();
    private final List<DataNumberPair<String>> semirares = new ArrayList<>();
    private final List<DataNumberPair<String>> badmoonAdventures = new ArrayList<>();
    private final ConsumptionSummary consumptionSummary;
    private final FreeRunaways freeRunaways;
    private final Sewer sewer = new Sewer();
    private final Goatlet goatlet = new Goatlet();
    private final InexplicableDoor nesRealm = new InexplicableDoor();
    private final QuestTurncounts questTurncounts;
    private Statgain totalStatgains = new Statgain();
    private Statgain combatsStatgains = new Statgain();
    private Statgain noncombatsStatgains = new Statgain();
    private Statgain othersStatgains = new Statgain();
    private final MPGain mpGains = new MPGain();
    private final MeatSummary meatSummary = new MeatSummary();
    private final int totalAmountSkillCasts;
    private final int totalMPUsed;
    private final int totalMeatGain;
    private final int totalMeatSpent;
    private final int totalTurnsFromRollover;
    private final int totalTurnsCombat;
    private final int totalTurnsNoncombat;
    private final int totalTurnsOther;

    SummaryDataCalculator(final LogDataHolder logData) {
        if (logData == null) {
            throw new NullPointerException("Log data holder must not be null.");
        }
        final List<Consumable> consumables = new ArrayList<>(100);
        // Objects needed for familiar usage summary.
        final LookAheadIterator<FamiliarChange> index = new LookAheadIterator<>(
                logData.getFamiliarChanges().iterator());
        FamiliarChange currentFamiliar = index.hasNext() ? index.next() : null;
        int totalFreeRunawaysTries = 0;
        int successfulFreeRunaways = 0;
        int totalTurnsCombat = 0;
        int totalTurnsNoncombat = 0;
        int totalTurnsOther = 0;
        int totalMeatGain = 0;
        int totalMeatSpent = 0;
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            // Consumables summary, day of usage is only a hindrance here.
            for (final Consumable c : ti.getConsumablesUsed()) {
                this.totalStatgains = this.totalStatgains.addStats(c
                        .getStatGain());
                final Consumable tmp = c.newInstance();
                tmp.setDayNumberOfUsage(Integer.MAX_VALUE);
                this.consumablesUsed.addElement(tmp);
            }
            consumables.addAll(ti.getConsumablesUsed());
            // Item summary
            for (final Item i : ti.getDroppedItems()) {
                this.droppedItems.addElement(i);
            }
            // Skill summary
            for (final Skill s : ti.getSkillsCast()) {
                this.skillsCast.addElement(s);
            }
            // MP summary
            this.mpGains.addMPGains(ti.getMPGain());
            // Turns per area summary
            if (ti.getTotalTurns() > 0) {
                this.turnsPerArea.addDataElement(ti.getAreaName(),
                        ti.getTotalTurns());
            }
            for (final SingleTurn st : ti.getTurns()) {
                // Total turncounts and stats of different turn versions.
                this.totalStatgains = this.totalStatgains.addStats(st
                        .getStatGain());
                switch (st.getTurnVersion()) {
                case COMBAT:
                    totalTurnsCombat++;
                    this.combatsStatgains = this.combatsStatgains.addStats(st
                            .getStatGain());
                    break;
                case NONCOMBAT:
                    totalTurnsNoncombat++;
                    this.noncombatsStatgains = this.noncombatsStatgains
                            .addStats(st.getStatGain());
                    break;
                case OTHER:
                    totalTurnsOther++;
                    this.othersStatgains = this.othersStatgains.addStats(st
                            .getStatGain());
                    break;
                default:
                    break;
                }
                // Familiar usage summary
                if ((st.getTurnVersion() == TurnVersion.COMBAT)
                        && (currentFamiliar != null)) {
                    while (index.hasNext()
                            && (st.getTurnNumber() > index.peek()
                                    .getTurnNumber())) {
                        currentFamiliar = index.next();
                    }
                    if (currentFamiliar != null) {
                        this.familiarUsage.addDataElement(currentFamiliar
                                .getFamiliarName());
                    }
                }
                // Hunted combats summary
                if ((st.getTurnVersion() == TurnVersion.COMBAT)
                        && (st.isSkillCast(SummaryDataCalculator.OLFACTION) || st
                                .isConsumableUsed(SummaryDataCalculator.ODOR_EXTRACTOR))) {
                    this.huntedCombats.add(DataNumberPair.of(
                            st.getEncounterName(), st.getTurnNumber()));
                }
                // Disintegrated combats summary
                if (st.isDisintegrated()) {
                    this.disintegratedCombats.add(DataNumberPair.of(
                            st.getEncounterName(), st.getTurnNumber()));
                }
                // Semirare summary
                if (DataTablesHandler.isSemirareEncounter(st)) {
                    this.semirares.add(DataNumberPair.of(st.getEncounterName(),
                            st.getTurnNumber()));
                }
                // Bad Moon summary
                if (DataTablesHandler.isBadMoonEncounter(st)) {
                    this.badmoonAdventures.add(DataNumberPair.of(
                            st.getEncounterName(), st.getTurnNumber()));
                }
            }
            // Free runaways summary
            totalFreeRunawaysTries += ti.getFreeRunaways()
                    .getNumberOfAttemptedRunaways();
            successfulFreeRunaways += ti.getFreeRunaways()
                    .getNumberOfSuccessfulRunaways();
            // Sewer summary
            if (ti.getAreaName().equals("Unlucky Sewer")
                    || ti.getAreaName().equals("Sewer With Clovers")) {
                this.sewer.setTurnsSpent(this.sewer.getTurnsSpent()
                        + ti.getTotalTurns());
                // If there is a single turn list, look through it for the turn
                // numbers.
                if (!ti.getTurns().isEmpty()) {
                    for (final SingleTurn st : ti.getTurns()) {
                        for (final Item i : st.getDroppedItems()) {
                            if (i.getName().startsWith("worthless")) {
                                this.sewer.setTrinketsFound(this.sewer
                                        .getTrinketsFound() + 1);
                                this.sewer.addTrinketsTurnNumber(st
                                        .getTurnNumber());
                            }
                        }
                    }
                } else {
                    for (final Item i : ti.getDroppedItems()) {
                        if (i.getName().startsWith("worthless")) {
                            this.sewer.setTrinketsFound(this.sewer
                                    .getTrinketsFound() + i.getAmount());
                            // Not really correct but has to do for now.
                            this.sewer.addTrinketsTurnNumber(ti.getEndTurn());
                        }
                    }
                }
            }
            // Goatlet summary
            if (ti.getAreaName().equals("Goatlet")) {
                this.goatlet.setTurnsSpent(this.goatlet.getTurnsSpent()
                        + ti.getTotalTurns());
                for (final SingleTurn st : ti.getTurns()) {
                    if (st.getEncounterName().equals("dairy goat")) {
                        this.goatlet.setDairyGoatsFound(this.goatlet
                                .getDairyGoatsFound() + 1);
                    }
                }
                for (final Item i : ti.getDroppedItems()) {
                    if (i.getName().equals("goat cheese")) {
                        this.goatlet.setCheeseFound(this.goatlet
                                .getCheeseFound() + i.getAmount());
                    } else if (i.getName().equals("glass of goat's milk")) {
                        this.goatlet.setMilkFound(this.goatlet.getMilkFound()
                                + i.getAmount());
                    }
                }
            }
            // 8-Bit Realm summary
            if (ti.getAreaName().equals("8-Bit Realm")) {
                this.nesRealm.setTurnsSpent(this.nesRealm.getTurnsSpent()
                        + ti.getTotalTurns());
                for (final SingleTurn st : ti.getTurns()) {
                    if (st.getEncounterName().equals("Bullet Bill")) {
                        this.nesRealm.setBulletsFound(this.nesRealm
                                .getBulletsFound() + 1);
                    } else if (st.getEncounterName().equals("Blooper")) {
                        this.nesRealm.setBloopersFound(this.nesRealm
                                .getBloopersFound() + 1);
                    }
                }
            }
            // Meat gain/spent
            // Nuns encounter meat ignored here.
            if (!ti.getAreaName().equals("Themthar Hills")) {
                totalMeatGain += ti.getEncounterMeatGain();
            }
            totalMeatGain += ti.getOtherMeatGain();
            totalMeatSpent += ti.getMeatSpent();
        }
        this.freeRunaways = new FreeRunaways(totalFreeRunawaysTries,
                successfulFreeRunaways);
        this.totalTurnsCombat = totalTurnsCombat;
        this.totalTurnsNoncombat = totalTurnsNoncombat;
        this.totalTurnsOther = totalTurnsOther;
        // Consumption summary
        this.consumptionSummary = new ConsumptionSummary(consumables,
                logData.getDayChanges());
        final int tempRolloverTurns = logData.getTurnsSpent().last()
                .getEndTurn()
                - this.consumptionSummary.getTotalTurnsFromFood()
                - this.consumptionSummary.getTotalTurnsFromBooze()
                - this.consumptionSummary.getTotalTurnsFromOther();
        this.totalTurnsFromRollover = tempRolloverTurns < 0 ? 0
                : tempRolloverTurns;
        // Total meat gain/spent
        this.totalMeatGain = totalMeatGain;
        this.totalMeatSpent = totalMeatSpent;
        // Total amount of skill casts and total MP used
        int totalAmountSkillCasts = 0;
        int totalMPUsed = 0;
        for (final Skill s : this.skillsCast.getElements()) {
            totalAmountSkillCasts += s.getAmount();
            totalMPUsed += s.getMpCost();
        }
        this.totalAmountSkillCasts = totalAmountSkillCasts;
        this.totalMPUsed = totalMPUsed;
        // Level data summary
        this.createLevelSummaryData(logData);
        // Meat per level summary
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            for (final SingleTurn st : ti.getTurns()) {
                if (!st.getMeat().isMeatGainSpentZero()) {
                    this.meatSummary.addLevelMeatData(
                            logData.getCurrentLevel(st.getTurnNumber())
                                    .getLevelNumber(), st.getMeat());
                }
            }
        }
        // Quest turncount summary
        this.questTurncounts = new QuestTurncounts(logData.getTurnsSpent(),
                this.droppedItems.getElements());
    }

    /**
     * Automatically creates the level summary from the turn rundown of the
     * ascension log.
     */
    private void createLevelSummaryData(final LogDataHolder logData) {
        final Iterator<PlayerSnapshot> plSsIter = logData.getPlayerSnapshots()
                .iterator();
        PlayerSnapshot currentPlayerSnapshot = plSsIter.hasNext() ? plSsIter
                .next() : null;
        int currentStatBoarder = SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP
                .get(2);
        Statgain stats = new Statgain();
        int combatTurns = 0;
        int noncombatTurns = 0;
        int otherTurns = 0;
        // Try to guess the character class if it isn't set yet.
        if (logData.getCharacterClass() == CharacterClass.NOT_DEFINED) {
            final Set<String> guildItems = new HashSet<>(5);
            for (final TurnInterval ti : logData.getTurnsSpent()) {
                if (ti.getAreaName().equals(
                        SummaryDataCalculator.GUILD_CHALLENGE)) {
                    for (final Item i : ti.getDroppedItems()) {
                        if (i.getName().equals(
                                SummaryDataCalculator.ENCHANTED_BARBELL)
                                || i.getName()
                                        .equals(SummaryDataCalculator.CONCENTRATED_MAGICALNESS_PILL)
                                || i.getName().equals(
                                        SummaryDataCalculator.GIANT_MOXIE_WEED)) {
                            guildItems.add(i.getName());
                        }
                    }
                }
            }
            if ((this.totalStatgains.mus > this.totalStatgains.myst)
                    && (this.totalStatgains.mus > this.totalStatgains.myst)) {
                if (guildItems.contains(SummaryDataCalculator.GIANT_MOXIE_WEED)) {
                    logData.setCharacterClass("Seal Clubber");
                } else {
                    logData.setCharacterClass("Turtle Tamer");
                }
            } else if ((this.totalStatgains.myst > this.totalStatgains.mus)
                    && (this.totalStatgains.myst > this.totalStatgains.mox)) {
                if (guildItems.contains(SummaryDataCalculator.GIANT_MOXIE_WEED)) {
                    logData.setCharacterClass("Sauceror");
                } else {
                    logData.setCharacterClass("Pastamancer");
                }
            } else if (guildItems
                    .contains(SummaryDataCalculator.CONCENTRATED_MAGICALNESS_PILL)) {
                logData.setCharacterClass("Accordion Thief");
            } else {
                logData.setCharacterClass("Disco Bandit");
            }
        }
        // Substats at the start of an ascension.
        switch (logData.getCharacterClass()) {
        case SEAL_CLUBBER:
            stats = new Statgain(15, 5, 10);
            break;
        case TURTLE_TAMER:
            stats = new Statgain(15, 10, 5);
            break;
        case PASTAMANCER:
            stats = new Statgain(10, 15, 5);
            break;
        case SAUCEROR:
            stats = new Statgain(5, 15, 10);
            break;
        case DISCO_BANDIT:
            stats = new Statgain(10, 5, 15);
            break;
        case ACCORDION_THIEF:
            stats = new Statgain(5, 10, 15);
            break;
        case NOT_DEFINED:
            break;
        default:
            break;
        }
        // Set level 1.
        this.levels.add(new LevelData(1, 0));
        this.levels.get(0).setStatsAtLevelReached(stats);
        for (final TurnInterval ti : logData.getTurnsSpent()) {
            for (final SingleTurn st : ti.getTurns()) {
                // Add stats to the stat counter.
                stats = stats.addStats(st.getStatGain());
                for (final Consumable c : st.getConsumablesUsed()) {
                    stats = stats.addStats(c.getStatGain());
                }
                if ((currentPlayerSnapshot != null)
                        && (currentPlayerSnapshot.getTurnNumber() <= st
                                .getTurnNumber())) {
                    final int playerMus = currentPlayerSnapshot
                            .getMuscleStats()
                            * currentPlayerSnapshot.getMuscleStats();
                    final int playerMyst = currentPlayerSnapshot.getMystStats()
                            * currentPlayerSnapshot.getMystStats();
                    final int playerMox = currentPlayerSnapshot.getMoxieStats()
                            * currentPlayerSnapshot.getMoxieStats();
                    // Player snapshot is always right, so if it says the player
                    // stats are higher, set them to that value.
                    if (playerMus > stats.mus) {
                        stats = stats.setMuscle(playerMus);
                    }
                    if (playerMyst > stats.myst) {
                        stats = stats.setMyst(playerMyst);
                    }
                    if (playerMox > stats.mox) {
                        stats = stats.setMoxie(playerMox);
                    }
                    currentPlayerSnapshot = plSsIter.hasNext() ? plSsIter
                            .next() : null;
                }
                // Increment the correct turn counter.
                switch (st.getTurnVersion()) {
                case COMBAT:
                    combatTurns++;
                    break;
                case NONCOMBAT:
                    noncombatTurns++;
                    break;
                case OTHER:
                    otherTurns++;
                    break;
                case NOT_DEFINED:
                    break;
                default:
                    break;
                }
                // Check whether a new level is reached and act accordingly.
                while (SummaryDataCalculator.isNewLevelReached(logData, currentStatBoarder,
                        stats)) {
                    final LevelData newLevel = this.computeNewLevelReached(
                            st.getTurnNumber(), stats, combatTurns,
                            noncombatTurns, otherTurns);
                    this.levels.add(newLevel);
                    currentStatBoarder = SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP
                            .get(newLevel.getLevelNumber() + 1);
                    combatTurns = 0;
                    noncombatTurns = 0;
                    otherTurns = 0;
                }
            }
        }
        // Add level data to the LogDataHolder if it isn't created from a
        // pre-parsed ascension log.
        if (logData.getParsedLogCreator() == ParsedLogClass.NOT_DEFINED) {
            for (final LevelData lvl : this.levels) {
                logData.addLevel(lvl);
            }
        }
    }

    private static boolean isNewLevelReached(final LogDataHolder logData,
            final int currentStatBoarder, final Statgain stats) {
        boolean isNewLevelReached = false;
        switch (logData.getCharacterClass().getStatClass()) {
        case MUSCLE:
            isNewLevelReached = currentStatBoarder <= Math.sqrt(stats.mus);
            break;
        case MYSTICALITY:
            isNewLevelReached = currentStatBoarder <= Math.sqrt(stats.myst);
            break;
        case MOXIE:
            isNewLevelReached = currentStatBoarder <= Math.sqrt(stats.mox);
            break;
        default:
            break;
        }
        return isNewLevelReached;
    }

    /**
     * Adds the still missing data to the current level and returns the next
     * level.
     */
    private LevelData computeNewLevelReached(final int currentTurnNumber,
            final Statgain currentStats, final int combatTurns,
            final int noncombatTurns, final int otherTurns) {
        final LevelData currentLevel = this.levels.get(this.levels.size() - 1);
        final LevelData newLevel = new LevelData(
                currentLevel.getLevelNumber() + 1, currentTurnNumber);
        final int turnDifference = currentTurnNumber
                - currentLevel.getLevelReachedOnTurn();
        final int substatAmountCurrentLevel = SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP
                .get(currentLevel.getLevelNumber())
                * SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP
                        .get(currentLevel.getLevelNumber());
        final int substatAmountNewLevel = SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP
                .get(newLevel.getLevelNumber())
                * SummaryDataCalculator.LEVEL_STAT_BOARDERS_MAP.get(newLevel
                        .getLevelNumber());
        currentLevel.setCombatTurns(combatTurns);
        currentLevel.setNoncombatTurns(noncombatTurns);
        currentLevel.setOtherTurns(otherTurns);
        if (turnDifference > 0) {
            currentLevel
                    .setStatGainPerTurn(((substatAmountNewLevel - substatAmountCurrentLevel) * 1.0)
                            / turnDifference);
        } else {
            currentLevel.setStatGainPerTurn(substatAmountNewLevel
                    - substatAmountCurrentLevel);
        }
        newLevel.setStatsAtLevelReached(currentStats);
        return newLevel;
    }

    /**
     * @return A list of areas and the turns spent in them.
     */
    List<DataNumberPair<String>> getTurnsPerArea() {
        return this.turnsPerArea.getCountedData();
    }

    /**
     * @return A list of all consumables used.
     */
    Collection<Consumable> getConsumablesUsed() {
        return this.consumablesUsed.getElements();
    }

    /**
     * @return A list of all items dropped.
     */
    Collection<Item> getDroppedItems() {
        return this.droppedItems.getElements();
    }

    /**
     * @return A list of all skills cast.
     */
    Collection<Skill> getSkillsCast() {
        return this.skillsCast.getElements();
    }

    /**
     * @return A list of all levels.
     */
    List<LevelData> getLevelData() {
        return this.levels;
    }

    /**
     * @return A list of all used familiars and how often they were used.
     */
    List<DataNumberPair<String>> getFamiliarUsage() {
        return this.familiarUsage.getCountedData();
    }

    /**
     * @return A list of all started hunts on combats.
     */
    List<DataNumberPair<String>> getHuntedCombats() {
        return this.huntedCombats;
    }

    /**
     * @return A list of all disintegrated combats.
     */
    List<DataNumberPair<String>> getDisintegratedCombats() {
        return this.disintegratedCombats;
    }

    /**
     * @return A list of all semirares.
     */
    List<DataNumberPair<String>> getSemirares() {
        return this.semirares;
    }

    /**
     * @return A list of all Bad Moon adventures.
     */
    List<DataNumberPair<String>> getBadmoonAdventures() {
        return this.badmoonAdventures;
    }

    /**
     * @return A summary on consumables used during the ascension.
     */
    ConsumptionSummary getConsumptionSummary() {
        return this.consumptionSummary;
    }

    /**
     * @return The free runaways over the whole ascension.
     */
    public FreeRunaways getFreeRunaways() {
        return this.freeRunaways;
    }

    /**
     * @return The RNG data of the Sewer.
     */
    Sewer getSewer() {
        return this.sewer;
    }

    /**
     * @return The RNG data of the Goatlet.
     */
    Goatlet getGoatlet() {
        return this.goatlet;
    }

    /**
     * @return The RNG data of the 8-Bit Realm.
     */
    InexplicableDoor get8BitRealm() {
        return this.nesRealm;
    }

    /**
     * @return The quest turncounts.
     */
    QuestTurncounts getQuestTurncounts() {
        return this.questTurncounts;
    }

    /**
     * @return The total mp gains collected during this ascension.
     */
    MPGain getMPGains() {
        return this.mpGains;
    }

    /**
     * @return The meat per level summary.
     */
    MeatSummary getMeatSummary() {
        return this.meatSummary;
    }

    /**
     * @return The total amount of substats collected during this ascension.
     */
    Statgain getTotalStatgains() {
        return this.totalStatgains;
    }

    /**
     * @return The total amount of substats from combats collected during this
     *         ascension.
     */
    Statgain getCombatsStatgains() {
        return this.combatsStatgains;
    }

    /**
     * @return The total amount of substats from noncombats collected during
     *         this ascension.
     */
    Statgain getNoncombatsStatgains() {
        return this.noncombatsStatgains;
    }

    /**
     * @return The total amount of substats from other encounters collected
     *         during this ascension.
     */
    Statgain getOthersStatgains() {
        return this.othersStatgains;
    }

    /**
     * @return The total amount of skill casts.
     */
    int getTotalAmountSkillCasts() {
        return this.totalAmountSkillCasts;
    }

    /**
     * @return The total amount of MP spent on skills.
     */
    int getTotalMPUsed() {
        return this.totalMPUsed;
    }

    /**
     * @return The total amount of meat gathered.
     */
    int getTotalMeatGain() {
        return this.totalMeatGain;
    }

    /**
     * @return The total amount of meat spent.
     */
    int getTotalMeatSpent() {
        return this.totalMeatSpent;
    }

    /**
     * @return The total amount of turns gained from rollover.
     */
    int getTotalTurnsFromRollover() {
        return this.totalTurnsFromRollover;
    }

    /**
     * @return The total amount of combat turns.
     */
    int getTotalTurnsCombat() {
        return this.totalTurnsCombat;
    }

    /**
     * @return The total amount of noncombat turns.
     */
    int getTotalTurnsNoncombat() {
        return this.totalTurnsNoncombat;
    }

    /**
     * @return The total amount of other (smithing, mixing, cooking, etc.)
     *         turns.
     */
    int getTotalTurnsOther() {
        return this.totalTurnsOther;
    }
}
