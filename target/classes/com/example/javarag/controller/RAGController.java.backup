package com.example.javarag.controller;

import com.example.javarag.dto.IndexDocumentRequest;
import com.example.javarag.dto.QuestionRequest;
import com.example.javarag.dto.QuestionResponse;
import com.example.javarag.model.Document;
import com.example.javarag.service.RAGService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour l'API RAG.
 * 
 * Endpoints disponibles:
 * - POST /api/documents : Indexer un nouveau document
 * - GET /api/documents : Récupérer tous les documents
 * - POST /api/ask : Poser une question au système RAG
 * - GET /api/stats : Obtenir des statistiques sur la base de documents
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RAGController {
    
    private final RAGService ragService;
    
    /**
     * Indexe un nouveau document dans la base vectorielle.
     * 
     * Exemple de requête:
     * POST /api/documents
     * {
     *   "content": "Java est un langage de programmation orienté objet...",
     *   "source": "wikipedia-java"
     * }
     */
    @PostMapping("/documents")
    public ResponseEntity<Document> indexDocument(@Valid @RequestBody IndexDocumentRequest request) {
        log.info("Requête d'indexation reçue pour la source: {}", request.getSource());
        
        Document document = ragService.indexDocument(
                request.getContent(),
                request.getSource()
        );
        
        return ResponseEntity.ok(document);
    }
    
    /**
     * Récupère tous les documents indexés.
     * 
     * GET /api/documents
     */
    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = ragService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Pose une question au système RAG.
     * 
     * Le système va:
     * 1. Chercher les documents les plus pertinents
     * 2. Construire un contexte avec ces documents
     * 3. Générer une réponse avec un LLM
     * 
     * Exemple de requête:
     * POST /api/ask
     * {
     *   "question": "Qu'est-ce que Java ?",
     *   "topK": 3
     * }
     */
    @PostMapping("/ask")
    public ResponseEntity<QuestionResponse> askQuestion(@Valid @RequestBody QuestionRequest request) {
        log.info("Question reçue: {}", request.getQuestion());
        
        int topK = request.getTopK() != null ? request.getTopK() : 3;
        String answer = ragService.answerQuestion(request.getQuestion(), topK);
        
        QuestionResponse response = QuestionResponse.builder()
                .question(request.getQuestion())
                .answer(answer)
                .documentsUsed(topK)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtient des statistiques sur la base de documents.
     * 
     * GET /api/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long documentCount = ragService.countDocuments();
        
        Map<String, Object> stats = Map.of(
                "totalDocuments", documentCount,
                "status", documentCount > 0 ? "ready" : "empty"
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Endpoint de santé pour vérifier que l'API fonctionne.
     * 
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Java RAG API"
        ));
    }
}
