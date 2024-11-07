package info.hermiths.chatapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadBigFileUtils {

    public static Map<Integer, ArrayList<ByteBuffer>> blocks = new HashMap<>();

    public static final long defaultChunkSize = 5 * 1024 * 1024;

    public static void splitFile(File file, long chunkSize) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileChannel = fileInputStream.getChannel();
        long size = fileChannel.size();
        long position = 0;
//        int chunkCount = 0;
        ArrayList<ByteBuffer> buffers = new ArrayList<>();
        while (position < size) {
            long remaining = Math.min(chunkSize, size - position);
            ByteBuffer buffer = ByteBuffer.allocate((int) remaining);
            fileChannel.read(buffer, position);
            buffer.flip();
            buffers.add(buffer); // save buffer
            position += remaining;
//            chunkCount++;
        }
        blocks.put(file.hashCode(), buffers);
        fileInputStream.close();
    }
}
