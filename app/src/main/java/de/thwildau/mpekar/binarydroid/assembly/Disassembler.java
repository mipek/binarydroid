package de.thwildau.mpekar.binarydroid.assembly;

/**
 * Disassembling functionality
 */
public interface Disassembler {
    /**
     * Disassembles virtual memory
     * @param accessor  accessor used to access virtual memory
     * @param address   address that is to be disassembled
     * @param bytes     how many bytes to disassmble
     * @return          array of instructions
     */
    Instruction [] disassemble(ByteAccessor accessor, long address, int bytes);

    /**
     * Disassembles instructions
     * @param code      code as a byte array
     * @param address   address where this byte array resides in
     * @param bytes     length of the byte array
     * @return          array of instructions
     */
    Instruction [] disassemble(byte [] code, long address, int bytes);

    /**
     * Describes a single instruction
     */
    interface Instruction {
        /**
         * Returns the mnemonic of this instruction.
         * @return  mnemonic
         */
        String mnemonic();

        /**
         * Returns the operands of this instruction
         * @return  operands
         */
        String operands();

        /**
         * Size of instruction
         * @return  instruction size
         */
        short size();

        /**
         * Returns true if this function modifies the innstruction pointer
         * @return  true if IP is modified by this instruction
         */
        boolean isReturn();
    }
}
