package drlugha.translator.system.batch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.shared.enums.DeletionStatus;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.system.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "batch_details")
@Entity
@SQLDelete(sql = "UPDATE batch_details SET deletion_status = 1 WHERE batch_details_id=?")
@Where(clause = "deletion_status=0")
public class BatchDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchDetailsId;

    @JoinColumn(name = "language", referencedColumnName = "languageId")
    @ManyToOne
    private Language language;

    @Enumerated(EnumType.ORDINAL)
    private BatchStatus batchStatus;

    @Column(name = "batch_id")
    private Long batchId;

    @JoinColumn(name = "batch_id", referencedColumnName = "batchNo", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private BatchEntity batch;

    @JsonIgnore
    @OneToMany(mappedBy = "batchDetails")
    private List<TranslatedSentenceEntity> translatedSentence;

    @Column(name = "translated_by")
    private Long translatedById;

    @JoinColumn(name = "translated_by", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User translatedBy;

    @Column(name = "translation_verified_by_id")
    private Long translationVerifiedById;

    @JoinColumn(name = "translation_verified_by_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User translationVerifiedBy;

    @Column(name = "second_reviewer_id")
    private Long secondReviewerId;

    @JoinColumn(name = "second_reviewer_id", referencedColumnName = "userId", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User secondReviewer;

    @Column(name = "recorded_by_id")
    private Long recordedById;

    @JoinColumn(name = "recorded_by_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User recordedBy;

    @Column(name = "audio_verified_by_id")
    private Long audioVerifiedById;

    @JoinColumn(name = "audio_verified_by_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User audioVerifiedBy;

    @Column(columnDefinition = "int default 0", nullable = false)
    private DeletionStatus deletionStatus = DeletionStatus.NOT_DELETED;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "batchDetails")
    private List<BatchDetailsUserAssignment> batchDetailsUserAssignment = new ArrayList<>();

}
