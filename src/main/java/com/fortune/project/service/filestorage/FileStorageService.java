package com.fortune.project.service.filestorage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String uploadImage(String path, MultipartFile image) throws IOException;
}
