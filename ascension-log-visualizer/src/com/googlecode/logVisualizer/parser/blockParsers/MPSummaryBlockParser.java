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
package com.googlecode.logVisualizer.parser.blockParsers;

import java.io.BufferedReader;
import java.io.IOException;

import com.googlecode.logVisualizer.logData.LogDataHolder;

/**
 * A parser for the mp summary at the end of preparsed ascension logs.
 */
public final class MPSummaryBlockParser extends AbstractBlockParser {
    private final String ENCOUNTER_MP_BEGINNING_STRING = "Inside Encounters: ";
    private final String STARFISH_MP_BEGINNING_STRING = "Starfish Familiars: ";
    private final String RESTING_MP_BEGINNING_STRING = "Resting: ";
    private final String OUTSIDE_ENCOUNTERS_MP_BEGINNING_STRING = "Outside Encounters: ";
    private final String CONSUMABLES_MP_BEGINNING_STRING = "Consumables: ";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParsing(final BufferedReader reader,
            final LogDataHolder logData) throws IOException {
        int emptyLineCounter = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                if (line.startsWith(this.ENCOUNTER_MP_BEGINNING_STRING)) {
                    logData.getLogSummary()
                            .getMPGains()
                            .setEncounterMPGain(
                                    MPSummaryBlockParser.parseOutMP(line,
                                            this.ENCOUNTER_MP_BEGINNING_STRING));
                } else if (line.startsWith(this.STARFISH_MP_BEGINNING_STRING)) {
                    logData.getLogSummary()
                            .getMPGains()
                            .setStarfishMPGain(
                                    MPSummaryBlockParser.parseOutMP(line,
                                            this.STARFISH_MP_BEGINNING_STRING));
                } else if (line.startsWith(this.RESTING_MP_BEGINNING_STRING)) {
                    logData.getLogSummary()
                            .getMPGains()
                            .setRestingMPGain(
                                    MPSummaryBlockParser.parseOutMP(line,
                                            this.RESTING_MP_BEGINNING_STRING));
                } else if (line
                        .startsWith(this.OUTSIDE_ENCOUNTERS_MP_BEGINNING_STRING)) {
                    logData.getLogSummary()
                            .getMPGains()
                            .setOutOfEncounterMPGain(
                                    MPSummaryBlockParser
                                            .parseOutMP(
                                                    line,
                                                    this.OUTSIDE_ENCOUNTERS_MP_BEGINNING_STRING));
                } else if (line
                        .startsWith(this.CONSUMABLES_MP_BEGINNING_STRING)) {
                    logData.getLogSummary()
                            .getMPGains()
                            .setConsumableMPGain(
                                    MPSummaryBlockParser
                                            .parseOutMP(
                                                    line,
                                                    this.CONSUMABLES_MP_BEGINNING_STRING));
                }
                emptyLineCounter = 0;
            } else {
                emptyLineCounter++;
                if (emptyLineCounter >= 2) {
                    reader.reset();
                    break;
                }
                reader.mark(10);
            }
        }
    }

    private static int parseOutMP(final String line, final String prefix) {
        return Integer.parseInt(line.substring(prefix.length()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCompatibleBlock(final String line) {
        return line.contains("MP GAINS");
    }
}
