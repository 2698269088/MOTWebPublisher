// src/top/mcocet/service/LoggerService.java
package top.mcocet.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerService {
    private final Logger logger;
    private final Map<String, Integer> ipAccessCount;

    public LoggerService(String logFile) {
        logger = Logger.getLogger(LoggerService.class.getName());
        logger.setUseParentHandlers(false);
        ipAccessCount = new HashMap<>();

        try {
            FileHandler fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("日志初始化失败: " + e.getMessage());
        }
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }

    public void severe(String msg) {
        logger.severe(msg);
    }

    public void recordAccess(String ip) {
        ipAccessCount.put(ip, ipAccessCount.getOrDefault(ip, 0) + 1);
    }

    public Map<String, Integer> getIpAccessCount() {
        return ipAccessCount;
    }
}