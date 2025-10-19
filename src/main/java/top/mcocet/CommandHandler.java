// src/top/mcocet/CommandHandler.java
package top.mcocet;

import top.mcocet.config.ConfigLoader;
import top.mcocet.service.LoggerService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

public class CommandHandler implements Runnable {
    private final ConfigLoader config;
    private final LoggerService logger;

    public CommandHandler(ConfigLoader config, LoggerService logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            while ((command = reader.readLine()) != null) {
                String[] parts = command.split(" ");
                if ("list".equalsIgnoreCase(parts[0])) {
                    listAccessRecords();
                } else if ("add".equalsIgnoreCase(parts[0]) && parts.length == 2) {
                    addBlacklistedIP(parts[1]);
                } else if ("remove".equalsIgnoreCase(parts[0]) && parts.length == 2) {
                    removeBlacklistedIP(parts[1]);
                } else if ("help".equalsIgnoreCase(parts[0])) {
                    showHelp();
                } else {
                    System.out.println("未知命令: " + command);
                }
            }
        } catch (Exception e) {
            System.err.println("控制台指令处理失败: " + e.getMessage());
        }
    }

    private void listAccessRecords() {
        Map<String, Integer> ipAccessCount = logger.getIpAccessCount();
        System.out.println("访问记录：");
        for (Map.Entry<String, Integer> entry : ipAccessCount.entrySet()) {
            System.out.println("IP: " + entry.getKey() + ", 访问次数: " + entry.getValue());
        }
    }

    private void addBlacklistedIP(String ip) {
        config.addBlacklistedIP(ip);
        System.out.println("IP " + ip + " 已加入黑名单");
    }

    private void removeBlacklistedIP(String ip) {
        config.removeBlacklistedIP(ip);
        System.out.println("IP " + ip + " 已从黑名单中移除");
    }

    private void showHelp() {
        System.out.println("可用命令：");
        System.out.println("  list - 显示所有HTML页面的访问记录");
        System.out.println("  add <IP> - 将指定IP地址加入黑名单");
        System.out.println("  remove <IP> - 从黑名单中移除指定IP地址");
        System.out.println("  help - 显示帮助信息");
    }
}