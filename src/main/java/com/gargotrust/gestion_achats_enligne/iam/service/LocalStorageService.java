package com.gargotrust.gestion_achats_enligne.iam.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.strategy", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageService implements StorageService {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024L; // 5 Mo
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${app.storage.local.base-path:./uploads}")
    private String basePath;

    @Value("${app.storage.local.base-url:http://localhost:8081/cargo-trust-api/uploads}")
    private String baseUrl;

    @Override
    public String store(MultipartFile file, String folder) {
        validate(file);

        String ext      = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + ext;
        Path   dir      = Paths.get(basePath, folder);

        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Erreur stockage fichier", e);
            throw new RuntimeException("ERR_STORAGE_FAILURE");
        }

        return baseUrl + "/" + folder + "/" + filename;
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) return;
        String relativePath = fileUrl.substring(baseUrl.length());
        Path target = Paths.get(basePath + relativePath);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier : {}", target);
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) throw new IamException(IamException.PHOTO_INVALID_FORMAT);
        if (file.getSize() > MAX_SIZE_BYTES) throw new IamException(IamException.PHOTO_TOO_LARGE);
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IamException(IamException.PHOTO_INVALID_FORMAT);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
