package drlugha.translator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.enums.DeletionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sentences")
@SQLDelete(sql = "UPDATE sentences SET deletion_status = 1 WHERE sentence_id=?")
@Where(clause = "deletion_status=0")
public class SentenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sentenceId;

    private Date dateCreated;

    private String sentenceText;

    @Column(columnDefinition = "boolean default false")
    private boolean translatedToKikuyu;

    @Column(columnDefinition = "boolean default false")
    private boolean translatedToKimeru;

    @Column(name = "batch_no")
    private Long batchNo;

    private String audioLink;

    @JsonIgnore
    @JoinColumn(name = "batch_no", referencedColumnName = "batchNo", updatable = false, insertable = false)
    @ManyToOne()
    private BatchEntity batch;

    @JsonIgnore
    @OneToMany(mappedBy = "sentence")
    private List<TranslatedSentenceEntity> translatedTexts;

    @Column(columnDefinition = "int default 0", nullable = false)
    private DeletionStatus deletionStatus = DeletionStatus.NOT_DELETED;

    // Transient fields
    @Transient
    private Long translatedSentenceId;

    @Transient
    private String translatedText;

    @Transient
    private String audioUrl;

    public SentenceEntity(Long batchNo, String audioLink) {
        this.batchNo = batchNo;
        this.audioLink = audioLink;
        this.dateCreated = new Date();
    }

    // Constructor for including transient fields
    public SentenceEntity(Long sentenceId, String sentenceText, String audioLink, Long translatedSentenceId, String translatedText, String audioUrl) {
        this.sentenceId = sentenceId;
        this.sentenceText = sentenceText;
        this.audioLink = audioLink;
        this.translatedSentenceId = translatedSentenceId;
        this.translatedText = translatedText;
        this.audioUrl = audioUrl;
    }

    // Getters and setters for transient fields (ensure they are handled correctly)
    // It's important to note that transient fields are not persisted in the database

    // Getters and setters for other persistent fields
}

