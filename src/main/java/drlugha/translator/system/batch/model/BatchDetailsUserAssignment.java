package drlugha.translator.system.batch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.shared.model.BaseEntity;
import drlugha.translator.system.batch.enums.UserBatchRole;
import drlugha.translator.system.user.model.User;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "batch_user_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "batch_details_id", "batch_role"})},
        indexes = {
                @Index(name = "idx_batch_user_assignments", columnList = "batch_details_id, batch_role, user_id")
        })
public class BatchDetailsUserAssignment extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_details_id", insertable = false, updatable = false)
    private BatchDetailsEntity batchDetails;

    @Column(name = "batch_details_id")
    private Long batchDetailsId;

    @Column
    private Long batchId;

    @Column(name = "user_id")
    private Long userId;

    @JoinColumn(name = "user_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "batch_role")
    @Enumerated(EnumType.STRING)
    private UserBatchRole batchRole;

}
