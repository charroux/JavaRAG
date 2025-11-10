package com.example.javarag.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration LangChain4j pour les modèles d'IA.
 * 
 * Cette configuration crée automatiquement les beans nécessaires
 * pour utiliser Ollama ou OpenAI selon la configuration.
 */
@Configuration
@RequiredArgsConstructor
public class LangChain4jConfig {
    
    private final AIConfig aiConfig;
    
    /**
     * Bean pour le modèle d'embedding (transformation texte → vecteur).
     * LangChain4j gère automatiquement la communication avec Ollama/OpenAI.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        if ("ollama".equals(aiConfig.getProvider())) {
            return OllamaEmbeddingModel.builder()
                    .baseUrl(aiConfig.getOllama().getBaseUrl())
                    .modelName(aiConfig.getOllama().getEmbeddingModel())
                    .timeout(Duration.ofMinutes(2))
                    .build();
        } else {
            return OpenAiEmbeddingModel.builder()
                    .apiKey(aiConfig.getOpenai().getApiKey())
                    .modelName(aiConfig.getOpenai().getEmbeddingModel())
                    .timeout(Duration.ofMinutes(1))
                    .build();
        }
    }
    
    /**
     * Bean pour le modèle de chat (génération de réponses).
     * LangChain4j gère automatiquement le format des requêtes/réponses.
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if ("ollama".equals(aiConfig.getProvider())) {
            return OllamaChatModel.builder()
                    .baseUrl(aiConfig.getOllama().getBaseUrl())
                    .modelName(aiConfig.getOllama().getChatModel())
                    .timeout(Duration.ofMinutes(2))
                    .build();
        } else {
            return OpenAiChatModel.builder()
                    .apiKey(aiConfig.getOpenai().getApiKey())
                    .modelName(aiConfig.getOpenai().getChatModel())
                    .timeout(Duration.ofMinutes(1))
                    .build();
        }
    }
}
