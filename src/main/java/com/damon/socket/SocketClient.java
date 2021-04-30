package com.damon.socket;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @author damon
 * @version 1.0 2020/9/25
 */
public class SocketClient extends Socket implements Runnable {
    public static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private Socket client;
    private FileInputStream fileInputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private File file;
    private String filePath;

    /**
     * 构造函数与服务器建立连接
     *
     * @throws Exception
     */
    public SocketClient(String SERVER_HOST, Integer SERVER_PORT) throws Exception {
        super(SERVER_HOST, SERVER_PORT);
        this.client = this;
    }

    public void init(File file, String filePath) {
        this.file = file;
        this.filePath = filePath;
    }

    @lombok.SneakyThrows
    public void send(File file) {
        try {
            fileInputStream = new FileInputStream(file);
            dataOutputStream = new DataOutputStream(client.getOutputStream());

            // 文件长度
            dataOutputStream.writeLong(file.length());
            dataOutputStream.flush();

            byte[] bytes = new byte[1024 * 1024 * 4];
            int readLength;
            long progress = 0;
            while ((readLength = fileInputStream.read(bytes)) != -1) {
                progress += readLength;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(bytes, 0, readLength);
                byteArrayOutputStream.flush();

                byte[] data = byteArrayOutputStream.toByteArray();
                dataOutputStream.write(data, 0, readLength);
                dataOutputStream.flush();
                logger.info("| " + (100 * progress / file.length()) + "% |");
                if (progress == file.length()) {
                    break;
                }
            }
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    @lombok.SneakyThrows
    public void receive(String filePath) {
        try {
            dataInputStream = new DataInputStream(client.getInputStream());
            byte[] readBytes = new byte[1024 * 64];

            int writeLength;
            int progress = 0;
            while ((writeLength = dataInputStream.read(readBytes)) != -1) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(readBytes, 0, writeLength);
                byteArrayOutputStream.flush();
                byte[] data = byteArrayOutputStream.toByteArray();
                progress += writeLength;
                FileUtils.writeByteArrayToFile(new File(filePath), data, true);
                if (progress == file.length()) {
                    break;
                }
            }
        } finally {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (dataInputStream != null) {
                dataInputStream.close();
            }

            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void run() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("socket-pool-%d").build();
        ExecutorService singleThreadPool = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        singleThreadPool.execute(() -> {
            this.send(this.file);
        });
        singleThreadPool.execute(() -> {
            this.receive(this.filePath);
        });
    }
}
