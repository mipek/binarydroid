package de.thwildau.mpekar.binarydroid.assembly;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import capstone.Capstone;

public class DisassemblerCapstone implements Disassembler {
    private static Capstone cs;

    @Override
    public Instruction [] disassemble(ByteAccessor accessor, long address, int size) {
        ByteBuffer buf = accessor.getBytes(address, size);
        if (buf == null) {
            return new InstructionCapstone[0];
        }

        ensureCs();

        Capstone.CsInsn[] capInsns = cs.disasm(buf.array(), buf.limit());
        InstructionCapstone[] insns = new InstructionCapstone[capInsns.length];
        for (int i=0; i<insns.length; ++i) {
            insns[i] = new InstructionCapstone(capInsns[i]);
        }
        return insns;
    }

    @Override
    public Instruction [] disassemble(byte [] code, long address, int bytes) {
        ensureCs();

        Capstone.CsInsn[] capInsns = cs.disasm(code, bytes);
        InstructionCapstone[] insns = new InstructionCapstone[capInsns.length];
        for (int i=0; i<insns.length; ++i) {
            insns[i] = new InstructionCapstone(capInsns[i]);
        }
        return insns;
    }

    // Ensures that we have a valid Capstone instance
    private void ensureCs() {
        if (cs == null) {
            cs = new Capstone(Capstone.CS_ARCH_ARM,
                    Capstone.CS_MODE_LITTLE_ENDIAN | Capstone.CS_MODE_ARM);
        }
    }

    private class InstructionCapstone implements Instruction {
        private Capstone.CsInsn insn;

        public InstructionCapstone(Capstone.CsInsn insn) {
            this.insn = insn;
        }

        @Override
        public String mnemonic() {
            return insn.mnemonic;
        }

        @Override
        public String operands() {
            return insn.opStr;
        }

        @Override
        public short size() {
            return insn.size;
        }

        @Override
        public boolean isReturn() {
            // TODO: we can probably get something out of opinfo (like EIP being modified?)
            // ARM branch
            final byte [] branch = fromHexString("C7 FC FF EA");
            final byte [] ldmfd = fromHexString("70 8C BD E8");

            if (isEqual(insn.bytes, branch))
                return true;
            if (isEqual(insn.bytes, ldmfd))
                return true;
            return false;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(insn.mnemonic);
            builder.append(' ');
            builder.append(insn.opStr);
            return builder.toString();
        }
    }

    private static boolean isEqual(byte [] a, byte []b ){
        if (a.length != b.length) return false;
        for (int i=0; i<a.length; ++i) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }

    public static byte[] fromHexString(String src) {
        byte[] biBytes = new BigInteger("10" + src.replaceAll("\\s", ""), 16).toByteArray();
        return Arrays.copyOfRange(biBytes, 1, biBytes.length);
    }
}
