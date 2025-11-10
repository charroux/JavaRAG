package com.example.javarag.controller;

import com.example.javarag.dto.IndexDocumentRequest;
import com.example.javarag.dto.QuestionRequest;
import com.example.javarag.dto.QuestionResponse;
import com.example.javarag.model.Document;
import com.example.javarag.service.LangChain4jRAGService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour l'API RAG avec LangChain4j.
 * 
 * Les endpoints restent les mêmes, seule l'implémentation sous-jacente change.
 * Utilise LangChain4jRAGService au lieu de RAGService.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LangChain4jRAGController {
    
    private final LangChain4jRAGService ragService;
    
    /**
     * Indexe un nouveau document dans la base vectorielle.
     */
    @PostMapping("/documents")
    public Document indexDocument(@Valid @RequestBody IndexDocumentRequest request) {
        log.info("Requête d'indexation reçue (LangChain4j) - source: {}", request.getSource());
        
        return ragService.indexDocument(
                request.getContent(),
                request.getSource()
        );
    }
    
    /**
     * Récupère tous les documents indexés.
     */
    @GetMapping("/documents")
    public List<Document> getAllDocuments() {
        return ragService.getAllDocuments();
    }
    
    /**
     * Pose une question au système RAG avec LangChain4j.
     */
    @PostMapping("/ask")
    public QuestionResponse askQuestion(@Valid @RequestBody QuestionRequest request) {
        log.info("Question reçue (LangChain4j): {}", request.getQuestion());
        
        int topK = request.getTopK() != null ? request.getTopK() : 3;
        String answer = ragService.answerQuestion(request.getQuestion(), topK);
        
        return QuestionResponse.builder()
                .question(request.getQuestion())
                .answer(answer)
                .documentsUsed(topK)
                .build();
    }
    
    /**
     * Obtient des statistiques sur la base de documents.
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        long documentCount = ragService.countDocuments();
        
        return Map.of(
                "totalDocuments", documentCount,
                "status", documentCount > 0 ? "ready" : "empty",
                "implementation", "LangChain4j"
        );
    }
    
    /**
     * Endpoint de santé.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "Java RAG API with LangChain4j"
        );
    }
}
