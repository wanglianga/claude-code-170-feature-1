package com.community.eldercare.controller;

import com.community.eldercare.common.ApiException;
import com.community.eldercare.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * 现场照片 / 语音文件上传
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ApiException("文件为空");
        }
        String original = file.getOriginalFilename() == null ? "f" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot);
        }
        String dateDir = LocalDate.now().toString();
        Path dir = Paths.get(uploadDir, dateDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Files.copy(file.getInputStream(), dir.resolve(filename));
        String url = "/uploads/" + dateDir + "/" + filename;
        return R.ok(Map.of("url", url, "name", original));
    }
}
