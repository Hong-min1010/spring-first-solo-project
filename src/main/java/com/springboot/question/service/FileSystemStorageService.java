package com.springboot.question.service;

import com.springboot.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileSystemStorageService implements StorageService {
    private final Path rootLocation = Paths.get("C:\\output");;
    @Override
    public void store(MultipartFile file) {
        Path directory = Paths.get("C:\\output");
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to create directory.", e);
        }
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation.resolve(
                    Paths.get(file.getOriginalFilename())).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                log.info("# store coffee image!");
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }
}