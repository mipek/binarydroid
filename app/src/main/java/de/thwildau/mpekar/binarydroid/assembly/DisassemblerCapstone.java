package de.thwildau.mpekar.binarydroid.assembly;

import java.nio.ByteBuffer;

import capstone.Capstone;

public class DisassemblerCapstone implements Disassembler {
    Capstone cs;

    void DisassemblerCapstone() {

    }

    @Override
    public String disassemble(ByteAccessor accessor, long address) {
        ByteBuffer buf = accessor.getBytes(address, 4);

        if (cs == null) {
            cs = new Capstone(Capstone.CS_ARCH_ARM, Capstone.CS_MODE_ARM);
        }
        Capstone.CsInsn[] insns = cs.disasm(buf.array(), buf.limit());
        return stringifyInstruction(insns);
    }

    private String stringifyInstruction(Capstone.CsInsn[] insns)
    {
        StringBuilder builder = new StringBuilder();
        for(Capstone.CsInsn insn: insns) {
            builder.append(insn.mnemonic);
            builder.append(' ');
            builder.append(insn.opStr);
        }
        return builder.toString();
    }
}
