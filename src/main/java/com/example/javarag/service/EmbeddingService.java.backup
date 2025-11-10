package com.example.javarag.service;

import com.example.javarag.config.AIConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Service pour générer des embeddings (représentations vectorielles) de textes.
 * 
 * Les embeddings sont des vecteurs de nombres qui représentent le sens sémantique d'un texte.
 * Des textes similaires auront des embeddings proches dans l'espace vectoriel.
 * 
 * C'est la première étape du RAG: transformer le texte en vecteurs pour pouvoir
 * faire des recherches de similarité.
 */
@Service
@Slf4j
public class EmbeddingService {
    
    private final WebClient webClient;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;
    
    public EmbeddingService(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.objectMapper = new ObjectMapper();
        
        // Configurer le client HTTP selon le provider
        if ("ollama".equals(aiConfig.getProvider())) {
            this.webClient = WebClient.builder()
                    .baseUrl(aiConfig.getOllama().getBaseUrl())
                    .build();
        } else {
            this.webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader("Authorization", "Bearer " + aiConfig.getOpenai().getApiKey())
                    .build();
        }
    }
    
    /**
     * Génère un embedding vectoriel pour un texte donné.
     * 
     * @param text Le texte à transformer en vecteur
     * @return Un tableau de nombres représentant le texte (embedding)
     */
    public List<Double> generateEmbedding(String text) {
        log.debug("Génération d'embedding pour le texte: {}", text.substring(0, Math.min(50, text.length())));
        
        try {
            if ("ollama".equals(aiConfig.getProvider())) {
                return generateOllamaEmbedding(text);
            } else {
                return generateOpenAIEmbedding(text);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'embedding", e);
            // En cas d'erreur, retourner un embedding factice pour la démo
            log.warn("Utilisation d'un embedding factice pour la démonstration");
            return generateMockEmbedding(text);
        }
    }
    
    /**
     * Génère un embedding avec Ollama (local)
     */
    private List<Double> generateOllamaEmbedding(String text) {
        String model = aiConfig.getOllama().getEmbeddingModel();
        
        Map<String, Object> request = Map.of(
                "model", model,
                "prompt", text
        );
        
        String response = webClient.post()
                .uri("/api/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return parseOllamaEmbedding(response);
    }
    
    /**
     * Génère un embedding avec OpenAI
     */
    private List<Double> generateOpenAIEmbedding(String text) {
        String model = aiConfig.getOpenai().getEmbeddingModel();
        
        Map<String, Object> request = Map.of(
                "model", model,
                "input", text
        );
        
        String response = webClient.post()
                .uri("/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return parseOpenAIEmbedding(response);
    }
    
    /**
     * Parse la réponse d'Ollama pour extraire l'embedding
     */
    private List<Double> parseOllamaEmbedding(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.get("embedding");
            
            return objectMapper.convertValue(embeddingNode, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
        } catch (Exception e) {
            log.error("Erreur lors du parsing de l'embedding Ollama", e);
            throw new RuntimeException("Impossible de parser l'embedding", e);
        }
    }
    
    /**
     * Parse la réponse d'OpenAI pour extraire l'embedding
     */
    private List<Double> parseOpenAIEmbedding(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.get("data").get(0).get("embedding");
            
            return objectMapper.convertValue(embeddingNode, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
        } catch (Exception e) {
            log.error("Erreur lors du parsing de l'embedding OpenAI", e);
            throw new RuntimeException("Impossible de parser l'embedding", e);
        }
    }
    
    /**
     * Génère un embedding factice basé sur le hash du texte (pour la démo uniquement)
     * Dans un vrai système, il faut TOUJOURS utiliser un vrai modèle d'embedding
     */
    private List<Double> generateMockEmbedding(String text) {
        // Créer un vecteur simple basé sur le texte (dimension 384 comme nomic-embed-text)
        int dimension = 384;
        Double[] embedding = new Double[dimension];
        
        int hash = text.hashCode();
        for (int i = 0; i < dimension; i++) {
            embedding[i] = Math.sin(hash + i) * 0.5;
        }
        
        return List.of(embedding);
    }
    
    /**
     * Calcule la similarité cosinus entre deux embeddings.
     * Retourne une valeur entre -1 et 1, où 1 signifie identique.
     * 
     * @param embedding1 Premier vecteur
     * @param embedding2 Second vecteur
     * @return Score de similarité entre -1 et 1
     */
    public double cosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1.size() != embedding2.size()) {
            throw new IllegalArgumentException("Les embeddings doivent avoir la même dimension");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += Math.pow(embedding1.get(i), 2);
            norm2 += Math.pow(embedding2.get(i), 2);
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
