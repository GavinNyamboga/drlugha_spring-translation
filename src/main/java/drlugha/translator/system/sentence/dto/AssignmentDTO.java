package drlugha.translator.system.sentence.dto;

import drlugha.translator.system.sentence.model.AssignedSentencesEntity;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.user.model.User;
import drlugha.translator.shared.enums.StatusTypes;

import java.util.Date;
import java.util.List;

public class AssignmentDTO {
    private Long assignmentId;

    private Date dateAssigned;

    private User translator;

    private Long translatorId;

    private List<Sentence> sentences;

    private Long sentenceId;

    private Long translateToLanguage;

    private User assignedToReview;

    private Long assignedToReviewId;

    private StatusTypes translationStatus;

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setDateAssigned(Date dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public void setTranslator(User translator) {
        this.translator = translator;
    }

    public void setTranslatorId(Long translatorId) {
        this.translatorId = translatorId;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public void setSentenceId(Long sentenceId) {
        this.sentenceId = sentenceId;
    }

    public void setTranslateToLanguage(Long translateToLanguage) {
        this.translateToLanguage = translateToLanguage;
    }

    public void setAssignedToReview(User assignedToReview) {
        this.assignedToReview = assignedToReview;
    }

    public void setAssignedToReviewId(Long assignedToReviewId) {
        this.assignedToReviewId = assignedToReviewId;
    }

    public void setTranslationStatus(StatusTypes translationStatus) {
        this.translationStatus = translationStatus;
    }

    public AssignmentDTO() {
    }

    public AssignmentDTO(Long assignmentId, Date dateAssigned, User translator, Long translatorId, List<Sentence> sentences, Long sentenceId, Long translateToLanguage, User assignedToReview, Long assignedToReviewId, StatusTypes translationStatus) {
        this.assignmentId = assignmentId;
        this.dateAssigned = dateAssigned;
        this.translator = translator;
        this.translatorId = translatorId;
        this.sentences = sentences;
        this.sentenceId = sentenceId;
        this.translateToLanguage = translateToLanguage;
        this.assignedToReview = assignedToReview;
        this.assignedToReviewId = assignedToReviewId;
        this.translationStatus = translationStatus;
    }

    public Long getAssignmentId() {
        return this.assignmentId;
    }

    public Date getDateAssigned() {
        return this.dateAssigned;
    }

    public User getTranslator() {
        return this.translator;
    }

    public Long getTranslatorId() {
        return this.translatorId;
    }

    public List<Sentence> getSentences() {
        return this.sentences;
    }

    public Long getSentenceId() {
        return this.sentenceId;
    }

    public Long getTranslateToLanguage() {
        return this.translateToLanguage;
    }

    public User getAssignedToReview() {
        return this.assignedToReview;
    }

    public Long getAssignedToReviewId() {
        return this.assignedToReviewId;
    }

    public StatusTypes getTranslationStatus() {
        return this.translationStatus;
    }

    public AssignedSentencesEntity DtoToEntity(AssignmentDTO assignmentDto) {
        AssignedSentencesEntity assignmentEntity = new AssignedSentencesEntity();
        if (assignmentDto == null)
            return assignmentEntity;
        if (assignmentDto.getTranslatorId() != null)
            assignmentEntity.setTranslatorId(assignmentDto.getTranslatorId());
        if (assignmentDto.getDateAssigned() != null)
            assignmentEntity.setDateAssigned(assignmentDto.getDateAssigned());
        if (assignmentDto.getSentenceId() != null)
            assignmentEntity.setSentenceId(assignmentDto.getSentenceId());
        if (assignmentDto.getTranslateToLanguage() != null)
            assignmentEntity.setTranslateToLanguage(new Language(assignmentDto.getTranslateToLanguage()));
        if (assignmentDto.getAssignedToReviewId() != null)
            assignmentEntity.setAssignedToReviewId(assignmentDto.getAssignedToReviewId());
        if (assignmentDto.getAssignmentId() != null)
            assignmentEntity.setAssignmentId(assignmentEntity.getAssignmentId());
        if (assignmentDto.getTranslationStatus() != null)
            assignmentEntity.setTranslationStatus(assignmentDto.getTranslationStatus());
        return assignmentEntity;
    }
}
