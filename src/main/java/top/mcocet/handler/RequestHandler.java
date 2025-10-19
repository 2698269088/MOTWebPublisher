// src/top/mcocet/handler/RequestHandler.java
package top.mcocet.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface RequestHandler {
    void handle(HttpExchange exchange) throws IOException;
}