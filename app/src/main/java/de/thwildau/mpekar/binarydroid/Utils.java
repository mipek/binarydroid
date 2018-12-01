package de.thwildau.mpekar.binarydroid;

import android.graphics.Paint;

import de.thwildau.mpekar.binarydroid.assembly.Disassembler;

public class Utils {
    private static final String HEXCHARS = "0123456789ABCDEF";

    public static String l2s(long value, int pad) {
        return String.format("%0" + pad + "X", value);
    }

    public static String b2s(byte value) {
        StringBuilder builder = new StringBuilder();
        b2s(builder, value);
        return builder.toString();
    }

    public static void b2s(StringBuilder builder, byte value) {
        builder.append(HEXCHARS.charAt((value & 0xF0) >> 4))
               .append(HEXCHARS.charAt(value & 0x0F));
    }

    private static Disassembler.Instruction dummy;
    public static Disassembler.Instruction dummyInstruction(final short size) {
        if (dummy == null || dummy.size() != size) {
            dummy = new Disassembler.Instruction() {
                @Override
                public short size() {
                    return size;
                }

                @Override
                public boolean isReturn() {
                    return false;
                }

                @Override
                public String toString() {
                    return "undef";
                }
            };
        }
        return dummy;
    }
}
