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
package com.googlecode.logVisualizer.logData.turn.turnAction;

/**
 * This immutable class is a representation of an equipment change. It holds the
 * turn number of when the change occurred and the names of all the equipment
 * used at that particular turn after the change.
 * <p>
 * If no equipment was worn in a particular slot, that slot will return
 * {@link #NO_EQUIPMENT_STRING}.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class EquipmentChange extends AbstractTurnAction<EquipmentChange> {
    public static final String NO_EQUIPMENT_STRING = "none";
    private final String hat;
    private final String weapon;
    private final String offhand;
    private final String shirt;
    private final String pants;
    private final String acc1;
    private final String acc2;
    private final String acc3;
    private final String famEquip;

    /**
     * Constructs a new equipment change object.
     * <p>
     * The object will be initialised with no equipment being worn.
     *
     * @param turnNumber
     *            The turn number of this equipment change to set.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     */
    public EquipmentChange(final int turnNumber) {
        this(turnNumber, EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING,
                EquipmentChange.NO_EQUIPMENT_STRING);
    }

    /**
     * Constructs a new equipment change object.
     * <p>
     * In case one wants to denote a particular slot as not sporting any
     * equipment, one has to pass {@link #NO_EQUIPMENT_STRING} for the given
     * slot.
     *
     * @param turnNumber
     *            The turn number of this equipment change to set.
     * @param hat
     *            The name of the equipment used in the hat slot.
     * @param weapon
     *            The name of the equipment used in the weapon slot.
     * @param offhand
     *            The name of the equipment used in the offhand slot.
     * @param shirt
     *            The name of the equipment used in the shirt slot.
     * @param pants
     *            The name of the equipment used in the pants slot.
     * @param acc1
     *            The name of the equipment used in the acc1 slot.
     * @param acc2
     *            The name of the equipment used in the acc2 slot.
     * @param acc3
     *            The name of the equipment used in the acc3 slot.
     * @param famEquip
     *            The name of the equipment used in the familiar equipment slot.
     * @throws IllegalArgumentException
     *             if turnNumber is below 0
     * @throws NullPointerException
     *             if one of the equipment names is {@code null}
     */
    public EquipmentChange(final int turnNumber, final String hat,
            final String weapon, final String offhand, final String shirt,
            final String pants, final String acc1, final String acc2,
            final String acc3, final String famEquip) {
        super(turnNumber);
        if ((hat == null) || (weapon == null) || (offhand == null)
                || (shirt == null) || (pants == null) || (acc1 == null)
                || (acc2 == null) || (acc3 == null) || (famEquip == null)) {
            throw new NullPointerException(
                    "No null objects allowed as parameters.");
        }
        this.hat = hat;
        this.weapon = weapon;
        this.offhand = offhand;
        this.shirt = shirt;
        this.pants = pants;
        this.acc1 = acc1;
        this.acc2 = acc2;
        this.acc3 = acc3;
        this.famEquip = famEquip;
    }

    /**
     * @param equipment
     *            The name of the equipment which is looked for.
     * @return {@code true} if one of the equipment slots has equipment with a
     *         name equal to the given one, otherwise {@code false}.
     */
    public boolean isEquiped(final String equipment) {
        return this.hat.equals(equipment) || this.weapon.equals(equipment)
                || this.offhand.equals(equipment)
                || this.shirt.equals(equipment) || this.pants.equals(equipment)
                || this.acc1.equals(equipment) || this.acc2.equals(equipment)
                || this.acc3.equals(equipment)
                || this.famEquip.equals(equipment);
    }

    /**
     * @param equipment
     *            The name of the equipment which is looked for.
     * @return The amount of times the given equipment is used in any equipment
     *         slots.
     */
    public int getNumberOfEquips(final String equipment) {
        // Equipment that can only appear once.
        if (this.hat.equals(equipment) || this.shirt.equals(equipment)
                || this.famEquip.equals(equipment)
                || this.pants.equals(equipment)) {
            return 1;
        }
        // Weapons can appear once or twice.
        if (this.weapon.equals(equipment)) {
            return this.offhand.equals(equipment) ? 2 : 1;
        }
        if (this.offhand.equals(equipment)) {
            return 1;
        }
        // Accessories can appear multiple times without certain order.
        int number = 0;
        if (this.acc1.equals(equipment)) {
            number++;
        }
        if (this.acc2.equals(equipment)) {
            number++;
        }
        if (this.acc3.equals(equipment)) {
            number++;
        }
        return number;
    }

    /**
     * @return The equipment used on the hat slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getHat() {
        return this.hat;
    }

    /**
     * @return The equipment used on the weapon slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getWeapon() {
        return this.weapon;
    }

    /**
     * @return The equipment used on the offhand slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getOffhand() {
        return this.offhand;
    }

    /**
     * @return The equipment used on the shirt slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getShirt() {
        return this.shirt;
    }

    /**
     * @return The equipment used on the pants slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getPants() {
        return this.pants;
    }

    /**
     * @return The equipment used on the acc1 slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getAcc1() {
        return this.acc1;
    }

    /**
     * @return The equipment used on the acc2 slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getAcc2() {
        return this.acc2;
    }

    /**
     * @return The equipment used on the acc3 slot. {@link #NO_EQUIPMENT_STRING}
     *         if no equipment is used.
     */
    public String getAcc3() {
        return this.acc3;
    }

    /**
     * @return The equipment used on the familiar equipment slot.
     *         {@link #NO_EQUIPMENT_STRING} if no equipment is used.
     */
    public String getFamEquip() {
        return this.famEquip;
    }

    @Override
    public String toString() {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder str = new StringBuilder(100);
        str.append("Equipment change on turn ");
        str.append(this.getTurnNumber());
        str.append(newLine);
        str.append("Hat: ");
        str.append(this.hat);
        str.append(newLine);
        str.append("Weapon: ");
        str.append(this.weapon);
        str.append(newLine);
        str.append("Offhand: ");
        str.append(this.offhand);
        str.append(newLine);
        str.append("Shirt: ");
        str.append(this.shirt);
        str.append(newLine);
        str.append("Pants: ");
        str.append(this.pants);
        str.append(newLine);
        str.append("Acc1: ");
        str.append(this.acc1);
        str.append(newLine);
        str.append("Acc2: ");
        str.append(this.acc2);
        str.append(newLine);
        str.append("Acc3: ");
        str.append(this.acc3);
        str.append(newLine);
        str.append("Fam. equipment: ");
        str.append(this.famEquip);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (super.equals(o) && (o instanceof EquipmentChange)) {
            return this.hat.equals(((EquipmentChange) o).getHat())
                    && this.weapon.equals(((EquipmentChange) o).getWeapon())
                    && this.offhand.equals(((EquipmentChange) o).getOffhand())
                    && this.shirt.equals(((EquipmentChange) o).getShirt())
                    && this.pants.equals(((EquipmentChange) o).getPants())
                    && this.acc1.equals(((EquipmentChange) o).getAcc1())
                    && this.acc2.equals(((EquipmentChange) o).getAcc2())
                    && this.acc3.equals(((EquipmentChange) o).getAcc3())
                    && this.famEquip
                            .equals(((EquipmentChange) o).getFamEquip());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 2935;
        result = (result * 31) + super.hashCode();
        result = (result * 31) + this.hat.hashCode();
        result = (result * 31) + this.weapon.hashCode();
        result = (result * 31) + this.offhand.hashCode();
        result = (result * 31) + this.shirt.hashCode();
        result = (result * 31) + this.pants.hashCode();
        result = (result * 31) + this.acc1.hashCode();
        result = (result * 31) + this.acc2.hashCode();
        result = (result * 31) + this.acc3.hashCode();
        result = (result * 31) + this.famEquip.hashCode();
        return result;
    }
}
