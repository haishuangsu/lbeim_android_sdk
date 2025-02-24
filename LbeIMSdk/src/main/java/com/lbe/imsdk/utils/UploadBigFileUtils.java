package com.lbe.imsdk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadBigFileUtils {

    public static Map<String, ArrayList<ByteBuffer>> blocks = new HashMap<>();

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
        blocks.put(file.getAbsolutePath(), buffers);
        fileInputStream.close();
    }

    public static void splitFile(InputStream inputStream, long chunkSize, String path) throws IOException {
        ArrayList<ByteBuffer> buffers = new ArrayList<>();
        byte[] buffer = new byte[(int) chunkSize];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytesRead);
            byteBuffer.put(buffer, 0, bytesRead);
            byteBuffer.flip();
            buffers.add(byteBuffer);
        }
        blocks.put(path, buffers);
    }

    public static void releaseMemory(String path) {
        ArrayList<ByteBuffer> buffers = blocks.get(path);
        assert buffers != null;
        buffers.clear();
        blocks.remove(path);
    }
}
