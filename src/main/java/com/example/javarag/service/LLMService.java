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
 * Service pour générer du texte avec un LLM (Large Language Model).
 * 
 * Ce service prend un prompt (qui inclut le contexte récupéré et la question)
 * et génère une réponse en langage naturel.
 * 
 * C'est la partie "Generation" du RAG.
 */
@Service
@Slf4j
public class LLMService {
    
    private final WebClient webClient;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;
    
    public LLMService(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.objectMapper = new ObjectMapper();
        
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
     * Génère une réponse à partir d'un prompt.
     * 
     * @param prompt Le prompt qui contient les instructions, le contexte et la question
     * @return La réponse générée par le LLM
     */
    public String generateResponse(String prompt) {
        log.debug("Génération de réponse pour le prompt");
        
        try {
            if ("ollama".equals(aiConfig.getProvider())) {
                return generateOllamaResponse(prompt);
            } else {
                return generateOpenAIResponse(prompt);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la génération de la réponse", e);
            return "Erreur: Impossible de générer une réponse. Assurez-vous que " + 
                   aiConfig.getProvider() + " est correctement configuré.";
        }
    }
    
    /**
     * Génère une réponse avec Ollama
     */
    private String generateOllamaResponse(String prompt) {
        String model = aiConfig.getOllama().getChatModel();
        
        Map<String, Object> request = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );
        
        String response = webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return parseOllamaResponse(response);
    }
    
    /**
     * Génère une réponse avec OpenAI
     */
    private String generateOpenAIResponse(String prompt) {
        String model = aiConfig.getOpenai().getChatModel();
        
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );
        
        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(message)
        );
        
        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return parseOpenAIResponse(response);
    }
    
    /**
     * Parse la réponse d'Ollama
     */
    private String parseOllamaResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.get("response").asText();
        } catch (Exception e) {
            log.error("Erreur lors du parsing de la réponse Ollama", e);
            throw new RuntimeException("Impossible de parser la réponse", e);
        }
    }
    
    /**
     * Parse la réponse d'OpenAI
     */
    private String parseOpenAIResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            log.error("Erreur lors du parsing de la réponse OpenAI", e);
            throw new RuntimeException("Impossible de parser la réponse", e);
        }
    }
}
