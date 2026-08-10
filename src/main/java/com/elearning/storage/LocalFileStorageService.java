package com.elearning.storage;

import com.elearning.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String UPLOAD_ROOT = "uploads";

    @Override
    public String store(MultipartFile file, String subdirectory) {
        try {
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            // UUID làm tên file — tránh 2 người cùng upload "thumbnail.jpg" ghi
            // đè lên nhau, và tránh lộ tên file gốc trên máy người dùng.
            String filename = UUID.randomUUID() + (extension != null ? "." + extension : "");

            Path targetDir = Paths.get(UPLOAD_ROOT, subdirectory);
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(filename));

            return "/" + UPLOAD_ROOT + "/" + subdirectory + "/" + filename;
        } catch (IOException e) {
            throw new InvalidFileException("Không lưu được file: " + e.getMessage());
        }
    }
}