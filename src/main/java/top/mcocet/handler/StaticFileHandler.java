// src/top/mcocet/handler/StaticFileHandler.java
package top.mcocet.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import top.mcocet.http.ErrorResponse;
import top.mcocet.service.FileService;
import top.mcocet.service.LoggerService;
import top.mcocet.config.ConfigLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Set;

public class StaticFileHandler implements HttpHandler, RequestHandler {
    private final FileService fileService;
    private final LoggerService logger;
    private final ErrorResponse errorResponse;
    private final ConfigLoader config;

    public StaticFileHandler(String documentRoot, LoggerService logger) {
        this.logger = logger;
        this.fileService = new FileService(documentRoot, "index.html", "50x.html", logger);
        this.errorResponse = new ErrorResponse(fileService, logger);
        this.config = new ConfigLoader("config.yml");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String uri = exchange.getRequestURI().toString();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String clientHost = ((InetSocketAddress) exchange.getRemoteAddress()).getHostName();
        logger.info("收到请求: " + clientIP + " (" + clientHost + ") - 方法: " + method + " - URI: " + uri);

        // 检查是否启用安全功能
        if (config.isEnableSecurity()) {
            // 检查黑名单
            if (config.getBlacklistedIPs().contains(clientIP)) {
                errorResponse.send(exchange, 403, "Forbidden");
                logger.warning("[" + LocalDateTime.now() + "] " + clientIP + " - 禁止访问: " + uri);
                return;
            }

            // 检查允许的主机列表
            if (!config.getAllowedHosts().isEmpty() && !config.getAllowedHosts().contains(clientHost) && !config.getAllowedHosts().contains(clientIP)) {
                errorResponse.send(exchange, 403, "Forbidden");
                logger.warning("[" + LocalDateTime.now() + "] " + clientIP + " (" + clientHost + ") - 不允许的主机: " + uri);
                return;
            }

            // 额外检查Host头部，防止IP地址访问
            String hostHeader = exchange.getRequestHeaders().getFirst("Host");
            if (hostHeader != null && !config.getAllowedHosts().contains(hostHeader)) {
                errorResponse.send(exchange, 403, "Forbidden");
                logger.warning("[" + LocalDateTime.now() + "] " + clientIP + " - Host头部不匹配: " + hostHeader);
                return;
            }
        }

        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            errorResponse.send(exchange, 405, "Method Not Allowed");
            logger.info("[" + java.time.LocalDateTime.now() + "] " + clientIP + " - 方法不被允许: " + method);
            return;
        }

        var filePathOpt = fileService.resolveFilePath(uri);
        if (filePathOpt.isEmpty()) {
            errorResponse.send(exchange, 404, "Not Found");
            return;
        }

        Path filePath = filePathOpt.get();
        var mimeTypeOpt = fileService.getMimeType(filePath);
        String mimeType = mimeTypeOpt.orElse("application/octet-stream");

        long fileSize;
        try {
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            errorResponse.send(exchange, 500, "Internal Server Error");
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", mimeType);

        /*
        if (!"GET".equals(method)) {
            errorResponse.send(exchange, 405, "Method Not Allowed");
            logger.info("[" + java.time.LocalDateTime.now() + "] " + clientIP + " - 方法不被允许: " + method);
            return;
        }
        // 这是原有处理GET和HEAD请求的代码
        // 此处可能会导致Content-Length警告
        // HEAD一般不会返回长度数据
         */

        // 根据请求方法决定是否发送内容体
        if ("GET".equals(method)) {
            exchange.sendResponseHeaders(200, fileSize);
        } else { // HEAD 请求
            exchange.sendResponseHeaders(200, -1); // -1 表示无内容体
        }

        // 只在 GET 请求时传输内容
        if ("GET".equals(method)) {
            try (InputStream is = Files.newInputStream(filePath);
                 OutputStream os = exchange.getResponseBody()) {
                is.transferTo(os);
            }
        }

        String fileName = filePath.getFileName().toString();
        String log = "[" + java.time.LocalDateTime.now() + "] " + clientIP + " - 访问: " + uri;
        if (mimeType.startsWith("text/html")) {
            System.out.println(log);
            logger.info(log);

            // 检查 Host 头部
            String hostHeader = exchange.getRequestHeaders().getFirst("Host");
            if (hostHeader != null && hostHeader.equals(clientIP)) {
                System.out.println("警告: 客户端 " + clientIP + " 通过IP地址访问服务器，而不是域名。");
            }

            // 记录HTML页面的访问次数
            logger.recordAccess(clientIP);
        } else {
            logger.info(log + " (资源, MIME: " + mimeType + ")");
        }
    }

}