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
package com.googlecode.logVisualizer.logData.turn;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.MeatGain;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * An implementation for a turn interval. It can either consist of single turns
 * or simply a specified start and end turn. Note that the start turn is the
 * turn number of the last turn <b>before</b> this turn interval.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class TurnInterval extends AbstractTurn implements
        Comparable<TurnInterval> {
    private int startTurn;
    private int endTurn;
    private SortedSet<SingleTurn> turns = new TreeSet<>();
    private int successfulFreeRunaways;
    private String notes = UsefulPatterns.EMPTY_STRING;

    /**
     * If you use this constructor, please adhere to the standard set by
     * {@link #getStartTurn()} for the start turn number, namely that it is the
     * number of the last turn <b>before</b> this turn interval started.
     * <p>
     * Note that if the value of endTurn is smaller than the value of startTurn,
     * the ending turn of this interval will be set to {@code startTurn}.
     *
     * @param areaName
     *            The name of the area of this turn interval to set.
     * @param startTurn
     *            The start of this turn interval to set.
     * @param endTurn
     *            The end of this turn interval to set.
     * @throws IllegalArgumentException
     *             if startTurn is below 0; if endTurn is below 0
     */
    public TurnInterval(final String areaName, final int startTurn,
            final int endTurn) {
        super(areaName);
        if ((startTurn < 0) || (endTurn < 0)) {
            throw new IllegalArgumentException("Turn range below 0.");
        }
        this.startTurn = startTurn;
        this.endTurn = endTurn >= startTurn ? endTurn : startTurn;
    }

    /**
     * Constructs a turn interval with the given turn as a part of it.
     *
     * @param turn
     *            Starting point of this turn interval to set.
     */
    public TurnInterval(final SingleTurn turn) {
        super(turn.getAreaName());
        final int tmpStartTurn = turn.getTurnNumber() - 1;
        this.startTurn = tmpStartTurn < 0 ? 0 : tmpStartTurn;
        this.endTurn = turn.getTurnNumber();
        this.addTurnData(turn);
        this.turns.add(turn);
    }

    /**
     * Constructs a turn interval with the given turns as its content.
     *
     * @param turns
     *            The single turns of this turn interval to set.
     * @throws IllegalArgumentException
     *             if turns is empty; if areaName is not equal to the area name
     *             of the turns inside the collection
     */
    public TurnInterval(final SortedSet<SingleTurn> turns, final String areaName) {
        super(areaName);
        if (turns.isEmpty()) {
            throw new IllegalArgumentException(
                    "Turns collection must not be empty.");
        }
        final Iterator<SingleTurn> index = turns.iterator();
        SingleTurn turn = index.next();
        if (!turn.getAreaName().equals(areaName)) {
            throw new IllegalArgumentException(
                    "Area name parameter and area name of the turns inside the collcetion must be equal.");
        }
        this.addTurnData(turn);
        this.turns.add(turn);
        this.setStartEndInterval();
        while (index.hasNext()) {
            turn = index.next();
            if (!turn.getAreaName().equals(areaName)) {
                throw new IllegalArgumentException(
                        "Area name parameter and area name of the turns inside the collcetion must be equal.");
            }
            this.addTurn(turn);
        }
    }

    /**
     * Helper method will set the start and end turn of this interval correctly.
     * Note that this method only works if the single turn collection is not
     * empty.
     */
    private void setStartEndInterval() {
        this.startTurn = this.turns.first().getTurnNumber() - 1;
        this.endTurn = this.turns.last().getTurnNumber();
    }

    /**
     * Checks the given turn on whether a runaway was done with the Navel Ring
     * equipped and increments the Navel Ring usage summary.
     */
    private int countUnsuccessfulNavelRingUsages() {
        int tmp = 0;
        for (final SingleTurn turn : this.turns) {
            if (turn.isRanAwayOnThisTurn() && turn.isNavelRingEquipped()) {
                tmp++;
            }
        }
        return tmp;
    }

    /**
     * @param turns
     *            The turns whose data will be added to this turn interval and
     *            the last single turn of this interval if one is present.
     */
    public void addTurnIntervalData(final TurnInterval turns) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addTurnData(turns);
        }
        this.addTurnData(turns);
        this.addNotes(turns.getNotes());
        this.successfulFreeRunaways += turns.getFreeRunaways()
                .getNumberOfSuccessfulRunaways();
    }

    /**
     * Simply an alias for {@link #addStatGain(int, int, int)}.
     */
    public void addStats(final int mus, final int myst, final int mox) {
        this.addStatGain(mus, myst, mox);
    }

    /**
     * These statgains will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param mus
     *            The muscle stat gain to add.
     * @param myst
     *            The mysticality stat gain to add.
     * @param mox
     *            The moxie stat gain to add.
     */
    @Override
    public void addStatGain(final int mus, final int myst, final int mox) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addStatGain(mus, myst, mox);
        }
        super.addStatGain(mus, myst, mox);
    }

    /**
     * These mp gains will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param encounterMPGain
     *            The encounter mp gain to add. This should not include starfish
     *            mp gains or mp gains from resting.
     */
    public void addEncounterMPGain(final int encounterMPGain) {
        if (!this.turns.isEmpty()) {
            this.turns.last().getMPGain().addEncounterMPGain(encounterMPGain);
        }
        this.getMPGain().addEncounterMPGain(encounterMPGain);
    }

    /**
     * These mp gains will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param starfishMPGain
     *            The starfish mp gain to add.
     */
    public void addStarfishMPGain(final int starfishMPGain) {
        if (!this.turns.isEmpty()) {
            this.turns.last().getMPGain().addStarfishMPGain(starfishMPGain);
        }
        this.getMPGain().addStarfishMPGain(starfishMPGain);
    }

    /**
     * These mp gains will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param restingMPGain
     *            The resting mp gain to add.
     */
    public void addRestingMPGain(final int restingMPGain) {
        if (!this.turns.isEmpty()) {
            this.turns.last().getMPGain().addRestingMPGain(restingMPGain);
        }
        this.getMPGain().addRestingMPGain(restingMPGain);
    }

    /**
     * These mp gains will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param outOfEncounterMPGain
     *            The out-of-encounter mp gain to add.
     */
    public void addOutOfEncounterMPGain(final int outOfEncounterMPGain) {
        if (!this.turns.isEmpty()) {
            this.turns.last().getMPGain()
                    .addOutOfEncounterMPGain(outOfEncounterMPGain);
        }
        this.getMPGain().addOutOfEncounterMPGain(outOfEncounterMPGain);
    }

    /**
     * These mp gains will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param consumableMPGain
     *            The consumable mp gain to add.
     */
    public void addConsumableMPGain(final int consumableMPGain) {
        if (!this.turns.isEmpty()) {
            this.turns.last().getMPGain().addConsumableMPGain(consumableMPGain);
        }
        this.getMPGain().addConsumableMPGain(consumableMPGain);
    }

    /**
     * The meat data will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param meat
     *            The meat data to add.
     */
    @Override
    public void addMeat(final MeatGain meat) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addMeat(meat);
        }
        super.addMeat(meat);
    }

    /**
     * The meat gained will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param encounterMeatGain
     *            The meat gain from inside the encounter to add.
     */
    @Override
    public void addEncounterMeatGain(final int encounterMeatGain) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addEncounterMeatGain(encounterMeatGain);
        }
        super.addEncounterMeatGain(encounterMeatGain);
    }

    /**
     * The meat gained will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param otherMeatGain
     *            The meat gain from outside the encounter to add.
     */
    @Override
    public void addOtherMeatGain(final int otherMeatGain) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addOtherMeatGain(otherMeatGain);
        }
        super.addOtherMeatGain(otherMeatGain);
    }

    /**
     * The meat spent will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param meatSpent
     *            The meat spent to add.
     */
    @Override
    public void addMeatSpent(final int meatSpent) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addMeatSpent(meatSpent);
        }
        super.addMeatSpent(meatSpent);
    }

    /**
     * The item will be added to this turn interval and the last single turn of
     * this interval, if one is present.
     *
     * @param droppedItem
     *            The item to add.
     */
    @Override
    public void addDroppedItem(final Item droppedItem) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addDroppedItem(droppedItem);
        }
        super.addDroppedItem(droppedItem);
    }

    /**
     * The skill will be added to this turn interval and the last single turn of
     * this interval, if one is present.
     *
     * @param skill
     *            The skill to add.
     */
    @Override
    public void addSkillCast(final Skill skill) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addSkillCast(skill);
        }
        super.addSkillCast(skill);
    }

    /**
     * The consumable will be added to this turn interval and the last single
     * turn of this interval, if one is present.
     *
     * @param consumable
     *            The consumable to add.
     */
    @Override
    public void addConsumableUsed(final Consumable consumable) {
        if (!this.turns.isEmpty()) {
            this.turns.last().addConsumableUsed(consumable);
        }
        super.addConsumableUsed(consumable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addTurnData(final AbstractTurn turn) {
        if (turn == null) {
            throw new NullPointerException("Turn must not be null.");
        }
        final Statgain turnStats = turn.getStatGain();
        super.addMeat(turn.getMeat());
        super.addStatGain(turnStats.mus, turnStats.myst, turnStats.mox);
        this.getMPGain().addMPGains(turn.getMPGain());
        for (final Item i : turn.getDroppedItems()) {
            super.addDroppedItem(i);
        }
        for (final Skill s : turn.getSkillsCast()) {
            super.addSkillCast(s);
        }
        for (final Consumable c : turn.getConsumablesUsed()) {
            super.addConsumableUsed(c);
        }
    }

    /**
     * Will return the turn number of the last turn before this turn interval
     * starts. Thus, this method is equal to
     * {@code getTurns().get(0).getTurnNumber-1}. The reasoning behind this is
     * that a turn interval consisting of one single turn is the interval from
     * {@code turn.getTurnNumber()-1} to {@code turn.getTurnNumber()}.
     *
     * @return The start of this turn interval.
     */
    public int getStartTurn() {
        return this.startTurn;
    }

    /**
     * @return The last turn of this turn interval.
     */
    public int getEndTurn() {
        return this.endTurn;
    }

    /**
     * @return The amount of turns in this turn interval.
     */
    public int getTotalTurns() {
        return this.endTurn - this.startTurn;
    }

    /**
     * Adds the given turn to this turn interval.
     * <p>
     * In case there is already a turn present with the same turn number, that
     * turn will replaced with the given turn and the turn data of the replaced
     * turn will be added to the turn before it or the given turn if the
     * replaced turn is the first in the interval.
     *
     * @param turn
     *            The turn to add.
     * @throws IllegalStateException
     *             if this class wasn't initialised with single turns (which is
     *             equal to a call to getTurns().isEmpty() returning true)
     * @throws IllegalArgumentException
     *             if area name of the turn interval is not equal to the area
     *             name of the given turn
     */
    public void addTurn(final SingleTurn turn) {
        if (this.turns.isEmpty()) {
            throw new IllegalStateException(
                    "Adding single turns to an interval that wasn't initialised with single turns isn't supported.");
        }
        if (!this.getAreaName().equals(turn.getAreaName())) {
            throw new IllegalArgumentException(
                    "Area name of the turn interval is not the same as the area name of the given turn.");
        }
        this.addTurnData(turn);
        // If there is only one turn present in the interval, check whether
        // there is a big turn number difference between the turn that's
        // supposed to be added and the turn in the interval. If that is the
        // case, treat it as an error of a mafia log. Sometimes after
        // ascensions, mafia doesn't check the turn counts and uses wrong
        // numbers for the first few turns. Not all these errors will be catched
        // by this check, but at least some should be.
        if (this.turns.size() == 1) {
            final SingleTurn onlyTurn = this.turns.first();
            if ((turn.getTurnNumber() - onlyTurn.getTurnNumber()) < -2) {
                final SingleTurn tmp = new SingleTurn(onlyTurn.getAreaName(),
                        onlyTurn.getEncounterName(), turn.getTurnNumber() - 1,
                        onlyTurn.getUsedEquipment(), onlyTurn.getUsedFamiliar());
                tmp.addSingleTurnData(onlyTurn);
                this.turns.remove(onlyTurn);
                this.turns.add(tmp);
            }
        }
        if (this.turns.contains(turn)) {
            // Add all the data from the turn which should be removed to the
            // turn that comes before it, or if there is none, to the new turn
            // so the data is not lost.
            final SortedSet<SingleTurn> tmp = this.turns.headSet(turn);
            final SingleTurn equalTurn = this.turns.tailSet(turn).first();
            if (!tmp.isEmpty()) {
                tmp.last().addSingleTurnData(equalTurn);
            } else {
                turn.addSingleTurnData(equalTurn);
            }
            // If the skipped turn was a runaway and the Navel Ring was
            // equipped, it means that it was a successful usage of the Navel
            // Ring.
            if (equalTurn.isRanAwayOnThisTurn()
                    && equalTurn.isNavelRingEquipped()) {
                this.successfulFreeRunaways++;
            }
            this.turns.remove(equalTurn);
        }
        this.turns.add(turn);
        this.setStartEndInterval();
    }

    /**
     * Removes the first turn of this interval and adds all the data of the turn
     * to the following one. If no other turn exists, the data will simply be
     * lost.
     *
     * @return The removed turn.
     * @throws IllegalStateException
     *             if this class wasn't initialised with single turns (which is
     *             equal to a call to getTurns().isEmpty() returning true)
     */
    public SingleTurn removeFirstTurn() {
        if (this.turns.isEmpty()) {
            throw new IllegalStateException(
                    "Removing single turns from an interval that wasn't initialised with single turns isn't supported.");
        }
        final SingleTurn turn = this.turns.first();
        this.turns.remove(turn);
        if (!this.turns.isEmpty()) {
            this.turns.first().addSingleTurnData(turn);
            this.setStartEndInterval();
        }
        return turn;
    }

    /**
     * Removes the last turn of this interval and adds all the data of the turn
     * to the one coming before it. If no other turn exists, the data will
     * simply be lost.
     *
     * @return The removed turn.
     * @throws IllegalStateException
     *             if this class wasn't initialised with single turns (which is
     *             equal to a call to getTurns().isEmpty() returning true)
     */
    public SingleTurn removeLastTurn() {
        if (this.turns.isEmpty()) {
            throw new IllegalStateException(
                    "Removing single turns from an interval that wasn't initialised with single turns isn't supported.");
        }
        final SingleTurn turn = this.turns.last();
        this.turns.remove(turn);
        if (!this.turns.isEmpty()) {
            this.turns.last().addSingleTurnData(turn);
            this.setStartEndInterval();
        }
        return turn;
    }

    /**
     * @param turns
     *            The single turns to set.
     * @throws IllegalArgumentException
     *             if turns is empty
     */
    public void setTurns(final Collection<SingleTurn> turns) {
        if (turns.isEmpty()) {
            throw new IllegalArgumentException(
                    "Turn collection must not be empty.");
        }
        this.turns = new TreeSet<>(turns);
        this.setStartEndInterval();
        this.clearAllTurnDataCollections();
        this.setStatGain(0, 0, 0);
        this.setMeat(new MeatGain());
        for (final SingleTurn turn : turns) {
            this.addTurnData(turn);
        }
    }

    /**
     * Returns the collection of single turns spent during this turn interval.
     * Please note that the set returned by this method is directly backed by
     * the internal collection of this class and thus, great care should be
     * taken when handling its elements.
     *
     * @return The single turns of this turn interval. Can be empty, if no turns
     *         have been set.
     */
    public SortedSet<SingleTurn> getTurns() {
        return this.turns;
    }

    /**
     * @param successfulNavelRingUsages
     *            The number of successful Navel Ring usages to set.
     */
    public void setSuccessfulFreeRunaways(final int successfulFreeRunaways) {
        this.successfulFreeRunaways = successfulFreeRunaways;
    }

    /**
     * @param successfulNavelRingUsages
     *            The number of successful Navel Ring usages to increment the
     *            already existing value by..
     */
    public void incrementSuccessfulFreeRunaways(final int successfulFreeRunaways) {
        this.successfulFreeRunaways += successfulFreeRunaways;
    }

    /**
     * @return The Navel Ring usage of this turn interval.
     */
    public FreeRunaways getFreeRunaways() {
        return new FreeRunaways(this.successfulFreeRunaways
                + this.countUnsuccessfulNavelRingUsages(),
                this.successfulFreeRunaways);
    }

    /**
     * @param notes
     *            The notes tagged to this turn interval to set.
     */
    public void setNotes(final String notes) {
        if (notes == null) {
            throw new NullPointerException("notes must not be null.");
        }
        this.notes = notes;
    }

    /**
     * Adds the given notes to this turn interval. The already existing notes
     * and the ones added will be divided by a line break ({@code"\n"}).
     *
     * @param notes
     *            The notes tagged to this turn interval to set.
     */
    public void addNotes(final String notes) {
        if (notes == null) {
            throw new NullPointerException("notes must not be null.");
        }
        if (notes.length() > 0) {
            final StringBuilder str = new StringBuilder(this.notes.length()
                    + notes.length() + 1);
            if (this.notes.length() > 0) {
                str.append(this.notes);
                str.append("\n");
            }
            str.append(notes);
            this.notes = str.toString();
        }
    }

    /**
     * @return The notes tagged to this turn interval.
     */
    public String getNotes() {
        return this.notes;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(60);
        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        if (this.getTotalTurns() > 1) {
            str.append(this.startTurn + 1);
            str.append(UsefulPatterns.MINUS);
        }
        str.append(this.endTurn);
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getAreaName());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getStatGain().toString());
        return str.toString();
    }

    /**
     * @return The difference between the start turns of this turn interval and
     *         the given turn interval.
     */
    @Override
    public int compareTo(final TurnInterval turns) {
        return this.startTurn - turns.getStartTurn();
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof TurnInterval) {
                final boolean isSameTurnRange = (this.startTurn == ((TurnInterval) o)
                        .getStartTurn())
                        && (this.endTurn == ((TurnInterval) o).getEndTurn());
                return isSameTurnRange && super.equals(o)
                        && this.turns.equals(((TurnInterval) o).getTurns());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 11;
        result = (31 * result) + this.startTurn;
        result = (31 * result) + this.endTurn;
        result = (31 * result) + super.hashCode();
        result = (31 * result) + this.turns.hashCode();
        return result;
    }

    /**
     * This immutable class is a representation of the runaway usage of the
     * Navel Ring.
     */
    public static final class FreeRunaways {
        private static final String SLASH = "/";
        private static final String FREE_RETREATS_STRING = "free retreats";
        private final int numberOfAttemptedRunaways;
        private final int numberOfSuccessfulRunaways;

        /**
         * Creates a new FreeRunaways instance with the given number of runaways
         * and successful runaways.
         *
         * @throws IllegalArgumentException
         *             if numberOfSuccessfulUsages is greater than
         *             numberOfAttemptedUsages
         */
        public FreeRunaways(final int numberOfAttemptedRunaways,
                final int numberOfSuccessfulRunaways) {
            if (numberOfSuccessfulRunaways > numberOfAttemptedRunaways) {
                throw new IllegalArgumentException(
                        "Number of successful usages cannot be below number of usages.");
            }
            this.numberOfAttemptedRunaways = numberOfAttemptedRunaways;
            this.numberOfSuccessfulRunaways = numberOfSuccessfulRunaways;
        }

        /**
         * @return The number of attempted runaway usages of the Navel Ring.
         */
        public int getNumberOfAttemptedRunaways() {
            return this.numberOfAttemptedRunaways;
        }

        /**
         * @return The number of successful runaway usages of the Navel Ring.
         */
        public int getNumberOfSuccessfulRunaways() {
            return this.numberOfSuccessfulRunaways;
        }

        @Override
        public String toString() {
            final StringBuilder str = new StringBuilder(25);
            str.append(this.numberOfSuccessfulRunaways);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(FreeRunaways.SLASH);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(this.numberOfAttemptedRunaways);
            str.append(UsefulPatterns.WHITE_SPACE);
            str.append(FreeRunaways.FREE_RETREATS_STRING);
            return str.toString();
        }

        @Override
        public boolean equals(final Object o) {
            if (super.equals(o) && (o instanceof FreeRunaways)) {
                return ((FreeRunaways) o).getNumberOfSuccessfulRunaways() == this.numberOfSuccessfulRunaways;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 687;
            result = (31 * result) + super.hashCode();
            result = (31 * result) + this.numberOfSuccessfulRunaways;
            return result;
        }
    }
}
