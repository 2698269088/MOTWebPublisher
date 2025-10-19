// src/top/mcocet/config/ConfigLoader.java
package top.mcocet.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ConfigLoader {
    private final String configPath;
    private int port = 80;
    private String logFilePath = "run.log";
    private String documentRoot = "http";
    private String indexFile = "index.html";
    private String errorFile = "50x.html";
    private Set<String> blacklistedIPs = new HashSet<>();
    private Set<String> allowedHosts = new HashSet<>();
    private boolean enableSecurity = false;

    public ConfigLoader(String configPath) {
        this.configPath = configPath;
        load();
    }

    private void load() {
        Path path = Paths.get(configPath);
        if (!Files.exists(path)) {
            createDefaultConfig(path);
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("port:")) {
                    port = Integer.parseInt(line.split(":")[1].trim());
                } else if (line.startsWith("logFile:")) {
                    logFilePath = line.split(":")[1].trim();
                } else if (line.startsWith("documentRoot:")) {
                    documentRoot = line.split(":")[1].trim();
                } else if (line.startsWith("indexFile:")) {
                    indexFile = line.split(":")[1].trim();
                } else if (line.startsWith("errorFile:")) {
                    errorFile = line.split(":")[1].trim();
                } else if (line.startsWith("blacklist:")) {
                    String[] ips = line.split(":")[1].trim().split(",");
                    blacklistedIPs = new HashSet<>();
                    for (String ip : ips) {
                        blacklistedIPs.add(ip.trim());
                    }
                } else if (line.startsWith("allowedHosts:")) {
                    String[] hosts = line.split(":")[1].trim().split(",");
                    allowedHosts = new HashSet<>();
                    for (String host : hosts) {
                        allowedHosts.add(host.trim());
                    }
                } else if (line.startsWith("enableSecurity:")) {
                    enableSecurity = Boolean.parseBoolean(line.split(":")[1].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("配置文件读取失败，使用默认值: " + e.getMessage());
        }
    }

    private void createDefaultConfig(Path path) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            writer.println("port: 80");
            writer.println("logFile: run.log");
            writer.println("documentRoot: http");
            writer.println("indexFile: index.html");
            writer.println("errorFile: 50x.html");
            writer.println("blacklist: 192.168.1.1,192.168.1.2");
            writer.println("allowedHosts: example.com,192.168.1.100");
            writer.println("enableSecurity: true");
        } catch (IOException e) {
            System.err.println("创建默认配置失败: " + e.getMessage());
        }
    }

    public synchronized void addBlacklistedIP(String ip) {
        blacklistedIPs.add(ip);
        saveConfig();
        reloadBlacklist();
    }

    public synchronized void removeBlacklistedIP(String ip) {
        blacklistedIPs.remove(ip);
        saveConfig();
        reloadBlacklist();
    }

    private void saveConfig() {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(configPath)))) {
            writer.println("port: " + port);
            writer.println("logFile: " + logFilePath);
            writer.println("documentRoot: " + documentRoot);
            writer.println("indexFile: " + indexFile);
            writer.println("errorFile: " + errorFile);
            writer.print("blacklist: ");
            writer.println(String.join(",", blacklistedIPs));
            writer.print("allowedHosts: ");
            writer.println(String.join(",", allowedHosts));
            writer.println("enableSecurity: " + enableSecurity);
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
        }
    }

    public synchronized void reloadBlacklist() {
        load();
    }

    public Set<String> getBlacklistedIPs() {
        return blacklistedIPs;
    }

    // Getters
    public int getPort() { return port; }
    public String getLogFilePath() { return logFilePath; }
    public String getDocumentRoot() { return documentRoot; }
    public String getIndexFile() { return indexFile; }
    public String getErrorFile() { return errorFile; }
    public Set<String> getAllowedHosts() { return allowedHosts; }
    public boolean isEnableSecurity() { return enableSecurity; }
}