package com.damon.socket;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Socket 处理器
 *
 * @author damon
 * @version 1.0 2020/9/24
 */
public class SocketHandler implements Runnable {
    public static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public SocketHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        this.handle();
    }

    public void handle() {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            long fileLength = dataInputStream.readLong();
            byte[] bytes = new byte[1024 * 64];
            int length;
            long progress = 0;
            while ((length = dataInputStream.read(bytes)) != -1) {
                progress += length;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(bytes, 0, length);
                byteArrayOutputStream.flush();
                byte[] data = byteArrayOutputStream.toByteArray();
                dataOutputStream.write(data);
                dataOutputStream.flush();
                if (progress == fileLength) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
    }

    @SneakyThrows
    private void close() {
        if (dataInputStream != null) {
            dataInputStream.close();
        }
        if (dataOutputStream != null) {
            dataOutputStream.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}
