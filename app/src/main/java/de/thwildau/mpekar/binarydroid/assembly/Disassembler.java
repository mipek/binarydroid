package de.thwildau.mpekar.binarydroid.assembly;

public interface Disassembler {
    Instruction [] disassemble(ByteAccessor accessor, long address, int bytes);

    interface Instruction {
        short size();
        boolean isReturn();
    }
}
