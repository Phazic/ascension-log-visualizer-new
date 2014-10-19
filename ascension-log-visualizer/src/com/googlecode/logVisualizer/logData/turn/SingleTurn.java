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

import java.util.ArrayList;
import java.util.List;

import com.googlecode.logVisualizer.logData.Skill;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * An implementation of a single turn. It uses the services of the
 * {@link AbstractTurn} class and additionally implements turn number and
 * encounter name handling.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class SingleTurn extends AbstractTurn implements
        Comparable<SingleTurn> {
    private static final String DELIMITER_STRING = "--";
    private static final String NAVEL_RING_OF_NAVEL_GAZING = "navel ring of navel gazing";
    private static final Skill RUN = new Skill("return", 1);
    private final String encounterName;
    private final int turnNumber;
    private final EquipmentChange usedEquipment;
    private final FamiliarChange usedFamiliar;
    private boolean isDisintegrated = false;
    private TurnVersion turnVersion = TurnVersion.NOT_DEFINED;

    /**
     * @param areaName
     *            The name of the area of this turn to set.
     * @param encounterName
     *            The name of the encounter of this turn to set.
     * @param turnNumber
     *            The turn number of this turn to set.
     * @param usedEquipment
     *            The equipment used on this turn to set.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     */
    public SingleTurn(final String areaName, final String encounterName,
            final int turnNumber, final EquipmentChange usedEquipment,
            final FamiliarChange usedFamiliar) {
        super(areaName);
        if (encounterName == null) {
            throw new NullPointerException("Encounter name must not be null.");
        }
        if (usedEquipment == null) {
            throw new NullPointerException("The equipment must not be null.");
        }
        if (usedFamiliar == null) {
            throw new NullPointerException("The familiar must not be null.");
        }
        if (turnNumber < 0) {
            throw new IllegalArgumentException("Turn number below 0.");
        }
        this.encounterName = encounterName;
        this.usedFamiliar = usedFamiliar;
        this.turnNumber = turnNumber;
        this.usedEquipment = usedEquipment;
    }

    /**
     * @param turn
     *            The turn whose data will be added to this turn.
     */
    public void addSingleTurnData(final SingleTurn turn) {
        this.addTurnData(turn);
        // Casts of return (aka running away) should not be added, because they
        // mess up Navel Ring usage tracking.
        if (turn.isRanAwayOnThisTurn()) {
            final List<Skill> skills = new ArrayList<>();
            for (final Skill s : this.getSkillsCast()) {
                if (!s.getName().equals(SingleTurn.RUN.getName())) {
                    skills.add(s);
                } else if (s.getAmount() > 1) {
                    final Skill tmp = s.newInstance();
                    tmp.setAmount(tmp.getAmount() - 1);
                    skills.add(tmp);
                }
            }
            this.setSkillsCast(skills);
        }
    }

    /**
     * @return The name of the encounter found on this turn.
     */
    public String getEncounterName() {
        return this.encounterName;
    }

    /**
     * @return The turn number.
     */
    public int getTurnNumber() {
        return this.turnNumber;
    }

    /**
     * @return The familiar used on this turn.
     */
    public FamiliarChange getUsedFamiliar() {
        return this.usedFamiliar;
    }

    /**
     * @return The equipment used on this turn.
     */
    public EquipmentChange getUsedEquipment() {
        return this.usedEquipment;
    }

    /**
     * @return True if a navel ring was equipped on this turn, otherwise false.
     */
    public boolean isNavelRingEquipped() {
        return this.usedEquipment
                .isEquiped(SingleTurn.NAVEL_RING_OF_NAVEL_GAZING);
    }

    /**
     * @return {@code true} if this turn is a combat and was run away from,
     *         otherwise {@code false}.
     */
    public boolean isRanAwayOnThisTurn() {
        return this.turnVersion == TurnVersion.COMBAT ? this
                .isSkillCast(SingleTurn.RUN) : false;
    }

    /**
     * This flag can only be changed to {@code true} if this turn is a combat.
     *
     * @param isDisintegrated
     *            Sets the flag on whether this combat was disintegrated or not.
     */
    public void setDisintegrated(final boolean isDisintegrated) {
        this.isDisintegrated = this.turnVersion == TurnVersion.COMBAT ? isDisintegrated
                : false;
    }

    /**
     * @return True if this combat was disintegrated. Will always return false
     *         if this turn is not a combat.
     */
    public boolean isDisintegrated() {
        return this.turnVersion == TurnVersion.COMBAT ? this.isDisintegrated
                : false;
    }

    /**
     * @param turnVersion
     *            The turn version to set.
     */
    public void setTurnVersion(final TurnVersion turnVersion) {
        if (turnVersion == null) {
            throw new NullPointerException("Turn version must not be null.");
        }
        this.turnVersion = turnVersion;
    }

    /**
     * @return The turn version.
     */
    public TurnVersion getTurnVersion() {
        return this.turnVersion;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(80);
        str.append(UsefulPatterns.SQUARE_BRACKET_OPEN);
        str.append(this.turnNumber);
        str.append(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getAreaName());
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(SingleTurn.DELIMITER_STRING);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.encounterName);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(this.getStatGain().toString());
        return str.toString();
    }

    /**
     * @return The difference between this turns turn number and the turn number
     *         of the given turn.
     */
    @Override
    public int compareTo(final SingleTurn turn) {
        return this.turnNumber - turn.getTurnNumber();
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof SingleTurn) {
                final SingleTurn st = (SingleTurn) o;
                return (this.turnNumber == st.getTurnNumber())
                        && (this.turnVersion == st.getTurnVersion())
                        && this.encounterName.equals(st.getEncounterName())
                        && super.equals(o);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = (31 * result) + this.turnNumber;
        result = (31 * result) + this.turnVersion.hashCode();
        result = (31 * result) + this.encounterName.hashCode();
        result = (31 * result) + super.hashCode();
        return result;
    }

    /**
     * A simple enumeration for various turn types.
     */
    public static enum TurnVersion {
        COMBAT, NONCOMBAT, OTHER, NOT_DEFINED;
    }
}
