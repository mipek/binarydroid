package de.thwildau.mpekar.binarydroid.disasm;

public interface BinaryAccessor {
    void getBytes(long offset, int length, byte [] bytes);
}
