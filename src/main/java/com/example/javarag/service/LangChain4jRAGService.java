package com.example.javarag.service;

import com.example.javarag.model.Document;
import com.example.javarag.repository.DocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service RAG utilisant LangChain4j.
 * 
 * Comparé à l'implémentation manuelle, LangChain4j simplifie:
 * - La communication avec les LLMs (pas besoin de gérer HTTP/JSON)
 * - La gestion des embeddings (format standardisé)
 * - La construction des prompts (helpers intégrés)
 * 
 * Le processus RAG reste le même:
 * 1. RETRIEVAL: Chercher les documents pertinents
 * 2. AUGMENTATION: Construire le contexte
 * 3. GENERATION: Générer la réponse avec le LLM
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LangChain4jRAGService {
    
    private final DocumentRepository documentRepository;
    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    
    /**
     * Indexe un document en utilisant LangChain4j.
     * 
     * LangChain4j simplifie:
     * - La génération d'embeddings (1 ligne au lieu de gérer HTTP)
     * - La gestion des erreurs
     */
    @Transactional
    public Document indexDocument(String content, String source) {
        log.info("Indexation d'un document avec LangChain4j - source: {}", source);
        
        // LangChain4j génère l'embedding de manière simplifiée
        TextSegment segment = TextSegment.from(content);
        Response<Embedding> response = embeddingModel.embed(segment);
        Embedding embedding = response.content();
        
        // Convertir l'embedding en JSON pour le stockage
        String embeddingJson;
        try {
            embeddingJson = objectMapper.writeValueAsString(embedding.vectorAsList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la sérialisation de l'embedding", e);
        }
        
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
     * Répond à une question en utilisant le processus RAG avec LangChain4j.
     * 
     * LangChain4j simplifie:
     * - La génération de l'embedding de la question
     * - L'envoi du prompt au LLM (gestion automatique du format)
     * - La gestion des erreurs
     */
    public String answerQuestion(String question, int topK) {
        log.info("Traitement de la question avec LangChain4j: {}", question);
        
        // ÉTAPE 1: RETRIEVAL - Récupérer les documents pertinents
        List<Document> relevantDocuments = retrieveRelevantDocuments(question, topK);
        
        if (relevantDocuments.isEmpty()) {
            return "Aucun document trouvé dans la base. Veuillez d'abord indexer des documents.";
        }
        
        log.info("Trouvé {} documents pertinents", relevantDocuments.size());
        
        // ÉTAPE 2: AUGMENTATION - Construire le contexte
        String context = buildContext(relevantDocuments);
        
        // ÉTAPE 3: GENERATION - Générer la réponse avec LangChain4j
        String prompt = buildPrompt(context, question);
        
        try {
            // LangChain4j gère automatiquement la communication avec le LLM
            Response<AiMessage> response = chatModel.generate(UserMessage.from(prompt));
            String answer = response.content().text();
            
            log.info("Réponse générée avec succès");
            return answer;
        } catch (Exception e) {
            log.error("Erreur lors de la génération de la réponse", e);
            return "Erreur: Impossible de générer une réponse. " + e.getMessage();
        }
    }
    
    /**
     * Récupère les documents les plus pertinents pour la question.
     * LangChain4j simplifie la génération de l'embedding de la question.
     */
    private List<Document> retrieveRelevantDocuments(String question, int topK) {
        log.debug("Recherche des {} documents les plus pertinents", topK);
        
        // Générer l'embedding de la question avec LangChain4j (simplifié!)
        TextSegment questionSegment = TextSegment.from(question);
        Response<Embedding> response = embeddingModel.embed(questionSegment);
        Embedding questionEmbedding = response.content();
        List<Float> questionVector = questionEmbedding.vectorAsList();
        
        // Récupérer tous les documents
        List<Document> allDocuments = documentRepository.findAll();
        
        // Calculer la similarité avec chaque document et trier
        return allDocuments.stream()
                .map(doc -> {
                    try {
                        // Parser l'embedding du document
                        List<Double> docEmbedding = objectMapper.readValue(
                                doc.getEmbedding(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)
                        );
                        
                        // Calculer la similarité cosinus
                        double similarity = cosineSimilarity(questionVector, docEmbedding);
                        
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
     * Calcule la similarité cosinus entre deux vecteurs.
     */
    private double cosineSimilarity(List<Float> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Les vecteurs doivent avoir la même dimension");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += Math.pow(vec1.get(i), 2);
            norm2 += Math.pow(vec2.get(i), 2);
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * Construit le contexte à partir des documents récupérés.
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
     * Construit le prompt final pour le LLM.
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
     * Récupère tous les documents indexés.
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    /**
     * Compte le nombre total de documents.
     */
    public long countDocuments() {
        return documentRepository.count();
    }
    
    /**
     * Classe interne pour associer un document à son score de similarité.
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
