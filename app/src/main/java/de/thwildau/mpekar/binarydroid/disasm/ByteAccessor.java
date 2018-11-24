package de.thwildau.mpekar.binarydroid.disasm;

import net.fornwall.jelf.ElfFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ByteAccessor {
    //private FileInputStream inputStream;
    private FileChannel fileChannel;
    ///private ByteBuffer byteBuffer;

    public ByteAccessor(File f) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(f);
        fileChannel = inputStream.getChannel();
    }

    public ByteBuffer getBytes(long address, int bytes) {
        ////if (byteBuffer == null || byteBuffer.capacity() < bytes) {
        ////    byteBuffer = ByteBuffer.allocate(1024);
        ////}
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes);

        try {
            //inputStream.reset();
            //inputStream.skip(address);
            //return inputStream.read(out, 0, bytes);
            int actualBytes = fileChannel.read(byteBuffer);
            byteBuffer.limit(actualBytes);
            return byteBuffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
