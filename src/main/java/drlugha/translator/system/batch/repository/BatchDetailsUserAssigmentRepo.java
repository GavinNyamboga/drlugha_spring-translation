package drlugha.translator.system.batch.repository;


import drlugha.translator.system.batch.enums.UserBatchRole;
import drlugha.translator.system.batch.model.BatchDetailsUserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchDetailsUserAssigmentRepo extends JpaRepository<BatchDetailsUserAssignment, Long> {

    List<BatchDetailsUserAssignment> findByBatchDetailsIdAndBatchRole(Long batchDetailsId, UserBatchRole userBatchRole);


    @Modifying
    @Query("DELETE FROM BatchDetailsUserAssignment b WHERE b.batchDetailsId=:batchDetailsId and b.batchRole=:role and b.userId not in :userIds")
    void deleteAllByBatchDetailsIdAndBatchRoleAndNotInUserIds(Long batchDetailsId, UserBatchRole role, List<Long> userIds);

    List<BatchDetailsUserAssignment> findByUserIdAndBatchRole(Long userId, UserBatchRole userBatchRole);

    @Query("SELECT u FROM BatchDetailsUserAssignment u WHERE u.userId=:userId AND u.batchRole=:userBatchRole AND u.batchDetailsId=:batchDetailsId")
    List<BatchDetailsUserAssignment> findByUserIdAndBatchRoleAndBatchDetails_BatchDetailsId(Long userId, UserBatchRole userBatchRole,
                                                                                            Long batchDetailsId);
}
