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

import com.googlecode.logVisualizer.logData.Item;
import com.googlecode.logVisualizer.logData.MPGain;
import com.googlecode.logVisualizer.logData.MeatGain;
import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.Statgain;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.util.Countable;
import com.googlecode.logVisualizer.util.CountableSet;

/**
 * This class handles all the data which a turn/turns can collect. An actual
 * implementation would also need handling of turn numbers.
 * <p>
 * Note that all value classes handled by this class, which implement the
 * {@link Countable} interface, need not take special actions to make sure no
 * data corruption happens by sharing instances. The internal data collections
 * of this class will take care of this on their own. However, when an object is
 * added to this class, it should always be expected that it has been cloned in
 * some way.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public abstract class AbstractTurn {
    private final String areaName;
    private MeatGain meat = new MeatGain();
    private final MPGain mpGain = new MPGain();
    private Statgain statGain = new Statgain();
    private final CountableSet<Item> droppedItems = new CountableSet<>();
    private final CountableSet<Skill> skillsCast = new CountableSet<>();
    private final CountableSet<Consumable> consumablesUsed = new CountableSet<>();

    /**
     * @param areaName
     *            The name of the area to set.
     */
    public AbstractTurn(final String areaName) {
        if (areaName == null) {
            throw new NullPointerException("Area name must not be null.");
        }
        this.areaName = areaName;
    }

    /**
     * @return The name of the area this turn was spent in.
     */
    public String getAreaName() {
        return this.areaName;
    }

    /**
     * @param mus
     *            The muscle stats to add.
     * @param myst
     *            The mysticality stats to add.
     * @param mox
     *            The moxie stats to add.
     */
    public void addStatGain(final int mus, final int myst, final int mox) {
        this.statGain = this.statGain.addStats(mus, myst, mox);
    }

    /**
     * @param mus
     *            The muscle stats to set.
     * @param myst
     *            The mysticality stats to set.
     * @param mox
     *            The moxie stats to set.
     */
    public void setStatGain(final int mus, final int myst, final int mox) {
        this.statGain = new Statgain(mus, myst, mox);
    }

    /**
     * @return The stat gain from this turn. This doesn't entail stat gains from
     *         consumables used during this turn.
     */
    public Statgain getStatGain() {
        return this.statGain;
    }

    /**
     * @return The stat gain from this turn including those from consumables.
     */
    public Statgain getTotalStatGain() {
        Statgain totalStatgain = this.statGain;
        for (final Consumable c : this.consumablesUsed.getElements()) {
            totalStatgain = totalStatgain.addStats(c.getStatGain());
        }
        return totalStatgain;
    }

    /**
     * @return The mp gains from this turn.
     */
    public MPGain getMPGain() {
        return this.mpGain;
    }

    /**
     * @param meat
     *            The meat data to set.
     */
    public void setMeat(final MeatGain meat) {
        this.meat = meat;
    }

    /**
     * @param meat
     *            The meat data to add.
     */
    public void addMeat(final MeatGain meat) {
        this.meat = this.meat.addMeatData(meat);
    }

    /**
     * @return The meat data object.
     */
    public MeatGain getMeat() {
        return this.meat;
    }

    /**
     * @param encounterMeatGain
     *            The meat gain from inside the encounter to set.
     */
    public void setEncounterMeatGain(final int encounterMeatGain) {
        this.meat = this.meat.setEncounterMeatGain(encounterMeatGain);
    }

    /**
     * @param encounterMeatGain
     *            The meat gain from inside the encounter to add.
     */
    public void addEncounterMeatGain(final int encounterMeatGain) {
        this.meat = this.meat.addEncounterMeatGain(encounterMeatGain);
    }

    /**
     * @return The meat gained from inside the encounter of this turn.
     */
    public int getEncounterMeatGain() {
        return this.meat.encounterMeatGain;
    }

    /**
     * @param otherMeatGain
     *            The meat gain from outside the encounter to set.
     */
    public void setOtherMeatGain(final int otherMeatGain) {
        this.meat = this.meat.setOtherMeatGain(otherMeatGain);
    }

    /**
     * @param otherMeatGain
     *            The meat gain from outside the encounter to add.
     */
    public void addOtherMeatGain(final int otherMeatGain) {
        this.meat = this.meat.addOtherMeatGain(otherMeatGain);
    }

    /**
     * @return The meat gained from outside the encounter of this turn.
     */
    public int getOtherMeatGain() {
        return this.meat.otherMeatGain;
    }

    /**
     * @param meatSpent
     *            The meat spent to set.
     */
    public void setMeatSpent(final int meatSpent) {
        this.meat = this.meat.setMeatSpent(meatSpent);
    }

    /**
     * @param meatSpent
     *            The meat spent to add.
     */
    public void addMeatSpent(final int meatSpent) {
        this.meat = this.meat.addMeatSpent(meatSpent);
    }

    /**
     * @return The meat spent on this turn.
     */
    public int getMeatSpent() {
        return this.meat.meatSpent;
    }

    /**
     * @param droppedItem
     *            The item to add.
     */
    public void addDroppedItem(final Item droppedItem) {
        this.droppedItems.addElement(droppedItem);
    }

    /**
     * @param droppedItems
     *            The dropped items to set.
     */
    public void setDroppedItems(final Collection<Item> droppedItems) {
        this.droppedItems.setElements(droppedItems);
    }

    /**
     * @return The dropped items from this turn.
     */
    public Collection<Item> getDroppedItems() {
        return this.droppedItems.getElements();
    }

    /**
     * @return {@code true} if the given item has dropped on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Item#getComparator()} and nothing else.
     */
    public boolean isItemDropped(final Item i) {
        return this.droppedItems.contains(i);
    }

    /**
     * @param skill
     *            The skill to add.
     */
    public void addSkillCast(final Skill skill) {
        this.skillsCast.addElement(skill);
    }

    /**
     * @param skillsCast
     *            The skills cast to set.
     */
    public void setSkillsCast(final Collection<Skill> skillsCast) {
        this.skillsCast.setElements(skillsCast);
    }

    /**
     * @return The skills cast this turn.
     */
    public Collection<Skill> getSkillsCast() {
        return this.skillsCast.getElements();
    }

    /**
     * @return {@code true} if the given skill has been cast on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Skill#getComparator()} and nothing else.
     */
    public boolean isSkillCast(final Skill s) {
        return this.skillsCast.contains(s);
    }

    /**
     * @param consumable
     *            The consumable to add.
     */
    public void addConsumableUsed(final Consumable consumable) {
        this.consumablesUsed.addElement(consumable);
    }

    /**
     * @param consumablesUsed
     *            The consumables used to set.
     */
    public void setConsumablesUsed(final Collection<Consumable> consumablesUsed) {
        this.consumablesUsed.setElements(consumablesUsed);
    }

    /**
     * @return The consumables used this turn.
     */
    public Collection<Consumable> getConsumablesUsed() {
        return this.consumablesUsed.getElements();
    }

    /**
     * @return {@code true} if the given consumable has been used on this turn,
     *         otherwise {@code false}. This check is solely based on
     *         {@link Consumable#getComparator()} and nothing else.
     */
    public boolean isConsumableUsed(final Consumable c) {
        return this.consumablesUsed.contains(c);
    }

    /**
     * @param turn
     *            The turn whose data will be added to this turn.
     */
    protected void addTurnData(final AbstractTurn turn) {
        if (turn == null) {
            throw new NullPointerException("Turn must not be null.");
        }
        this.meat = this.getMeat().addMeatData(turn.getMeat());
        this.statGain = this.getStatGain().addStats(turn.getStatGain());
        this.getMPGain().addMPGains(turn.getMPGain());
        for (final Item i : turn.getDroppedItems()) {
            this.addDroppedItem(i);
        }
        for (final Skill s : turn.getSkillsCast()) {
            this.addSkillCast(s);
        }
        for (final Consumable c : turn.getConsumablesUsed()) {
            this.addConsumableUsed(c);
        }
    }

    protected void clearAllTurnDataCollections() {
        this.droppedItems.clear();
        this.skillsCast.clear();
        this.consumablesUsed.clear();
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof AbstractTurn) {
                final AbstractTurn at = (AbstractTurn) o;
                return this.meat.equals(at.getMeat())
                        && this.mpGain.equals(at.getMPGain())
                        && this.statGain.equals(at.getStatGain())
                        && this.areaName.equals(at.getAreaName())
                        && this.droppedItems.getElements().equals(
                                at.getDroppedItems())
                        && this.skillsCast.getElements().equals(
                                at.getSkillsCast())
                        && this.consumablesUsed.getElements().equals(
                                at.getConsumablesUsed());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 48;
        result = (31 * result) + this.meat.hashCode();
        result = (31 * result) + this.mpGain.hashCode();
        result = (31 * result) + this.statGain.hashCode();
        result = (31 * result) + this.areaName.hashCode();
        result = (31 * result) + this.droppedItems.hashCode();
        result = (31 * result) + this.skillsCast.hashCode();
        result = (31 * result) + this.consumablesUsed.hashCode();
        return result;
    }
}
