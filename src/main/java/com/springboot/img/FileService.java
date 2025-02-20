package com.springboot.img;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    // 현재 프로젝트 디렉토리 기준으로 업로드 폴더 설정함
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String BASE_URL = "http://localhost:8080/uploads/";

    public List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(this::saveFile)
                .collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file) {
        try {
            String fileType = file.getContentType();
            if (fileType == null ||
                    // 경로 설정
                    (!fileType.equals("image/jpeg") &&
                    !fileType.equals("image/png")&&
                    !fileType.equals("image/gif"))) {
                throw new IllegalStateException("지원하지 않는 파일 형식입니다.");
            }

            // 중복 방지
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get("C:/Users/qza73/OneDrive/Desktop/새 폴더/" + fileName);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            return "file:///" + filePath.toString().replace("\\", "/");
        }
        catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
