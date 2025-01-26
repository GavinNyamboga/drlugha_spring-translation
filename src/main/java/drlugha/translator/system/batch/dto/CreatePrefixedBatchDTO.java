package drlugha.translator.system.batch.dto;

import drlugha.translator.system.sentence.dto.CreateSentenceDTO;
import drlugha.translator.system.batch.model.BatchEntity;

import java.util.List;

public class CreatePrefixedBatchDTO {
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
