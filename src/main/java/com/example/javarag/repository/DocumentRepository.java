package com.example.javarag.repository;

import com.example.javarag.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour gérer les documents dans la base de données.
 * 
 * Dans un projet RAG réel, ce repository inclurait des requêtes de recherche vectorielle
 * utilisant pgvector pour trouver les documents les plus similaires à une requête.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    /**
     * Recherche des documents par source
     */
    List<Document> findBySource(String source);
    
    /**
     * Recherche tous les documents (pour la démonstration)
     * Dans un vrai système RAG, on utiliserait une recherche vectorielle:
     * SELECT * FROM documents ORDER BY embedding <-> query_vector LIMIT k
     */
    @Query("SELECT d FROM Document d ORDER BY d.createdAt DESC")
    List<Document> findAllOrderByCreatedAtDesc();
    
    /**
     * Compte le nombre total de documents
     */
    long count();
}
