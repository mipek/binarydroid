package de.thwildau.mpekar.binarydroid.assembly;

public interface Disassembler {
    String disassemble(ByteAccessor accessor, long address);
}
