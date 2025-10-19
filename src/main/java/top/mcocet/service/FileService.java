// src/top/mcocet/service/FileService.java
package top.mcocet.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileService {
    private final Path documentRoot;
    private final String indexFile;
    private final String errorFile;
    private final LoggerService logger;

    public FileService(String documentRoot, String indexFile, String errorFile, LoggerService logger) {
        this.documentRoot = Paths.get(documentRoot).toAbsolutePath().normalize();
        this.indexFile = indexFile;
        this.errorFile = errorFile;
        this.logger = logger;
    }

    public Optional<Path> resolveFilePath(String uri) {
        if ("/".equals(uri)) {
            uri = "/" + indexFile;
        }

        Path requestedPath = documentRoot.resolve(uri.substring(1)).normalize();

        // 安全检查：防止目录遍历
        if (!requestedPath.startsWith(documentRoot)) {
            logger.warning("尝试路径遍历: " + uri);
            return Optional.empty();
        }

        if (Files.exists(requestedPath) && Files.isRegularFile(requestedPath)) {
            return Optional.of(requestedPath);
        }

        return Optional.empty();
    }

    public Optional<String> getMimeType(Path path) {
        try {
            String type = Files.probeContentType(path);
            return Optional.ofNullable(type != null ? type : "application/octet-stream");
        } catch (IOException e) {
            logger.warning("无法探测 MIME 类型: " + path.getFileName() + ", 错误: " + e.getMessage());
            return Optional.of("application/octet-stream");
        }
    }

    public byte[] readErrorPage() {
        Path errorPath = documentRoot.resolve(errorFile);
        if (Files.exists(errorPath)) {
            try {
                return Files.readAllBytes(errorPath);
            } catch (IOException e) {
                logger.warning("读取错误页面失败: " + e.getMessage());
            }
        }
        return ("<html><body><h1>500 Internal Error</h1></body></html>").getBytes();
    }

    public Path getDocumentRoot() {
        return documentRoot;
    }
}