package de.thwildau.mpekar.binarydroid.disasm;

import net.fornwall.jelf.ElfFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ByteAccessor {
    private FileInputStream inputStream;

    public ByteAccessor(File f) throws FileNotFoundException {
        inputStream = new FileInputStream(f);
    }

    public int getBytes(long address, int bytes, byte[] out) {
        try {
            inputStream.reset();
            inputStream.skip(address);
            return inputStream.read(out, 0, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
