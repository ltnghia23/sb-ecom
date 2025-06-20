package com.fortune.project.service.filestorage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService{
    @Override
    public String uploadImage(String path, MultipartFile image) throws IOException {
        String contentType = image.getContentType();
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        String originalFileName = image.getOriginalFilename();
        String randomId = UUID.randomUUID().toString();
        String newFileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));
        String filePath = path + File.separator + newFileName;

        File folder = new File(path);
        if (!folder.exists()) folder.mkdirs();

        Files.copy(image.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }
}
