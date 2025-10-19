// src/top/mcocet/http/ErrorResponse.java
package top.mcocet.http;

import com.sun.net.httpserver.HttpExchange;
import top.mcocet.service.FileService;
import top.mcocet.service.LoggerService;

import java.io.IOException;
import java.io.OutputStream;

public class ErrorResponse {
    private final FileService fileService;
    private final LoggerService logger;

    public ErrorResponse(FileService fileService, LoggerService logger) {
        this.fileService = fileService;
        this.logger = logger;
    }

    public void send(HttpExchange exchange, int statusCode, String statusText) throws IOException {
        byte[] body;
        String contentType = "text/html; charset=UTF-8";

        if (statusCode == 404 || statusCode == 403 || statusCode == 500) {
            body = fileService.readErrorPage();
        } else {
            body = (statusCode + " " + statusText).getBytes();
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, body.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }

        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String log = String.format("[%s] %s - 错误 %d: %s",
                java.time.LocalDateTime.now(), clientIP, statusCode, exchange.getRequestURI());

        if (statusCode == 404) logger.info(log);
        else if (statusCode >= 500) logger.severe(log);
        else logger.warning(log);
    }
}