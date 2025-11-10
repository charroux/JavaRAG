package com.example.javarag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour les services d'IA (Ollama ou OpenAI)
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AIConfig {
    
    private String provider = "ollama"; // "ollama" ou "openai"
    
    private OllamaConfig ollama = new OllamaConfig();
    private OpenAIConfig openai = new OpenAIConfig();
    
    @Data
    public static class OllamaConfig {
        private String baseUrl = "http://localhost:11434";
        private String embeddingModel = "nomic-embed-text";
        private String chatModel = "llama3.2";
    }
    
    @Data
    public static class OpenAIConfig {
        private String apiKey;
        private String embeddingModel = "text-embedding-3-small";
        private String chatModel = "gpt-3.5-turbo";
    }
}
