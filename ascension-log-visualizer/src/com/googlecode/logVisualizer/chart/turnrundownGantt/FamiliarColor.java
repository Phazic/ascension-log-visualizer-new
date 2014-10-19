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
package com.googlecode.logVisualizer.chart.turnrundownGantt;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

public final class FamiliarColor {
    private final String familiarName;
    private Colors color;

    public FamiliarColor(final String familiarName, final String colorName) {
        this.familiarName = familiarName;
        this.color = Colors.fromString(colorName);
    }

    public String getFamiliarName() {
        return this.familiarName;
    }

    public void setColor(final Colors color) {
        this.setColor(color.toString());
    }

    public void setColor(final String colorName) {
        this.color = Colors.fromString(colorName);
    }

    public Colors getColor() {
        return this.color;
    }

    public Paint getColorPaint() {
        return this.color.getColor();
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null) {
            if (o instanceof FamiliarColor) {
                return ((FamiliarColor) o).getFamiliarName().equals(
                        this.familiarName)
                        && ((FamiliarColor) o).getColor().equals(this.color);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 3253;
        result = (result * 31) + this.familiarName.hashCode();
        result = (result * 31) + this.color.hashCode();
        return result;
    }

    public static enum Colors {
        NONE("none", Color.WHITE), BLUE("blue", new Color(200, 225, 255)), YELLOW(
                "yellow", new Color(255, 255, 200)), GREEN("green", new Color(
                200, 255, 200)), RED("red", new Color(255, 200, 255)), TURQUOISE(
                "turquoise", new Color(200, 255, 255)), GRAY("gray", new Color(
                200, 200, 175));
        private static final Map<String, Colors> stringToEnum = new HashMap<>();
        static {
            for (final Colors op : Colors.values()) {
                Colors.stringToEnum.put(op.toString(), op);
            }
        }
        private final String colorName;
        private final Paint color;

        Colors(final String colorName, final Paint color) {
            this.colorName = colorName;
            this.color = color;
        }

        public Paint getColor() {
            return this.color;
        }

        @Override
        public String toString() {
            return this.colorName;
        }

        /**
         * @return The enum whose toString method returns a string which is
         *         equal to the given string. If no match is found this method
         *         will return <code>WHITE</code>.
         */
        public static Colors fromString(final String colorName) {
            final Colors color = Colors.stringToEnum.get(colorName);
            return color != null ? color : NONE;
        }
    }
}
