package de.thwildau.mpekar.binarydroid.assembly;

import de.thwildau.mpekar.binarydroid.assembly.oracleassembler.AssemblerException;

public interface Assembler {
    /**
     * Assemble a single line of code
     * @param assembly      Code that is to be assembled
     * @param address       Assembling address
     * @return              Bytes of the assembled instruction or null on error.
     */
    byte [] assembleSingle(String assembly, long address) throws AssemblerException;
}
