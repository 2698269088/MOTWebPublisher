// src/top/mcocet/WebServer.java
package top.mcocet;

import com.sun.net.httpserver.HttpServer;
import top.mcocet.config.ConfigLoader;
import top.mcocet.handler.StaticFileHandler;
import top.mcocet.service.LoggerService;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    public static void main(String[] args) {
        ConfigLoader config = new ConfigLoader("config.yml");
        LoggerService logger = new LoggerService(config.getLogFilePath());

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            server.setExecutor(executor);

            // 注册处理器
            server.createContext("/", new StaticFileHandler(config.getDocumentRoot(), logger));

            server.start();
            logger.info("服务器启动，监听端口: " + config.getPort() +
                    ", 文档根目录: " + config.getDocumentRoot() +
                    ", 安全功能: " + (config.isEnableSecurity() ? "启用" : "禁用"));
            System.out.println("服务器已启动，端口: " + config.getPort() +
                    ", 文档根目录: " + config.getDocumentRoot() +
                    ", 安全功能: " + (config.isEnableSecurity() ? "启用" : "禁用"));
            System.out.println("您可以使用help命令获取指令帮助\n");

            // 启动控制台指令线程
            Thread consoleThread = new Thread(new CommandHandler(config, logger));
            consoleThread.start();
        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            logger.severe("启动失败: " + e.getMessage());
        }
    }
}