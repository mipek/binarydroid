package de.thwildau.mpekar.binarydroid.assembly;

import android.support.v4.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class enables us to access real file data.
 */
public class ByteAccessor {
    private FileChannel fileChannel;
    private long totalBytes;
    private List<Pair<Long, ByteBuffer>> modified;

    public ByteAccessor(File f) throws FileNotFoundException {
        totalBytes = f.length();
        FileInputStream inputStream = new FileInputStream(f);
        fileChannel = inputStream.getChannel();
        modified = new ArrayList<>();
    }

    /**
     * Retrieve bytes from the file
     * @param address       Address to read bytes from
     * @param bytes         Total count of bytes that are to be read
     * @return              Bytes from the file
     */
    public ByteBuffer getBytes(long address, int bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes);
        return getBytesInternal(byteBuffer, address, bytes);
    }

    /**
     * Modify the specified bytes. This operation is in memory only until you write to a new file.
     * @param address       Address to write to
     * @param bytes         Data that is to be written
     */
    public void modifyBytes(long address, ByteBuffer bytes) {
        Pair<Long, ByteBuffer> pair = new Pair<>(address, bytes);
        modified.add(pair);
    }

    /**
     * Saves the modified bytes in a new file
     * @param dest          Destination of the new file
     */
    public void writeModifiedFile(File dest) throws IOException {
        final int chunkSize = 4096;
        long address = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
        byte [] buffer = new byte[chunkSize];

        FileOutputStream fos = new FileOutputStream(dest);
        try {
            FileChannel channel = fos.getChannel();
            while (address < getTotalBytes()) {
                byteBuffer.clear();
                int maxReadCount = Math.min(chunkSize, (int)(getTotalBytes() - address));
                byteBuffer = getBytesInternal(byteBuffer, address, maxReadCount);

                byteBuffer.rewind();
                channel.write(byteBuffer);
                address += buffer.length;
            }
        } finally {
            fos.close();
        }

    }

    /**
     * Returns the total amount of bytes in this file
     * @return
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    private ByteBuffer getBytesInternal(ByteBuffer byteBuffer, long address, int bytes) {
        try {
            int actualBytes = fileChannel.read(byteBuffer, address);
            if (actualBytes == -1) {
                return null;
            }

            // If we modified some bytes we need to change the bytes we are about to return.
            long addressEnd = address + bytes;
            for (Pair<Long, ByteBuffer> p: modified) {
                ByteBuffer modifyBuffer = p.second;
                long end = p.first + modifyBuffer.limit();
                if (p.first >= address && end <= addressEnd) {
                    long delta = Math.abs(p.first - address);
                    for (int i=0; i<modifyBuffer.limit(); ++i) {
                        // substitute modified data
                        byte val = modifyBuffer.get(i);
                        byteBuffer.put((int)(delta + i), (byte) (val & 0xff));
                    }
                }
            }

            byteBuffer.limit(actualBytes);
            return byteBuffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
