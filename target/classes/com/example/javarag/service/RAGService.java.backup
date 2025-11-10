package com.example.javarag.service;

import com.example.javarag.model.Document;
import com.example.javarag.repository.DocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service principal qui orchestre le processus RAG complet.
 * 
 * RAG = Retrieval Augmented Generation
 * 
 * Processus en 3 étapes:
 * 1. RETRIEVAL (Récupération): Trouve les documents pertinents dans la base
 * 2. AUGMENTATION: Construit un contexte enrichi avec ces documents
 * 3. GENERATION: Génère une réponse avec un LLM en utilisant ce contexte
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RAGService {
    
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final LLMService llmService;
    private final ObjectMapper objectMapper;
    
    /**
     * Indexe un nouveau document dans la base vectorielle.
     * 
     * Étapes:
     * 1. Génère l'embedding du contenu
     * 2. Stocke le document avec son embedding
     * 
     * @param content Le contenu textuel du document
     * @param source La source du document
     * @return Le document créé
     */
    @Transactional
    public Document indexDocument(String content, String source) {
        log.info("Indexation d'un nouveau document de source: {}", source);
        
        // Étape 1: Générer l'embedding du contenu
        List<Double> embedding = embeddingService.generateEmbedding(content);
        
        // Convertir l'embedding en JSON pour le stockage
        String embeddingJson;
        try {
            embeddingJson = objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la sérialisation de l'embedding", e);
        }
        
        // Étape 2: Créer et sauvegarder le document
        Document document = Document.builder()
                .content(content)
                .embedding(embeddingJson)
                .source(source)
                .build();
        
        Document savedDocument = documentRepository.save(document);
        log.info("Document indexé avec l'ID: {}", savedDocument.getId());
        
        return savedDocument;
    }
    
    /**
     * Répond à une question en utilisant le processus RAG complet.
     * 
     * @param question La question posée par l'utilisateur
     * @param topK Nombre de documents à récupérer (par défaut 3)
     * @return La réponse générée
     */
    public String answerQuestion(String question, int topK) {
        log.info("Traitement de la question: {}", question);
        
        // ÉTAPE 1: RETRIEVAL - Récupérer les documents pertinents
        List<Document> relevantDocuments = retrieveRelevantDocuments(question, topK);
        
        if (relevantDocuments.isEmpty()) {
            return "Aucun document trouvé dans la base. Veuillez d'abord indexer des documents.";
        }
        
        log.info("Trouvé {} documents pertinents", relevantDocuments.size());
        
        // ÉTAPE 2: AUGMENTATION - Construire le contexte
        String context = buildContext(relevantDocuments);
        
        // ÉTAPE 3: GENERATION - Générer la réponse
        String prompt = buildPrompt(context, question);
        String answer = llmService.generateResponse(prompt);
        
        log.info("Réponse générée avec succès");
        return answer;
    }
    
    /**
     * ÉTAPE 1 du RAG: Récupère les documents les plus pertinents pour la question.
     * 
     * Dans un vrai système avec pgvector, on utiliserait une requête SQL comme:
     * SELECT * FROM documents ORDER BY embedding <-> query_embedding LIMIT topK
     * 
     * Ici, pour la démo, on calcule manuellement la similarité cosinus.
     */
    private List<Document> retrieveRelevantDocuments(String question, int topK) {
        log.debug("Recherche des {} documents les plus pertinents", topK);
        
        // Générer l'embedding de la question
        List<Double> questionEmbedding = embeddingService.generateEmbedding(question);
        
        // Récupérer tous les documents (en prod, filtrer par distance vectorielle)
        List<Document> allDocuments = documentRepository.findAll();
        
        // Calculer la similarité avec chaque document et trier
        return allDocuments.stream()
                .map(doc -> {
                    try {
                        // Parser l'embedding du document
                        List<Double> docEmbedding = objectMapper.readValue(
                                doc.getEmbedding(),
                                new TypeReference<List<Double>>() {}
                        );
                        
                        // Calculer la similarité
                        double similarity = embeddingService.cosineSimilarity(
                                questionEmbedding,
                                docEmbedding
                        );
                        
                        // Créer un wrapper pour stocker le score
                        return new DocumentWithScore(doc, similarity);
                    } catch (Exception e) {
                        log.error("Erreur lors du calcul de similarité pour le document {}", doc.getId(), e);
                        return new DocumentWithScore(doc, 0.0);
                    }
                })
                .sorted(Comparator.comparingDouble(DocumentWithScore::getScore).reversed())
                .limit(topK)
                .peek(dws -> log.debug("Document {} - Similarité: {}", dws.getDocument().getId(), dws.getScore()))
                .map(DocumentWithScore::getDocument)
                .collect(Collectors.toList());
    }
    
    /**
     * ÉTAPE 2 du RAG: Construit le contexte à partir des documents récupérés.
     */
    private String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append("Document ").append(i + 1).append(":\n");
            context.append(doc.getContent()).append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * ÉTAPE 3 du RAG: Construit le prompt final pour le LLM.
     * 
     * Le prompt contient:
     * - Des instructions sur comment répondre
     * - Le contexte (documents pertinents)
     * - La question de l'utilisateur
     */
    private String buildPrompt(String context, String question) {
        return String.format("""
                Tu es un assistant qui répond à des questions en te basant uniquement sur le contexte fourni.
                
                Instructions:
                - Réponds uniquement en utilisant les informations du contexte
                - Si le contexte ne contient pas l'information, dis-le clairement
                - Sois précis et concis
                - Cite les documents quand c'est pertinent
                
                Contexte:
                %s
                
                Question: %s
                
                Réponse:
                """, context, question);
    }
    
    /**
     * Récupère tous les documents indexés
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    /**
     * Compte le nombre total de documents
     */
    public long countDocuments() {
        return documentRepository.count();
    }
    
    /**
     * Classe interne pour associer un document à son score de similarité
     */
    private static class DocumentWithScore {
        private final Document document;
        private final double score;
        
        public DocumentWithScore(Document document, double score) {
            this.document = document;
            this.score = score;
        }
        
        public Document getDocument() {
            return document;
        }
        
        public double getScore() {
            return score;
        }
    }
}
