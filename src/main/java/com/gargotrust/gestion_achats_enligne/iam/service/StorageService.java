package com.gargotrust.gestion_achats_enligne.iam.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /** Stocke le fichier et retourne l'URL publique. */
    String store(MultipartFile file, String folder);
    /** Supprime le fichier à partir de son URL publique. */
    void delete(String fileUrl);
}
