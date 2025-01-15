package kmusau.translator.DTOs.assignmentDTOs;

import java.util.Date;
import java.util.List;
import kmusau.translator.entity.AssignedSentencesEntity;
import kmusau.translator.entity.LanguageEntity;
import kmusau.translator.entity.SentenceEntity;
import kmusau.translator.entity.UsersEntity;
import kmusau.translator.enums.StatusTypes;

public class AssignmentDto {
    private Long assignmentId;

    private Date dateAssigned;

    private UsersEntity translator;

    private Long translatorId;

    private List<SentenceEntity> sentences;

    private Long sentenceId;

    private Long translateToLanguage;

    private UsersEntity assignedToReview;

    private Long assignedToReviewId;

    private StatusTypes translationStatus;

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setDateAssigned(Date dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public void setTranslator(UsersEntity translator) {
        this.translator = translator;
    }

    public void setTranslatorId(Long translatorId) {
        this.translatorId = translatorId;
    }

    public void setSentences(List<SentenceEntity> sentences) {
        this.sentences = sentences;
    }

    public void setSentenceId(Long sentenceId) {
        this.sentenceId = sentenceId;
    }

    public void setTranslateToLanguage(Long translateToLanguage) {
        this.translateToLanguage = translateToLanguage;
    }

    public void setAssignedToReview(UsersEntity assignedToReview) {
        this.assignedToReview = assignedToReview;
    }

    public void setAssignedToReviewId(Long assignedToReviewId) {
        this.assignedToReviewId = assignedToReviewId;
    }

    public void setTranslationStatus(StatusTypes translationStatus) {
        this.translationStatus = translationStatus;
    }

    public AssignmentDto() {}

    public AssignmentDto(Long assignmentId, Date dateAssigned, UsersEntity translator, Long translatorId, List<SentenceEntity> sentences, Long sentenceId, Long translateToLanguage, UsersEntity assignedToReview, Long assignedToReviewId, StatusTypes translationStatus) {
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

    public UsersEntity getTranslator() {
        return this.translator;
    }

    public Long getTranslatorId() {
        return this.translatorId;
    }

    public List<SentenceEntity> getSentences() {
        return this.sentences;
    }

    public Long getSentenceId() {
        return this.sentenceId;
    }

    public Long getTranslateToLanguage() {
        return this.translateToLanguage;
    }

    public UsersEntity getAssignedToReview() {
        return this.assignedToReview;
    }

    public Long getAssignedToReviewId() {
        return this.assignedToReviewId;
    }

    public StatusTypes getTranslationStatus() {
        return this.translationStatus;
    }

    public AssignedSentencesEntity DtoToEntity(AssignmentDto assignmentDto) {
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
            assignmentEntity.setTranslateToLanguage(new LanguageEntity(assignmentDto.getTranslateToLanguage()));
        if (assignmentDto.getAssignedToReviewId() != null)
            assignmentEntity.setAssignedToReviewId(assignmentDto.getAssignedToReviewId());
        if (assignmentDto.getAssignmentId() != null)
            assignmentEntity.setAssignmentId(assignmentEntity.getAssignmentId());
        if (assignmentDto.getTranslationStatus() != null)
            assignmentEntity.setTranslationStatus(assignmentDto.getTranslationStatus());
        return assignmentEntity;
    }
}
