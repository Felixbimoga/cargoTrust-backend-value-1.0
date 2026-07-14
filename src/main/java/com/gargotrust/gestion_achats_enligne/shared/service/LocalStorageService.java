package com.gargotrust.gestion_achats_enligne.shared.service;

import com.gargotrust.gestion_achats_enligne.shared.exception.StorageException;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.strategy", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageService implements StorageService {

    private static final long IMAGE_MAX_BYTES = 5 * 1024 * 1024L;               // 5 Mo
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    /** Extensions par type MIME pour nommer le fichier stocké. */
    private static final Map<String, String> EXTENSION_BY_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png",  "png",
            "image/webp", "webp",
            "application/pdf", "pdf");

    @Value("${app.storage.local.base-path:./uploads}")
    private String basePath;

    @Value("${app.storage.local.base-url:http://localhost:8081/cargo-trust-api/uploads}")
    private String baseUrl;

    @Override
    public String store(MultipartFile file, String folder) {
        return store(file, folder, IMAGE_TYPES, IMAGE_MAX_BYTES);
    }

    @Override
    public String store(MultipartFile file, String folder, Set<String> allowedContentTypes, long maxBytes) {
        String contentType = validate(file, allowedContentTypes, maxBytes);

        String ext      = extensionFor(contentType, file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + ext;
        Path   dir      = Paths.get(basePath, folder);

        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Erreur stockage fichier", e);
            throw StorageException.failure();
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

    private String validate(MultipartFile file, Set<String> allowedTypes, long maxBytes) {
        if (file == null || file.isEmpty()) throw StorageException.invalidFormat();
        if (file.getSize() > maxBytes)      throw StorageException.fileTooLarge();
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw StorageException.invalidFormat();
        }
        return contentType;
    }

    private String extensionFor(String contentType, String originalFilename) {
        String byType = EXTENSION_BY_TYPE.get(contentType);
        if (byType != null) return byType;
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        return "bin";
    }
}
