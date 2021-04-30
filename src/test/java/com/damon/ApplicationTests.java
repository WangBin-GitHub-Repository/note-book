package com.damon;

import com.damon.socket.SocketClient;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void socketTest() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 50; i++) {
            SocketClient socketClient = new SocketClient("127.0.0.1", 4433);
            socketClient.init(new File("D:\\rpmbuild.tar.gz"), "D:\\test\\rpmbuild" + i + ".tar.gz");
            executorService.execute(socketClient);
        }

        while (true) {

        }
    }
}
