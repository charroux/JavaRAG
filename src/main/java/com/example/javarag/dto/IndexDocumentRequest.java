package com.example.javarag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour indexer un nouveau document
 */
@Data
public class IndexDocumentRequest {
    
    @NotBlank(message = "Le contenu ne peut pas être vide")
    private String content;
    
    private String source;
}
