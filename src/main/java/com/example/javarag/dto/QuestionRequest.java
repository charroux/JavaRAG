package com.example.javarag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour poser une question au système RAG
 */
@Data
public class QuestionRequest {
    
    @NotBlank(message = "La question ne peut pas être vide")
    private String question;
    
    /**
     * Nombre de documents à récupérer (par défaut 3)
     */
    private Integer topK = 3;
}
