package com.example.service;

import cn.hutool.core.io.FileUtil;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    private static final String FILE_PATH = System.getProperty("user.dir") + "/files/";

    public boolean deleteFileByUrl(String fileUrl) {
        if (fileUrl == null) return false;

        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName != null) {
            String fullPath = FILE_PATH + fileName;
            return FileUtil.del(fullPath);
        }
        return false;
    }

    public String extractFileNameFromUrl(String url) {
        // 从URL中提取文件名，例如从"http://localhost:8080/files/download/filename.jpg"中提取"filename.jpg"
        if (url != null) {
            int lastSlashIndex = url.lastIndexOf("/");
            if (lastSlashIndex >= 0) {
                return url.substring(lastSlashIndex + 1);
            }
        }
        return null;
    }
}