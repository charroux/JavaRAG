package com.example.javarag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse à une question
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    
    /**
     * La réponse générée par le système RAG
     */
    private String answer;
    
    /**
     * La question posée
     */
    private String question;
    
    /**
     * Nombre de documents utilisés pour générer la réponse
     */
    private int documentsUsed;
}
