package kmusau.translator.entity;

import kmusau.translator.enums.DeletionStatus;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Table(name = "batch_details_stats")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SQLDelete(sql = "UPDATE batch_details_stats SET deletion_status = 1 WHERE batch_details_id=?")
@Where(clause = "deletion_status=0")
public class BatchDetailsStatsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statsId;

    @OneToOne
    @JoinColumn(name = "batch_details_id", referencedColumnName = "batchDetailsId", unique = true)
    private BatchDetailsEntity batchDetails;

    private int sentencesTranslated;

    private int sentencesApproved;

    private int sentencesRejected;

    private int sentencesExpertApproved;

    private int sentencesExpertRejected;

    private int audiosRecorded;

    private int audiosApproved;

    private int audiosRejected;

    @Column(columnDefinition = "int default 0", nullable = false)
    private DeletionStatus deletionStatus = DeletionStatus.NOT_DELETED;
}
