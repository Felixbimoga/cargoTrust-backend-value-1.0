package com.gargotrust.gestion_achats_enligne.shared.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Service de stockage de fichiers — concern transverse partagé par tous les modules
 * (photos de profil, logos/couvertures transitaire, documents KYC, preuves, …).
 */
public interface StorageService {

    /** Stocke une image (JPEG/PNG/WebP, max 5 Mo) et retourne l'URL publique. */
    String store(MultipartFile file, String folder);

    /**
     * Stocke un fichier selon une politique explicite (types MIME autorisés + taille max)
     * et retourne l'URL publique. Utilisé notamment pour les documents (PDF + images).
     */
    String store(MultipartFile file, String folder, Set<String> allowedContentTypes, long maxBytes);

    /** Supprime le fichier à partir de son URL publique. */
    void delete(String fileUrl);
}
