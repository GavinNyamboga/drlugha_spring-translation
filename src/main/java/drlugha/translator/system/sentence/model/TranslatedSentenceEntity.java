package drlugha.translator.system.sentence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.shared.enums.DeletionStatus;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.system.sentence.SentenceStatus;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.voice.model.VoiceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "translated_sentence",
        indexes = {
                @Index(name = "idx_translated_sentence", columnList = "translated_sentence_id, sentence_id, batch_details_id," +
                        " deletion_status, review_status, second_review")
        })
@SQLDelete(sql = "UPDATE translated_sentence SET deletion_status = 1 WHERE translated_sentence_id=?")
@Where(clause = "deletion_status=0")
public class TranslatedSentenceEntity {

    @Id
    @Column(name = "translated_sentence_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long translatedSentenceId;

    @CreationTimestamp
    private Date dateCreated;

    @UpdateTimestamp
    private Date dateModified;

    private String translatedText;

    @JoinColumn(name = "language", referencedColumnName = "language_id")
    @ManyToOne
    private Language language;

    @Column(name = "sentence_id")
    private Long sentenceId;

    @JoinColumn(name = "sentence_id", referencedColumnName = "sentence_id", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Sentence sentence;

    @Column(name = "batch_details_id")
    private Long batchDetailsId;

    @JoinColumn(name = "batch_details_id", referencedColumnName = "batch_details_id", updatable = false, insertable = false)
    @ManyToOne
    private BatchDetailsEntity batchDetails;

    // TODO: To be removed
    private Long assignedTranslator;

    @Column(name = "review_status")
    @Enumerated(EnumType.ORDINAL)
    private StatusTypes reviewStatus;

    private int seconds;

    @ColumnDefault("1")
    @Column(name = "second_review")
    @Enumerated(EnumType.ORDINAL)
    private StatusTypes secondReview;

    @ColumnDefault("3")
    @Column(name = "recorded_status")
    @Enumerated(EnumType.ORDINAL)
    private StatusTypes recordedStatus;

    // TODO: To be removed
    private Long assignedToReview;

    // TODO: To be removed
    @Column(name = "assigned_recorder_id")
    private Long assignedRecorderId;

    // TODO: To be removed
    @JoinColumn(name = "assigned_recorder_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedRecorder;

    // TODO: To be removed
    @Column(name = "assigned_audio_reviewer_id")
    private Long assignedAudioReviewerId;

    // TODO: To be removed
    @JoinColumn(name = "assigned_audio_reviewer_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedAudioReviewer;

    @JoinColumn(name = "recorded_by", referencedColumnName = "userId")
    @ManyToOne(fetch = FetchType.LAZY)
    private User recordedBy;

    @JsonIgnore
    @OneToMany(mappedBy = "translatedSentence")
    private List<VoiceEntity> voice;

    @OneToMany(mappedBy = "translatedSentence", fetch = FetchType.LAZY)
    private List<CorrectedTranslatedSentences> correctedSentences;

    @Column(name = "deletion_status", columnDefinition = "int default 0", nullable = false)
    private DeletionStatus deletionStatus = DeletionStatus.NOT_DELETED;

    @Column
    @Enumerated(EnumType.STRING)
    private SentenceStatus sentenceStatus = SentenceStatus.OK;
}


