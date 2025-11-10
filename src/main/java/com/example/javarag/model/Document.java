package com.example.javarag.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un document stocké dans la base de données vectorielle.
 * 
 * Chaque document contient:
 * - Le contenu textuel original
 * - Un embedding vectoriel (représentation numérique du texte)
 * - Des métadonnées (source, date, etc.)
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Le contenu textuel du document
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * L'embedding vectoriel du contenu.
     * C'est une représentation numérique du texte qui permet de faire des recherches sémantiques.
     * Stocké comme chaîne de caractères pour simplification (en prod, utiliser pgvector)
     */
    @Column(columnDefinition = "TEXT")
    private String embedding;
    
    /**
     * La source du document (URL, nom de fichier, etc.)
     */
    @Column
    private String source;
    
    /**
     * Métadonnées supplémentaires au format JSON
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Date de création du document
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
