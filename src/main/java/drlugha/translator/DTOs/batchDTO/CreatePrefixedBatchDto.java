package drlugha.translator.DTOs.batchDTO;

import drlugha.translator.DTOs.sentenceDTOs.CreateSentenceDTO;
import drlugha.translator.entity.BatchEntity;

import java.util.List;

public class CreatePrefixedBatchDto {
    private String description;
    private List<CreateSentenceDTO> sentences;

    // Getters and Setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CreateSentenceDTO> getSentences() {
        return sentences;
    }

    public void setSentences(List<CreateSentenceDTO> sentences) {
        this.sentences = sentences;
    }

    public BatchEntity dtoToEntity() {
        BatchEntity batchEntity = new BatchEntity();
        batchEntity.setDescription(this.description);
        // Set other fields if needed
        return batchEntity;
    }
}
