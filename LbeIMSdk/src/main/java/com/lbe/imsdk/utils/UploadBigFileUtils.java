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

    // 使用 InputStream 分块的重载方法
    public static void splitFile(InputStream inputStream, long chunkSize) throws IOException {
        // 使用 ByteBuffer 来存储每个分块
        ArrayList<ByteBuffer> buffers = new ArrayList<>();
        byte[] buffer = new byte[(int) chunkSize];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytesRead);
            byteBuffer.put(buffer, 0, bytesRead);
            byteBuffer.flip(); // 准备好读取
            buffers.add(byteBuffer); // 保存分块
        }
        // 以文件的哈希值为 key 来存储块数据
        blocks.put(inputStream.hashCode(), buffers);
    }

    public static void releaseMemory(int hashCode) {
        ArrayList<ByteBuffer> buffers = blocks.get(hashCode);
        assert buffers != null;
        buffers.clear();
        blocks.remove(hashCode);
    }
}
