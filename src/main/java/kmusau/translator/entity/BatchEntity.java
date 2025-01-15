package kmusau.translator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kmusau.translator.enums.BatchType;
import kmusau.translator.enums.DeletionStatus;
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
    private List<SentenceEntity> sentences;

    @Enumerated(EnumType.STRING)
    @NonNull
    @Column(name = "batch_type", columnDefinition = "VARCHAR(255) NOT NULL DEFAULT 'TEXT'")
    private BatchType batchType = BatchType.TEXT;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private LanguageEntity audioLanguage;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @JoinColumn(name = "uploader_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UsersEntity uploader;

    @Column(columnDefinition = "int default 0", nullable = false)
    private DeletionStatus deletionStatus = DeletionStatus.NOT_DELETED;

    public BatchEntity(String source, String linkUrl, String description, BatchType batchType, LanguageEntity language) {
        this.source = source;
        this.linkUrl = linkUrl;
        this.description = description;
        this.batchType = batchType;
        this.audioLanguage = language;
    }
}
