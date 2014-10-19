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
package com.googlecode.logVisualizer.parser.lineParsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.UtilityConstants;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.turn.turnAction.EquipmentChange;
import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * A parser for the equipment change notation in mafia session logs.
 * <p>
 * The format looks like this:
 * <p>
 * {@code equip _slotName_ _itemName_}
 * <p>
 * OR
 * <p>
 * {@code unequip _slotName_ _itemName_}
 * <p>
 * OR
 * <p>
 * {@code outfit _outfitName_}
 * <p>
 * OR
 * <p>
 * {@code custom outfit _outfitName_}
 */
public final class EquipmentLineParser extends AbstractLineParser {
    private static final Map<String, EquipmentSetup> outfitsMap;
    static {
        outfitsMap = new HashMap<>(150);
        final Pattern outfitPattern = Pattern
                .compile(".+\\|.+\\|.+\\|.+\\|.+\\|.+\\|.+\\|.+\\|.+");
        final String splitPattern = "\\s*\\|\\s*";
        String tmpLine;
        final BufferedReader br = DataUtilities.getReader(
                UtilityConstants.KOL_DATA_DIRECTORY, "outfits.txt");
        try {
            while ((tmpLine = br.readLine()) != null) {
                if (!tmpLine.startsWith("//") && (tmpLine.length() > 15)
                        && outfitPattern.matcher(tmpLine).matches()) {
                    final String[] result = tmpLine.split(splitPattern);
                    final String outfitName = result[0]
                            .toLowerCase(Locale.ENGLISH);
                    final boolean isHat = Boolean.parseBoolean(result[1]);
                    final boolean isWeapon = Boolean.parseBoolean(result[2]);
                    final boolean isOffhand = Boolean.parseBoolean(result[3]);
                    final boolean isShirt = Boolean.parseBoolean(result[4]);
                    final boolean isPants = Boolean.parseBoolean(result[5]);
                    final boolean isAcc1 = Boolean.parseBoolean(result[6]);
                    final boolean isAcc2 = Boolean.parseBoolean(result[7]);
                    final boolean isAcc3 = Boolean.parseBoolean(result[8]);
                    EquipmentLineParser.outfitsMap.put(outfitName,
                            new EquipmentSetup(isHat, isWeapon, isOffhand,
                                    isShirt, isPants, isAcc1, isAcc2, isAcc3));
                }
            }
            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    private static final String EQUIP_STRING = "equip";
    private static final String UNEQUIP_STRING = "unequip";
    private static final String OUTFIT_STRING = "outfit";
    private static final String CUSTOM_OUTFIT_STRING = "custom outfit";
    private EquipmentChange lastUsedEquipment = new EquipmentChange(0,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING,
            EquipmentChange.NO_EQUIPMENT_STRING);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean parseLine(final String line, final LogDataHolder logData) {
        // Sometimes the action classifier strings (equip/unequip/outfit) are
        // written with a capital first letter, sometimes not. This side-steps
        // that problem.
        return super.parseLine(line.toLowerCase(Locale.ENGLISH), logData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final String line, final LogDataHolder logData) {
        // Outfit handling
        if (line.startsWith(EquipmentLineParser.OUTFIT_STRING)) {
            final EquipmentSetup outfit = EquipmentLineParser.outfitsMap
                    .get(line.substring(line
                            .indexOf(UsefulPatterns.WHITE_SPACE) + 1));
            if (outfit != null) {
                final EquipmentChange lastChange = logData
                        .getLastEquipmentChange();
                this.lastUsedEquipment = lastChange;
                final String hat = outfit.isHat() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getHat();
                final String weapon = outfit.isWeapon() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getWeapon();
                final String offhand = outfit.isOffhand() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getOffhand();
                final String shirt = outfit.isShirt() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getShirt();
                final String pants = outfit.isPants() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getPants();
                final String acc1 = outfit.isAcc1() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getAcc1();
                final String acc2 = outfit.isAcc2() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getAcc2();
                final String acc3 = outfit.isAcc3() ? EquipmentChange.NO_EQUIPMENT_STRING
                        : lastChange.getAcc3();
                logData.addEquipmentChange(new EquipmentChange(logData
                        .getTurnsSpent().last().getEndTurn(), hat, weapon,
                        offhand, shirt, pants, acc1, acc2, acc3, lastChange
                                .getFamEquip()));
            }
            return;
        } else if (line.startsWith(EquipmentLineParser.CUSTOM_OUTFIT_STRING)) {
            if (line.equals("custom outfit backup")
                    || line.equals("custom outfit your previous outfit")) {
                // Those two custom outfits roll the equipment back to the last
                // used one, so we do the same.
                logData.addEquipmentChange(new EquipmentChange(logData
                        .getTurnsSpent().last().getEndTurn(),
                        this.lastUsedEquipment.getHat(), this.lastUsedEquipment
                                .getWeapon(), this.lastUsedEquipment
                                .getOffhand(), this.lastUsedEquipment
                                .getShirt(), this.lastUsedEquipment.getPants(),
                        this.lastUsedEquipment.getAcc1(),
                        this.lastUsedEquipment.getAcc2(),
                        this.lastUsedEquipment.getAcc3(),
                        this.lastUsedEquipment.getFamEquip()));
            } else {
                // We cannot guarantee for anything as far as custom outfits are
                // concerned, so the only sensible thing is to assume nothing in
                // the way of character equipment.
                final EquipmentChange lastChange = logData
                        .getLastEquipmentChange();
                this.lastUsedEquipment = lastChange;
                logData.addEquipmentChange(new EquipmentChange(logData
                        .getTurnsSpent().last().getEndTurn(),
                        EquipmentChange.NO_EQUIPMENT_STRING,
                        EquipmentChange.NO_EQUIPMENT_STRING,
                        EquipmentChange.NO_EQUIPMENT_STRING,
                        EquipmentChange.NO_EQUIPMENT_STRING,
                        EquipmentChange.NO_EQUIPMENT_STRING,
                        EquipmentChange.NO_EQUIPMENT_STRING,
                        EquipmentChange.NO_EQUIPMENT_STRING,
                        EquipmentChange.NO_EQUIPMENT_STRING, lastChange
                                .getFamEquip()));
            }
            return;
        }
        // Equip/Unequip handling
        final String tmp = line.substring(line
                .indexOf(UsefulPatterns.WHITE_SPACE) + 1);
        final int whiteSpaceIndex = tmp.indexOf(UsefulPatterns.WHITE_SPACE);
        // Strings that don't fit the format should be ignored.
        if (whiteSpaceIndex < 0) {
            return;
        }
        final String slotName = tmp.substring(0, whiteSpaceIndex);
        final String itemName = tmp.substring(whiteSpaceIndex + 1);
        // Act depending on equip or unequip.
        if (line.startsWith(EquipmentLineParser.EQUIP_STRING)) {
            this.addEquipmentChange(slotName, itemName, logData);
        } else {
            this.addEquipmentChange(slotName,
                    EquipmentChange.NO_EQUIPMENT_STRING, logData);
        }
    }

    private void addEquipmentChange(final String slotName,
            final String itemName, final LogDataHolder logData) {
        final EquipmentChange lastChange = logData.getLastEquipmentChange();
        this.lastUsedEquipment = lastChange;
        // Switch constructs are ugly, but there isn't really a better way to do
        // this since at some point which equipment slot is used has to be
        // checked.
        final EquipmentChange equipmentChange;
        switch (EquipmentSlot.fromString(slotName)) {
        case HAT:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), itemName, lastChange.getWeapon(),
                    lastChange.getOffhand(), lastChange.getShirt(),
                    lastChange.getPants(), lastChange.getAcc1(),
                    lastChange.getAcc2(), lastChange.getAcc3(),
                    lastChange.getFamEquip());
            break;
        case WEAPON:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(), itemName,
                    lastChange.getOffhand(), lastChange.getShirt(),
                    lastChange.getPants(), lastChange.getAcc1(),
                    lastChange.getAcc2(), lastChange.getAcc3(),
                    lastChange.getFamEquip());
            break;
        case OFFHAND:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(),
                    lastChange.getWeapon(), itemName, lastChange.getShirt(),
                    lastChange.getPants(), lastChange.getAcc1(),
                    lastChange.getAcc2(), lastChange.getAcc3(),
                    lastChange.getFamEquip());
            break;
        case SHIRT:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(),
                    lastChange.getWeapon(), lastChange.getOffhand(), itemName,
                    lastChange.getPants(), lastChange.getAcc1(),
                    lastChange.getAcc2(), lastChange.getAcc3(),
                    lastChange.getFamEquip());
            break;
        case PANTS:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(),
                    lastChange.getWeapon(), lastChange.getOffhand(),
                    lastChange.getShirt(), itemName, lastChange.getAcc1(),
                    lastChange.getAcc2(), lastChange.getAcc3(),
                    lastChange.getFamEquip());
            break;
        case ACC1:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(),
                    lastChange.getWeapon(), lastChange.getOffhand(),
                    lastChange.getShirt(), lastChange.getPants(), itemName,
                    lastChange.getAcc2(), lastChange.getAcc3(),
                    lastChange.getFamEquip());
            break;
        case ACC2:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(),
                    lastChange.getWeapon(), lastChange.getOffhand(),
                    lastChange.getShirt(), lastChange.getPants(),
                    lastChange.getAcc1(), itemName, lastChange.getAcc3(),
                    lastChange.getFamEquip());
            break;
        case ACC3:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(),
                    lastChange.getWeapon(), lastChange.getOffhand(),
                    lastChange.getShirt(), lastChange.getPants(),
                    lastChange.getAcc1(), lastChange.getAcc2(), itemName,
                    lastChange.getFamEquip());
            break;
        case FAM_EQUIP:
            equipmentChange = new EquipmentChange(logData.getTurnsSpent()
                    .last().getEndTurn(), lastChange.getHat(),
                    lastChange.getWeapon(), lastChange.getOffhand(),
                    lastChange.getShirt(), lastChange.getPants(),
                    lastChange.getAcc1(), lastChange.getAcc2(),
                    lastChange.getAcc3(), itemName);
            break;
        default:
            equipmentChange = null;
            break;
        }
        // Add the equipment change.
        if (equipmentChange != null) {
            logData.addEquipmentChange(equipmentChange);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleLine(final String line) {
        return line.startsWith(EquipmentLineParser.EQUIP_STRING)
                || line.startsWith(EquipmentLineParser.UNEQUIP_STRING)
                || line.startsWith(EquipmentLineParser.OUTFIT_STRING)
                || line.startsWith(EquipmentLineParser.CUSTOM_OUTFIT_STRING);
    }

    private static class EquipmentSetup {
        private final boolean isHat;
        private final boolean isWeapon;
        private final boolean isOffhand;
        private final boolean isShirt;
        private final boolean isPants;
        private final boolean isAcc1;
        private final boolean isAcc2;
        private final boolean isAcc3;

        EquipmentSetup(final boolean isHat, final boolean isWeapon,
                final boolean isOffhand, final boolean isShirt,
                final boolean isPants, final boolean isAcc1,
                final boolean isAcc2, final boolean isAcc3) {
            this.isHat = isHat;
            this.isWeapon = isWeapon;
            this.isOffhand = isOffhand;
            this.isShirt = isShirt;
            this.isPants = isPants;
            this.isAcc1 = isAcc1;
            this.isAcc2 = isAcc2;
            this.isAcc3 = isAcc3;
        }

        public boolean isHat() {
            return this.isHat;
        }

        public boolean isWeapon() {
            return this.isWeapon;
        }

        public boolean isOffhand() {
            return this.isOffhand;
        }

        public boolean isShirt() {
            return this.isShirt;
        }

        public boolean isPants() {
            return this.isPants;
        }

        public boolean isAcc1() {
            return this.isAcc1;
        }

        public boolean isAcc2() {
            return this.isAcc2;
        }

        public boolean isAcc3() {
            return this.isAcc3;
        }
    }

    /**
     * This enumeration represents all equipment slots.
     */
    private static enum EquipmentSlot {
        HAT("hat"), WEAPON("weapon"), OFFHAND("off-hand"), SHIRT("shirt"), PANTS(
                "pants"), ACC1("acc1"), ACC2("acc2"), ACC3("acc3"), FAM_EQUIP(
                "familiar"), NOT_DEFINED("not defined");
        private static final Map<String, EquipmentSlot> stringToEnum = new HashMap<>();
        static {
            for (final EquipmentSlot op : EquipmentSlot.values()) {
                EquipmentSlot.stringToEnum.put(op.toString(), op);
            }
        }
        private final String slotName;

        EquipmentSlot(final String slotName) {
            this.slotName = slotName;
        }

        @Override
        public String toString() {
            return this.slotName;
        }

        /**
         * @return The enum whose toString method returns a string which is
         *         equal to the given string. If no match is found this method
         *         will return {@code NOT_DEFINED}.
         */
        public static EquipmentSlot fromString(final String slotName) {
            if (slotName == null) {
                throw new NullPointerException("Slot name must not be null.");
            }
            final EquipmentSlot equipmentSlot = EquipmentSlot.stringToEnum
                    .get(slotName);
            return equipmentSlot != null ? equipmentSlot : NOT_DEFINED;
        }
    }
}
