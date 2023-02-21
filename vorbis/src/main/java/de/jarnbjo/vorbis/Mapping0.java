/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: Mapping0.java,v 1.2 2003/03/16 01:11:12 jarnbjo Exp $
 * -----------------------------------------------------------
 *
 * $Author: jarnbjo $
 *
 * Description:
 *
 * Copyright 2002-2003 Tor-Einar Jarnbjo
 * -----------------------------------------------------------
 *
 * Change History
 * -----------------------------------------------------------
 * $Log: Mapping0.java,v $
 * Revision 1.2  2003/03/16 01:11:12  jarnbjo
 * no message
 */
package de.jarnbjo.vorbis;

import de.jarnbjo.util.io.BitInputStream;
import java.io.IOException;

class Mapping0 extends Mapping {
    final private int[] magnitudes, angles;
    final private int[] mux, submapFloors, submapResidues;

    protected Mapping0(
            VorbisStream vorbis, BitInputStream source, SetupHeader header)
            throws IOException {
        int submaps = 1;

        if (source.getBit()) {
            submaps = source.getInt(4) + 1;
        }

        //System.out.println("submaps: "+submaps);
        int channels = vorbis.getIdentificationHeader().getChannels();
        int ilogChannels = Util.ilog(channels - 1);

        //System.out.println("ilogChannels: "+ilogChannels);
        if (source.getBit()) {
            int couplingSteps = source.getInt(8) + 1;
            magnitudes = new int[couplingSteps];
            angles = new int[couplingSteps];

            for (int i = 0; i < couplingSteps; i++) {
                magnitudes[i] = source.getInt(ilogChannels);
                angles[i] = source.getInt(ilogChannels);
                if (magnitudes[i] == angles[i] || magnitudes[i] >= channels
                        || angles[i] >= channels) {
                    System.err.println(magnitudes[i]);
                    System.err.println(angles[i]);
                    throw new VorbisFormatException(
                            "The channel magnitude and/or angle mismatch.");
                }
            }
        } else {
            magnitudes = new int[0];
            angles = new int[0];
        }

        if (source.getInt(2) != 0) {
            throw new VorbisFormatException(
                    "A reserved mapping field has an invalid value.");
        }

        mux = new int[channels];
        if (submaps > 1) {
            for (int i = 0; i < channels; i++) {
                mux[i] = source.getInt(4);
                if (mux[i] > submaps) {
                    throw new VorbisFormatException("A mapping mux value is "
                            + "higher than the number of submaps");
                }
            }
        } else {
            for (int i = 0; i < channels; i++) {
                mux[i] = 0;
            }
        }

        submapFloors = new int[submaps];
        submapResidues = new int[submaps];

        int floorCount = header.getFloors().length;
        int residueCount = header.getResidues().length;

        for (int i = 0; i < submaps; i++) {
            source.getInt(8); // discard time placeholder
            submapFloors[i] = source.getInt(8);
            submapResidues[i] = source.getInt(8);

            if (submapFloors[i] > floorCount) {
                throw new VorbisFormatException("A mapping floor value is "
                        + "higher than the number of floors.");
            }

            if (submapResidues[i] > residueCount) {
                throw new VorbisFormatException("A mapping residue value is "
                        + "higher than the number of residues.");
            }
        }
    }

    @Override
    protected int getType() {
        return 0;
    }

    @Override
    protected int[] getAngles() {
        return angles;
    }

    @Override
    protected int[] getMagnitudes() {
        return magnitudes;
    }

    @Override
    protected int[] getMux() {
        return mux;
    }

    @Override
    protected int[] getSubmapFloors() {
        return submapFloors;
    }

    @Override
    protected int[] getSubmapResidues() {
        return submapResidues;
    }

    @Override
    protected int getCouplingSteps() {
        return angles.length;
    }

    @Override
    protected int getSubmaps() {
        return submapFloors.length;
    }
}
