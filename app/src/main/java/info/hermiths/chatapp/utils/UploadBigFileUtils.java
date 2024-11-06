package info.hermiths.chatapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class UploadBigFileUtils {

    public static Map<String, HashMap<String, ByteBuffer>> blocks = new HashMap<>();

    private static final long defaultChunkSize = 5 * 1024 * 1024;

    public static void uploadFile(File file, long chunkSize, String uploadUrl) throws IOException {
//        blocks.put("", new HashMap<String, ByteBuffer>());
//        blocks.get("");

        long size = file.length();
        splitFile(file, defaultChunkSize);

        // 取出 splitFile 中保存的分块
//        for (int i = 0; i < chunkCount; i++) {
//            ByteBuffer chunk = getChunk(i); // 从本地或缓存中获取分块
//            uploadChunk(chunk, i, uploadUrl);
//        }
    }

    public static void splitFile(File file, long chunkSize) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileChannel = fileInputStream.getChannel();
        long size = fileChannel.size();
        long position = 0;
        int chunkCount = 0;

        while (position < size) {
            long remaining = Math.min(chunkSize, size - position);
            ByteBuffer buffer = ByteBuffer.allocate((int) remaining);
            fileChannel.read(buffer, position);
            buffer.flip();

            // 将该块保存到本地或者放入队列中准备上传
            // saveChunk(buffer, chunkCount);
            position += remaining;
            chunkCount++;
        }
        fileInputStream.close();
    }

    public void uploadChunk(ByteBuffer chunk, int chunkNumber, String uploadUrl) throws IOException {
        URL url = new URL(uploadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("Put");
        connection.setRequestProperty("Chunk - Number", String.valueOf(chunkNumber));
        connection.getHeaderFieldKey(0);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(chunk.array());
        outputStream.flush();
        outputStream.close();

        if (connection.getResponseCode() == 200) {
            System.out.println("Chunk" + chunkNumber + "uploaded successfully !");
        } else {
            System.out.println("Failed to upload chunk" + chunkNumber);
        }
        connection.disconnect();
    }
}
