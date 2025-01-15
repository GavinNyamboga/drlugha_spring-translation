package kmusau.translator.DTOs.batchDTO;

import kmusau.translator.DTOs.sentenceDTOs.CreateSentenceDto;
import kmusau.translator.entity.BatchEntity;

import java.util.List;

public class CreatePrefixedBatchDto {
    private String description;
    private List<CreateSentenceDto> sentences;

    // Getters and Setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CreateSentenceDto> getSentences() {
        return sentences;
    }

    public void setSentences(List<CreateSentenceDto> sentences) {
        this.sentences = sentences;
    }

    public BatchEntity dtoToEntity() {
        BatchEntity batchEntity = new BatchEntity();
        batchEntity.setDescription(this.description);
        // Set other fields if needed
        return batchEntity;
    }
}
