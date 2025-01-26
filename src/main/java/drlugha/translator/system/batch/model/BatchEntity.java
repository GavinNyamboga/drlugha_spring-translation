package drlugha.translator.system.batch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.shared.enums.DeletionStatus;
import drlugha.translator.shared.enums.YesNo;
import drlugha.translator.system.language.model.Language;
import drlugha.translator.system.user.model.User;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "batches")
@SQLDelete(sql = "UPDATE batches SET deletion_status = 1 WHERE batch_no=?")
@Where(clause = "deletion_status=0")
public class BatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchNo;

    private String source;

    private String linkUrl;

    @Column(length = 1200)
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "batch")
    private List<BatchDetailsEntity> batchDetails;

    @OneToMany(mappedBy = "batchNo", fetch = FetchType.LAZY)
    private List<Sentence> sentences;

    @Enumerated(EnumType.STRING)
    @NonNull
    @Column(name = "batch_type", columnDefinition = "VARCHAR(255) NOT NULL DEFAULT 'TEXT'")
    private BatchType batchType = BatchType.TEXT;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language audioLanguage;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @JoinColumn(name = "uploader_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User uploader;

    @Column(columnDefinition = "int default 0", nullable = false)
    private DeletionStatus deletionStatus = DeletionStatus.NOT_DELETED;

    @JoinColumn(name = "source_language", referencedColumnName = "languageId")
    @ManyToOne
    private Language sourceLanguage;

    @JoinColumn(name = "target_language", referencedColumnName = "languageId")
    @ManyToOne
    private Language targetLanguage;

    @Column
    @Enumerated(EnumType.STRING)
    private YesNo fromFeedback = YesNo.NO;

    public BatchEntity(String source, String linkUrl, String description, BatchType batchType, Language language) {
        this.source = source;
        this.linkUrl = linkUrl;
        this.description = description;
        this.batchType = batchType;
        this.audioLanguage = language;
    }
}
