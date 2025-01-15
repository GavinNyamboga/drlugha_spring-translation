package kmusau.translator.entity;

import kmusau.translator.enums.DeletionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "translated_sentence_logs")
@SQLDelete(sql = "UPDATE translated_sentence_logs SET deletion_status = 1 WHERE log_id=?")
@Where(clause = "deletion_status=0")
public class TranslatedSentenceLogsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @OneToOne
    @JoinColumn(name = "translated_sentence_id")
    private TranslatedSentenceEntity translatedSentence;

    private Date dateModerated;

    private Date dateExpertModerated;

    private Date dateAudioModerated;

    @Column(columnDefinition = "int default 0", nullable = false)
    private DeletionStatus deletionStatus = DeletionStatus.NOT_DELETED;


}
