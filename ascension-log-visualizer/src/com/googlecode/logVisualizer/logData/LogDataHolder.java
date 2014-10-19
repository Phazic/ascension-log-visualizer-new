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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.logSummary.LevelData;
import com.googlecode.logVisualizer.logData.logSummary.LogSummaryData;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.logData.turn.turnAction.Pull;
import com.googlecode.logVisualizer.util.DataNumberPair;

/**
 * This class is basically the representation of an ascension log. It can hold
 * all the important data accumulated during an ascension.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class LogDataHolder {
    private String logName;
    // A little ugly, but since the TreeSet only checks based on compareTo() and
    // not equals() and the TurnInterval compareTo() method shouldn't be changed
    // just to make it easier to use in a TreeSet, this Comparator is used
    // instead. It should be good enough for this purpose.
    private final SortedSet<TurnInterval> turnsSpent = new TreeSet<>(
            new Comparator<TurnInterval>() {
                @Override
                public int compare(final TurnInterval o1, final TurnInterval o2) {
                    // Compare start turns
                    final int startTurnComparison = o1.getStartTurn()
                            - o2.getStartTurn();
                    // Return value, or if that fails, compare end turns.
                    return startTurnComparison != 0 ? startTurnComparison : o1
                            .getEndTurn() - o2.getEndTurn();
                }
            });
    private final SortedMap<Integer, FamiliarChange> familiarChanges = new TreeMap<>();
    private final SortedMap<Integer, DayChange> dayChanges = new TreeMap<>();
    private final SortedMap<Integer, LevelData> levels = new TreeMap<>();
    private final SortedMap<Integer, PlayerSnapshot> playerSnapshots = new TreeMap<>();
    private final SortedMap<Integer, EquipmentChange> equipmentChanges = new TreeMap<>();
    private final List<Pull> pulls = new ArrayList<>(100);
    private final List<DataNumberPair<String>> lostCombats = new ArrayList<>();
    private CharacterClass characterClass = CharacterClass.NOT_DEFINED;
    private ParsedLogClass parsedLogCreator = ParsedLogClass.NOT_DEFINED;
    private LogSummaryData logSummary;

    public LogDataHolder() {
        // The start of an ascension is always on day 1.
        this.dayChanges.put(1, new DayChange(1, 0));
        // A new ascension starts at level 1.
        this.levels.put(1, new LevelData(1, 0));
        // You don't have anything equipped at the start of an ascension.
        this.equipmentChanges.put(0, new EquipmentChange(0));
        // The familiar is not known at the very start of an ascension.
        this.familiarChanges.put(0, new FamiliarChange("none", 0));
        // A dummy turn for the start of an ascension.
        this.addTurnSpent(new SingleTurn("Ascension Start", "Ascension Start",
                0, this.getLastEquipmentChange(), this.getLastFamiliarChange()));
    }

    /**
     * Creates the log summary from the data of this log.
     * <p>
     * Calling this method should be done after all data additions are finished
     * (through parsing a log or otherwise). Otherwise, the summary will be
     * incomplete.
     */
    public void createLogSummary() {
        this.logSummary = new LogSummaryData(this);
    }

    /**
     * @return A summary of various parts of this ascension log.
     * @throws IllegalStateException
     *             if this method is called before a log summary is created by
     *             calling {@link #createLogSummary()}
     */
    public LogSummaryData getLogSummary() {
        if (this.logSummary == null) {
            throw new IllegalStateException(
                    "Log summary has to be created first.");
        }
        return this.logSummary;
    }

    /**
     * Set the name of this ascension log. The format should be
     * CharacterName-StartdateOfAscension.
     *
     * @param logName
     *            The name of this log.
     */
    public void setLogName(final String logName) {
        if (logName == null) {
            throw new NullPointerException("Log name must not be null.");
        }
        this.logName = logName;
    }

    /**
     * The name of this ascension log. The Format <b>usually</b> is:
     * CharacterName-StartdateOfAscension
     *
     * @return The name of this ascension log.
     */
    public String getLogName() {
        return this.logName;
    }

    /**
     * @param turnInterval
     *            The turn interval to add.
     */
    public void addTurnsSpent(final TurnInterval turnInterval) {
        if (turnInterval == null) {
            throw new NullPointerException("Turn interval must not be null.");
        }
        // Remove any existing turn interval in the set that is equal to
        // turnInterval as defined by the comparator of turnsSpent.
        if (this.turnsSpent.contains(turnInterval)) {
            // Add all the data from the interval which should be removed to the
            // interval that comes before it, or if there is none, to the new
            // interval so the data is not lost.
            final SortedSet<TurnInterval> tmp = this.turnsSpent
                    .headSet(turnInterval);
            final TurnInterval equalInterval = this.turnsSpent.tailSet(
                    turnInterval).first();
            if (!tmp.isEmpty()) {
                tmp.last().addTurnIntervalData(equalInterval);
            } else {
                turnInterval.addTurnIntervalData(equalInterval);
            }
            this.turnsSpent.remove(equalInterval);
        } else if (!this.turnsSpent.isEmpty()) {
            final TurnInterval lastInterval = this.turnsSpent.last();
            if (lastInterval.getAreaName().equals(turnInterval.getAreaName())) {
                for (final SingleTurn st : turnInterval.getTurns()) {
                    lastInterval.addTurn(st);
                }
                lastInterval.addNotes(turnInterval.getNotes());
                lastInterval.incrementSuccessfulFreeRunaways(turnInterval
                        .getFreeRunaways().getNumberOfSuccessfulRunaways());
                return;
            }
        }
        this.turnsSpent.add(turnInterval);
    }

    /**
     * Add the given turn to the log data.
     * <p>
     * Note that the integrity of the log data cannot be guaranteed if the given
     * single turn would be entered right in the middle of already existing turn
     * intervals. ({@code turn.getTurnNumber() >
     * getTurnsSpent().first().getStartTurn() && turn.getTurnNumber() <
     * getTurnsSpent().last().getEndTurn()})
     *
     * @param turn
     *            The single turn to add.
     */
    public void addTurnSpent(final SingleTurn turn) {
        if (turn == null) {
            throw new NullPointerException("Turn must not be null.");
        }
        // 1. If the turn rundown collection isn't empty and the last turn
        // interval's area name is equal to that of the given single turn, add
        // the turn to the last interval.
        // 2. If the last turn interval ends on the same turn number as the
        // given turn, remove the last turn from the last turn interval, make
        // sure the integrity of the turn interval collection is not compromised
        // and then proceed as stated in 3..
        // 3. Otherwise create a new interval and add it to the turn rundown
        // collection.
        if (!this.turnsSpent.isEmpty()) {
            TurnInterval lastInterval = this.turnsSpent.last();
            // Check for turn count errors as they sometimes occur in mafia
            // logs. If one is found, fully remove the intervals that seem to be
            // out of place in the turn interval collection.
            while ((lastInterval != null)
                    && ((turn.getTurnNumber() - lastInterval.getEndTurn()) <= -100)) {
                this.turnsSpent.remove(lastInterval);
                lastInterval = this.turnsSpent.last();
            }
            if (lastInterval != null) {
                if (lastInterval.getAreaName().equals(turn.getAreaName())) {
                    lastInterval.addTurn(turn);
                    return;
                } else if (lastInterval.getEndTurn() == turn.getTurnNumber()) {
                    final SingleTurn equalTurn = lastInterval.removeLastTurn();
                    // If the skipped turn was a runaway and the Navel Ring was
                    // equipped, it means that it was a successful usage of the
                    // Navel Ring.
                    if (equalTurn.isRanAwayOnThisTurn()
                            && equalTurn.isNavelRingEquipped()) {
                        lastInterval.incrementSuccessfulFreeRunaways(1);
                    }
                    if (lastInterval.getTurns().isEmpty()) {
                        turn.addSingleTurnData(equalTurn);
                        this.turnsSpent.remove(lastInterval);
                        // The removed interval may contain free runaways
                        // usages.
                        if (!this.turnsSpent.isEmpty()) {
                            this.turnsSpent
                                    .last()
                                    .incrementSuccessfulFreeRunaways(
                                            lastInterval
                                                    .getFreeRunaways()
                                                    .getNumberOfSuccessfulRunaways());
                        }
                    }
                }
            }
        }
        this.addTurnsSpent(new TurnInterval(turn));
    }

    /**
     * Returns a sorted set of all turn intervals of this ascension log.
     * <p>
     * Note that the given set and its contents is directly backed by the
     * internal collections of this class. This means that changing elements
     * will in the same way effect the internal collections. In general, it is
     * best not to change any elements, but if it is done, it must not change
     * the ordering of this set, otherwise the change will corrupt it.
     * <p>
     * Also, note that the returned collection is read-only.
     *
     * @return The turns spent.
     */
    public SortedSet<TurnInterval> getTurnsSpent() {
        return Collections.unmodifiableSortedSet(this.turnsSpent);
    }

    /**
     * Returns a sub interval of this LogDataHolder that includes all turn
     * intervals and other data that is inside the given interval (both
     * {@code startTurn} and {@code endTurn} are inclusive).
     * <p>
     * Note that turn intervals that start before the interval, but end inside
     * it will be included in the returned LogDataHolder. The same is true for
     * turn intervals that start inside the interval and end outside it.
     *
     * @param startTurn
     *            The start of the interval.
     * @param endTurn
     *            The end of the interval.
     * @return A LogDataHolder including all the data inside the given start and
     *         end points.
     * @throws IllegalArgumentException
     *             if {@code endTurn} is not greater than {@code startTurn}; if
     *             {@code endTurn} is not greater than 0
     */
    public LogDataHolder getSubIntervalLogData(final int startTurn,
            final int endTurn) {
        if (endTurn <= startTurn) {
            throw new IllegalArgumentException(
                    "The end turn must be greater than the start turn.");
        }
        if (endTurn <= 0) {
            throw new IllegalArgumentException(
                    "The end turn must be greater than zero.");
        }
        final LogDataHolder subLog = new LogDataHolder();
        subLog.logName = this.logName;
        subLog.parsedLogCreator = this.parsedLogCreator;
        subLog.characterClass = this.characterClass;
        // Remove "Ascension Start" turn interval that is always included in a
        // newly created LogDataHolder.
        subLog.turnsSpent.clear();
        // Add turn intervals.
        for (final TurnInterval ti : this.turnsSpent) {
            // Stop the iteration once we are outside the interval
            if (ti.getStartTurn() > endTurn) {
                break;
            }
            // Both start and end are inside the interval
            if ((ti.getStartTurn() >= startTurn)
                    && (ti.getEndTurn() <= endTurn)) {
                subLog.turnsSpent.add(ti);
            }
            // The start is not, but the end is inside the interval
            if ((ti.getEndTurn() <= endTurn) && (ti.getEndTurn() > startTurn)) {
                subLog.turnsSpent.add(ti);
            }
            // The start is inside the interval, but the end is not
            if ((ti.getStartTurn() >= startTurn)
                    && (ti.getStartTurn() < endTurn)) {
                subLog.turnsSpent.add(ti);
            }
        }
        // Add familiar changes
        final FamiliarChange famChange = this
                .getLastFamiliarChangeBeforeTurn(startTurn);
        if (famChange != null) {
            subLog.addFamiliarChange(famChange);
        }
        for (final FamiliarChange fc : this.getFamiliarChanges()) {
            // Stop the iteration once we are outside the interval
            if (fc.getTurnNumber() > endTurn) {
                break;
            }
            // Add if inside the interval
            if ((fc.getTurnNumber() >= startTurn)
                    && (fc.getTurnNumber() <= endTurn)) {
                subLog.addFamiliarChange(fc);
            }
        }
        // Add day changes
        for (final DayChange dc : this.getDayChanges()) {
            // Stop the iteration once we are outside the interval
            if (dc.getTurnNumber() > endTurn) {
                break;
            }
            // Add if inside the interval
            if ((dc.getTurnNumber() >= startTurn)
                    && (dc.getTurnNumber() < endTurn)) {
                subLog.addDayChange(dc);
            }
        }
        // Add level changes
        LevelData lastLevelBeforeInterval = null;
        for (final LevelData ld : this.getLevels()) {
            // Stop the iteration once we are outside the interval
            if (ld.getLevelReachedOnTurn() > endTurn) {
                break;
            }
            if (ld.getLevelReachedOnTurn() < startTurn) {
                lastLevelBeforeInterval = ld;
            }
            // Add if inside the interval
            if ((ld.getLevelReachedOnTurn() >= startTurn)
                    && (ld.getLevelReachedOnTurn() <= endTurn)) {
                subLog.addLevel(ld);
            }
        }
        if (lastLevelBeforeInterval != null) {
            subLog.addLevel(lastLevelBeforeInterval);
        }
        // Add player snapshots
        final PlayerSnapshot playSnap = this
                .getLastPlayerSnapshotBeforeTurn(startTurn);
        if (playSnap != null) {
            subLog.addPlayerSnapshot(playSnap);
        }
        for (final PlayerSnapshot ps : this.getPlayerSnapshots()) {
            // Stop the iteration once we are outside the interval
            if (ps.getTurnNumber() > endTurn) {
                break;
            }
            // Add if inside the interval
            if ((ps.getTurnNumber() >= startTurn)
                    && (ps.getTurnNumber() < endTurn)) {
                subLog.addPlayerSnapshot(ps);
            }
        }
        // Add equipment changes
        final EquipmentChange equipChange = this
                .getLastEquipmentChangeBeforeTurn(startTurn);
        if (equipChange != null) {
            subLog.addEquipmentChange(equipChange);
        }
        for (final EquipmentChange ec : this.getEquipmentChanges()) {
            // Stop the iteration once we are outside the interval
            if (ec.getTurnNumber() > endTurn) {
                break;
            }
            // Add if inside the interval
            if ((ec.getTurnNumber() > startTurn)
                    && (ec.getTurnNumber() < endTurn)) {
                subLog.addEquipmentChange(ec);
            }
        }
        // Add pulls
        final Set<Integer> includedDays = new HashSet<>();
        for (final DayChange dc : subLog.getDayChanges()) {
            includedDays.add(dc.getDayNumber());
        }
        for (final Pull p : this.getPulls()) {
            // Stop the iteration once we are outside the interval
            if (p.getTurnNumber() > endTurn) {
                break;
            }
            // Add if inside the interval
            if ((p.getTurnNumber() >= startTurn)
                    && (p.getTurnNumber() <= endTurn)
                    && includedDays.contains(p.getDayNumber())) {
                subLog.addPull(p);
            }
        }
        // Add lost combats
        for (final DataNumberPair<String> dnp : this.getLostCombats()) {
            final int turnNumber = dnp.getNumber();
            // Stop the iteration once we are outside the interval
            if (turnNumber > endTurn) {
                break;
            }
            // Add if inside the interval
            if ((turnNumber >= startTurn) && (turnNumber <= endTurn)) {
                subLog.addLostCombat(dnp);
            }
        }
        // Create log summary based on the sub interval
        subLog.createLogSummary();
        return subLog;
    }

    /**
     * @param familiarChange
     *            The familiar change to add.
     */
    public void addFamiliarChange(final FamiliarChange familiarChange) {
        if (familiarChange == null) {
            throw new NullPointerException("Familiar change must not be null.");
        }
        final Integer turnNumber = Integer.valueOf(familiarChange
                .getTurnNumber());
        // Only the last familiar change of a turn should be saved.
        this.familiarChanges.remove(turnNumber);
        // If the new familiar change is to a familiar that was already used
        // before the change, do not add the familiar change, because it would
        // be redundant.
        if (this.familiarChanges.isEmpty()
                || !this.getLastFamiliarChange().getFamiliarName()
                        .equals(familiarChange.getFamiliarName())) {
            this.familiarChanges.put(turnNumber, familiarChange);
        }
    }

    /**
     * Returns a sorted collection of all familiar changes of this ascension
     * log.
     * <p>
     * Note that the given collection and its contents is directly backed by the
     * internal collections of this class. This means that changing elements
     * will in the same way effect the internal collections. To ensure that the
     * underlying collection is not corrupted, the returned collection is
     * read-only.
     *
     * @return The familiar changes.
     */
    public Collection<FamiliarChange> getFamiliarChanges() {
        return Collections
                .unmodifiableCollection(this.familiarChanges.values());
    }

    /**
     * @return The last familiar change of this ascension. Returns {@code null}
     *         if there are no familiar changes.
     */
    public FamiliarChange getLastFamiliarChange() {
        return this.familiarChanges.isEmpty() ? null : this.familiarChanges
                .get(this.familiarChanges.lastKey());
    }

    /**
     * @param turn
     *            The turn number before which the last familiar change should
     *            be returned of.
     * @return The last familiar change before the given turn of this ascension.
     *         Returns {@code null} if there are no such familiar changes.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public FamiliarChange getLastFamiliarChangeBeforeTurn(final int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        return LogDataHolder.getLastElementBeforeInteger(this.familiarChanges,
                Integer.valueOf(turn));
    }

    /**
     * @param turn
     *            The turn number after which the first familiar change should
     *            be returned of.
     * @return The first familiar change on or after the given turn of this
     *         ascension. Returns {@code null} if there are no such familiar
     *         changes.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public FamiliarChange getFirstFamiliarChangeAfterTurn(final int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        return LogDataHolder.getFirstElementAfterInteger(this.familiarChanges,
                Integer.valueOf(turn));
    }

    /**
     * @param dayChange
     *            The day change to add.
     */
    public void addDayChange(final DayChange dayChange) {
        if (dayChange == null) {
            throw new NullPointerException("Day change must not be null.");
        }
        this.dayChanges.put(dayChange.getDayNumber(), dayChange);
    }

    /**
     * Returns a sorted collection of all day changes of this ascension log.
     * <p>
     * Note that the given collection and its contents is directly backed by the
     * internal collections of this class. This means that changing elements
     * will in the same way effect the internal collections. To ensure that the
     * underlying collection is not corrupted, the returned collection is
     * read-only.
     *
     * @return The day changes.
     */
    public Collection<DayChange> getDayChanges() {
        return Collections.unmodifiableCollection(this.dayChanges.values());
    }

    /**
     * @return The last day change of this ascension. Returns {@code null} if
     *         there are no day changes.
     */
    public DayChange getLastDayChange() {
        return this.dayChanges.isEmpty() ? null : this.dayChanges
                .get(this.dayChanges.lastKey());
    }

    /**
     * @param turnNumber
     *            The turn number specifying the point of which the day is
     *            wanted.
     * @return The day of the given turn number.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public DayChange getCurrentDay(final int turnNumber) {
        if (turnNumber < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        // Initialise with day 1, because it is always present.
        DayChange currentDay = this.dayChanges.get(Integer.valueOf(1));
        for (final DayChange day : this.getDayChanges()) {
            // If the turn number of the day change is higher than the specified
            // turn number, stop the loop.
            if (day.getTurnNumber() > turnNumber) {
                break;
            }
            // As long as loop isn't stopped, the checked day change happened
            // before the given turn number.
            currentDay = day;
        }
        return currentDay;
    }

    /**
     * @param level
     *            The level data to add.
     */
    public void addLevel(final LevelData level) {
        if (level == null) {
            throw new NullPointerException("Level must not be null.");
        }
        this.levels.put(level.getLevelNumber(), level);
    }

    /**
     * Returns a sorted collection of all level data of this ascension log.
     * <p>
     * Note that the given collection and its contents is directly backed by the
     * internal collections of this class. This means that changing elements
     * will in the same way effect the internal collections. To ensure that the
     * underlying collection is not corrupted, the returned collection is
     * read-only.
     *
     * @return The level data.
     */
    public Collection<LevelData> getLevels() {
        return Collections.unmodifiableCollection(this.levels.values());
    }

    /**
     * @return The last level reached of this ascension. Returns {@code null} if
     *         there are no levels reached.
     */
    public LevelData getLastLevel() {
        return this.levels.isEmpty() ? null : this.levels.get(this.levels
                .lastKey());
    }

    /**
     * @param turnNumber
     *            The turn number specifying the point of which the level is
     *            wanted.
     * @return The level reached at the given turn number.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public LevelData getCurrentLevel(final int turnNumber) {
        if (turnNumber < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        // Initialise with level 1, because it is always present.
        LevelData currentLevel = this.levels.get(Integer.valueOf(1));
        for (final LevelData level : this.getLevels()) {
            // If the turn number of the level change is higher than the
            // specified turn number, stop the loop.
            if (level.getLevelReachedOnTurn() > turnNumber) {
                break;
            }
            // As long as loop isn't stopped, the checked level change happened
            // before the given turn number.
            currentLevel = level;
        }
        return currentLevel;
    }

    /**
     * @param playerSnapshot
     *            The player snapshot to add.
     */
    public void addPlayerSnapshot(final PlayerSnapshot playerSnapshot) {
        if (playerSnapshot == null) {
            throw new NullPointerException("Player snapshot must not be null.");
        }
        // Add the player snapshot.
        this.playerSnapshots
                .put(Integer.valueOf(playerSnapshot.getTurnNumber()),
                        playerSnapshot);
    }

    /**
     * Returns a sorted collection of all player snapshots of this ascension
     * log.
     * <p>
     * Note that the given collection and its contents is directly backed by the
     * internal collections of this class. This means that changing elements
     * will in the same way effect the internal collections. To ensure that the
     * underlying collection is not corrupted, the returned collection is
     * read-only.
     *
     * @return The player snapshots.
     */
    public Collection<PlayerSnapshot> getPlayerSnapshots() {
        return Collections
                .unmodifiableCollection(this.playerSnapshots.values());
    }

    /**
     * @return The last player snapshot of this ascension. Returns {@code null}
     *         if there are no player snapshots.
     */
    public PlayerSnapshot getLastPlayerSnapshot() {
        return this.playerSnapshots.isEmpty() ? null : this.playerSnapshots
                .get(this.playerSnapshots.lastKey());
    }

    /**
     * @param turn
     *            The turn number before which the last player snapshot should
     *            be returned of.
     * @return The last player snapshot before the given turn of this ascension.
     *         Returns {@code null} if there are no such player snapshots.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public PlayerSnapshot getLastPlayerSnapshotBeforeTurn(final int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        return LogDataHolder.getLastElementBeforeInteger(this.playerSnapshots,
                Integer.valueOf(turn));
    }

    /**
     * @param turn
     *            The turn number after which the first player snapshot should
     *            be returned of.
     * @return The first player snapshot on or after the given turn of this
     *         ascension. Returns {@code null} if there are no such player
     *         snapshots.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public PlayerSnapshot getFirstPlayerSnapshotAfterTurn(final int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        return LogDataHolder.getFirstElementAfterInteger(this.playerSnapshots,
                Integer.valueOf(turn));
    }

    /**
     * @param equipmentChange
     *            The equipment change to add.
     */
    public void addEquipmentChange(final EquipmentChange equipmentChange) {
        if (equipmentChange == null) {
            throw new NullPointerException("Equipment change must not be null.");
        }
        // Add the equipment change.
        this.equipmentChanges.put(
                Integer.valueOf(equipmentChange.getTurnNumber()),
                equipmentChange);
    }

    /**
     * Returns a sorted collection of all equipment changes of this ascension
     * log.
     * <p>
     * Note that the given collection and its contents is directly backed by the
     * internal collections of this class. This means that changing elements
     * will in the same way effect the internal collections. To ensure that the
     * underlying collection is not corrupted, the returned collection is
     * read-only.
     *
     * @return The equipment changes.
     */
    public Collection<EquipmentChange> getEquipmentChanges() {
        return Collections.unmodifiableCollection(this.equipmentChanges
                .values());
    }

    /**
     * @return The last equipment change of this ascension. Returns {@code null}
     *         if there are no equipment changes.
     */
    public EquipmentChange getLastEquipmentChange() {
        return this.equipmentChanges.isEmpty() ? null : this.equipmentChanges
                .get(this.equipmentChanges.lastKey());
    }

    /**
     * @param turn
     *            The turn number before which the last equipment change should
     *            be returned of.
     * @return The last equipment change before the given turn of this
     *         ascension. Returns {@code null} if there are no such equipment
     *         changes.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public EquipmentChange getLastEquipmentChangeBeforeTurn(final int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        return LogDataHolder.getLastElementBeforeInteger(this.equipmentChanges,
                Integer.valueOf(turn));
    }

    /**
     * @param turn
     *            The turn number after which the first equipment change should
     *            be returned of.
     * @return The first equipment change on or after the given turn of this
     *         ascension. Returns {@code null} if there are no such equipment
     *         changes.
     * @throws IllegalArgumentException
     *             if turn is negative.
     */
    public EquipmentChange getFirstEquipmentChangeAfterTurn(final int turn) {
        if (turn < 0) {
            throw new IllegalArgumentException(
                    "Turn number cannot be negative.");
        }
        return LogDataHolder.getFirstElementAfterInteger(this.equipmentChanges,
                Integer.valueOf(turn));
    }

    /**
     * @param map
     *            The sorted map in which the element should be looked for.
     * @param number
     *            The number before which the last element in the map should be
     *            returned of.
     * @return The last element before the given number. Returns {@code null} if
     *         there is no such element.
     */
    private static <V> V getLastElementBeforeInteger(
            final SortedMap<Integer, V> map, final Integer number) {
        final SortedMap<Integer, V> headMap = map.headMap(number);
        return headMap.isEmpty() ? null : headMap.get(headMap.lastKey());
    }

    /**
     * @param map
     *            The sorted map in which the element should be looked for.
     * @param number
     *            The number on or after which the last element in the map
     *            should be returned of.
     * @return The last element before the given number. Returns {@code null} if
     *         there is no such element.
     */
    private static <V> V getFirstElementAfterInteger(
            final SortedMap<Integer, V> map, final Integer number) {
        final SortedMap<Integer, V> tailMap = map.tailMap(number);
        return tailMap.isEmpty() ? null : tailMap.get(tailMap.firstKey());
    }

    /**
     * @param pull
     *            The pull to add.
     */
    public void addPull(final Pull pull) {
        if (pull == null) {
            throw new NullPointerException("Pull must not be null.");
        }
        this.pulls.add(pull);
    }

    /**
     * Returns the pull list. This list is empty if no pulls were made.
     * <p>
     * Note that while this list usually is sorted after the turn numbers of the
     * pulls, this cannot be guaranteed.
     * <p>
     * Also, please note that the given list and its contents is directly backed
     * by the internal collections of this class. This means that changing
     * elements will in the same way effect the internal collections. Therefore,
     * great care should be taken when working with this collection. To ensure
     * that the underlying collection is not corrupted through remove or add
     * operations, the returned collection is read-only.
     *
     * @return The pulls.
     */
    public List<Pull> getPulls() {
        return Collections.unmodifiableList(this.pulls);
    }

    /**
     * @param lostCombat
     *            The lost combat to add.
     */
    public void addLostCombat(final DataNumberPair<String> lostCombat) {
        if (lostCombat == null) {
            throw new NullPointerException("Lost combat must not be null.");
        }
        this.lostCombats.add(lostCombat);
    }

    /**
     * Returns a list of all lost combats. This list is empty if no combats were
     * lost.
     * <p>
     * Note that while this list usually is sorted after the turn numbers of the
     * combats, this cannot be guaranteed.
     * <p>
     * Also, please note that the given list is read-only.
     *
     * @return The lost combats.
     */
    public List<DataNumberPair<String>> getLostCombats() {
        return Collections.unmodifiableList(this.lostCombats);
    }

    /**
     * Returns a list of all dropped items during this ascension. The list is
     * not sorted.
     * <p>
     * Note that this a convenience method. It is equal to
     * {@code logData.getLogSummary().getDroppedItems()}.
     *
     * @return A list of all dropped items during this ascension.
     */
    public List<Item> getDroppedItems() {
        return this.logSummary.getDroppedItems();
    }

    /**
     * Returns a list of all skills cast during this ascension.
     * <p>
     * Note that this a convenience method. It is equal to
     * {@code logData.getLogSummary().getSkillsCast()}.
     *
     * @return A list of all skills cast during this ascension.
     */
    public List<Skill> getAllSkillsCast() {
        return this.logSummary.getSkillsCast();
    }

    /**
     * Returns a list of all consumables used during this ascension.
     * <p>
     * Note that this a convenience method. It is equal to
     * {@code logData.getLogSummary().getAllConsumablesUsed()}.
     *
     * @return A list of all consumables used during this ascension.
     */
    public List<Consumable> getAllConsumablesUsed() {
        return this.logSummary.getAllConsumablesUsed();
    }

    /**
     * This method will set the character class of this ascension log based on
     * the given string. If the string doesn't match any of the character class
     * names, the character class of this ascension log will be set to
     * {@code NOT_DEFINED}.
     *
     * @param characterClassName
     *            The name of the character class to set.
     */
    public void setCharacterClass(final String characterClassName) {
        this.characterClass = CharacterClass.fromString(characterClassName);
    }

    /**
     * @return The character class of this ascension log. If no character class
     *         has been specified this method will return {@code NOT_DEFINED}.
     */
    public CharacterClass getCharacterClass() {
        return this.characterClass;
    }

    /**
     * @param parsedLogCreator
     *            The program which created the parsed ascension log behind this
     *            LogDataHolder to set.
     */
    public void setParsedLogCreator(final ParsedLogClass parsedLogCreator) {
        if (parsedLogCreator == null) {
            throw new NullPointerException(
                    "The parsed log creator must not be null.");
        }
        this.parsedLogCreator = parsedLogCreator;
    }

    /**
     * If this LogDataHolder hasn't been created by a parsed ascension log or
     * the log creator hasn't been set (most probably because it couldn't be
     * determined), this method should return {@code NOT_DEFINED}.
     *
     * @return The program which created the parsed ascension log behind this
     *         LogDataHolder.
     */
    public ParsedLogClass getParsedLogCreator() {
        return this.parsedLogCreator;
    }

    /**
     * This enumeration represents all six character classes.
     */
    public static enum CharacterClass {
        SEAL_CLUBBER("Seal Clubber", StatClass.MUSCLE), TURTLE_TAMER(
                "Turtle Tamer", StatClass.MUSCLE), PASTAMANCER("Pastamancer",
                StatClass.MYSTICALITY), SAUCEROR("Sauceror",
                StatClass.MYSTICALITY), DISCO_BANDIT("Disco Bandit",
                StatClass.MOXIE), ACCORDION_THIEF("Accordion Thief",
                StatClass.MOXIE), NOT_DEFINED("not defined", StatClass.MUSCLE);
        private static final Map<String, CharacterClass> stringToEnum = new HashMap<>();
        static {
            for (final CharacterClass op : CharacterClass.values()) {
                CharacterClass.stringToEnum.put(op.toString(), op);
            }
        }
        private final String className;
        private final StatClass statClass;

        CharacterClass(final String className, final StatClass statClass) {
            this.className = className;
            this.statClass = statClass;
        }

        /**
         * @return The mainstat of this character class.
         */
        public StatClass getStatClass() {
            return this.statClass;
        }

        @Override
        public String toString() {
            return this.className;
        }

        /**
         * @return The enum whose toString method returns a string which is
         *         equal to the given string. If no match is found this method
         *         will return {@code NOT_DEFINED}.
         */
        public static CharacterClass fromString(final String className) {
            if (className == null) {
                throw new NullPointerException("Class name must not be null.");
            }
            final CharacterClass characterClass = CharacterClass.stringToEnum
                    .get(className);
            return characterClass != null ? characterClass : NOT_DEFINED;
        }
    }

    /**
     * This enumeration represents the three stat classes.
     */
    public static enum StatClass {
        MUSCLE, MYSTICALITY, MOXIE;
    }

    /**
     * This enumeration represents the different parsers which could have
     * created a parsed ascension log.
     */
    public static enum ParsedLogClass {
        LOG_VISUALIZER, AFH_PARSER, NOT_DEFINED;
    }
}
