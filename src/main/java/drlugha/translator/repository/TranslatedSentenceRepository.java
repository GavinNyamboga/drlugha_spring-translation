package drlugha.translator.repository;

import drlugha.translator.DTOs.stats.BatchDetailsStatsDto;
import drlugha.translator.entity.LanguageEntity;
import drlugha.translator.entity.SentenceEntity;
import drlugha.translator.entity.TranslatedSentenceEntity;
import drlugha.translator.enums.StatusTypes;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TranslatedSentenceRepository extends JpaRepository<TranslatedSentenceEntity, Long> {

    List<TranslatedSentenceEntity> findByReviewStatus(StatusTypes reviewStatus);

    @Query("select t from TranslatedSentenceEntity t where t.reviewStatus = 0 or t.reviewStatus = 2")
    List<TranslatedSentenceEntity> findByReviewStatusAndRecordedStatus();

    @Query("select t from TranslatedSentenceEntity t where t.reviewStatus = 1 and t.batchDetails.translationVerifiedById = :userId and t.batchDetailsId = :batchDetailsId")
    List<TranslatedSentenceEntity> findUnreviewedByTranslationVerifiedByIdAndBatchDetailsId(
            @Param("userId") Long userId,
            @Param("batchDetailsId") Long batchDetailsId
    );

    @Query("select t from TranslatedSentenceEntity t where (t.reviewStatus = 0 OR t.reviewStatus = 2) and t.batchDetails.translationVerifiedById = :userId and t.batchDetailsId = :batchDetailsId")
    List<TranslatedSentenceEntity> findReviewedByTranslationVerifiedByIdAndBatchDetailsId(
            @Param("userId") Long userId,
            @Param("batchDetailsId") Long batchDetailsId
    );


    @Query("select t from TranslatedSentenceEntity t where t.reviewStatus = 0 and t.secondReview = 1 and t.batchDetails.secondReviewerId = :userId and t.batchDetailsId = :batchDetailsId")
    List<TranslatedSentenceEntity> findExpertReviewersUnreviewedTasks(
            @Param("userId") Long userId,
            @Param("batchDetailsId") Long batchDetailsId
    );

    @Query(value = "SELECT v.file_url, v.user_id " +
            "FROM voice v " +
            "WHERE v.translated_sentence_id = :translatedSentenceId " +
            "ORDER BY v.voice_id DESC " +
            "LIMIT 1", nativeQuery = true)
    List<Object[]> fetchVoiceDetailsByTranslatedSentenceId(@Param("translatedSentenceId") Long translatedSentenceId);


    @Query("select t from TranslatedSentenceEntity t where (t.secondReview = 0 or t.secondReview = 2) and t.batchDetails.secondReviewerId = :userId and t.batchDetailsId = :batchDetailsId")
    List<TranslatedSentenceEntity> findExpertReviewersReviewedTasks(
            @Param("userId") Long userId,
            @Param("batchDetailsId") Long batchDetailsId
    );

    List<TranslatedSentenceEntity> findByBatchDetailsId(Long batchDetailsId, Sort sort);

    List<TranslatedSentenceEntity> findByBatchDetailsId(Long batchDetailsId);

    List<TranslatedSentenceEntity> findByReviewStatusAndAssignedTranslator(StatusTypes reviewStatus, Long userId);

    List<TranslatedSentenceEntity> findByRecordedStatusAndAssignedRecorderUserId(StatusTypes recordedStatus, Long userId);

    @Query("select count(*) from TranslatedSentenceEntity t where t.assignedTranslator = :assignedTranslator and t.dateCreated between :startDate and :endDate")
    Integer numberOfTranslatedSentencesByUser(
            @Param("assignedTranslator") Long assignedTranslator,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("select t from TranslatedSentenceEntity t where t.assignedTranslator = :assignedTranslator and t.reviewStatus =0 and t.dateCreated between :startDate and :endDate")
    List<TranslatedSentenceEntity> numberOfApprovedTranslatedSentencesByUser(
            @Param("assignedTranslator") Long assignedTranslator,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("select t from TranslatedSentenceEntity t where t.assignedTranslator = :assignedTranslator and t.reviewStatus =2 and t.dateCreated between :startDate and :endDate")
    List<TranslatedSentenceEntity> numberOfRejectedTranslatedSentencesByUser(
            @Param("assignedTranslator") Long assignedTranslator,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("select t from TranslatedSentenceEntity t where t.assignedTranslator = :assignedTranslator and t.reviewStatus =1 and t.dateCreated between :startDate and :endDate")
    List<TranslatedSentenceEntity> numberOfUnreviewedTranslatedSentencesByUser(
            @Param("assignedTranslator") Long assignedTranslator,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("select t from TranslatedSentenceEntity t where t.batchDetailsId = :batchDetailId  and (recorded_status is null OR recorded_status != 4)")
    List<TranslatedSentenceEntity> findUnrecordedVoiceTasks(@Param("batchDetailId") Long batchDetailId);

    Integer countAllByBatchDetailsIdAndReviewStatus(Long batchDetailsId, StatusTypes status);

    @Query("SELECT COUNT(t) FROM TranslatedSentenceEntity t WHERE t.batchDetailsId = :batchDetailsId AND " +
            "(t.reviewStatus = :firstReview OR (t.secondReview IS NOT NULL AND t.secondReview = :secondReview))")
    Integer countRejectedSentences(Long batchDetailsId, StatusTypes firstReview, StatusTypes secondReview);


    Integer countAllByBatchDetailsIdAndSecondReview(Long batchDetailsId, StatusTypes status);

    Integer countAllByBatchDetailsIdAndRecordedStatus(Long batchDetailsId, StatusTypes status);

    @Query("SELECT t.sentence FROM TranslatedSentenceEntity t WHERE t.sentence.batchNo = :batchNumber AND t.language = :language")
    List<SentenceEntity> findTranslatedSentences(Long batchNumber, LanguageEntity language);

    @Modifying
    @Query("UPDATE TranslatedSentenceEntity t  SET t.deletionStatus = 1 WHERE " +
            "t.batchDetailsId IN (SELECT b.batchDetailsId FROM BatchDetailsEntity b WHERE b.batch.batchNo = :batchNo)")
    void deleteAllByBatchNumber(Long batchNo);

    @Modifying
    @Query("UPDATE TranslatedSentenceEntity t SET t.secondReview = 1 WHERE t.translatedSentenceId IN :toReviewIds")
    void assignSentencesToExpertReviewer(List<Long> toReviewIds);

    @Query(value = "SELECT" +
            "    (SELECT COUNT(*) FROM translated_sentence WHERE batch_details_id = :batchDetailsId AND review_status = 0) AS moderatorApprovedSentences," +
            "    (SELECT COUNT(*) FROM translated_sentence WHERE batch_details_id = :batchDetailsId AND review_status = 2) AS moderatorRejectedSentences," +
            "    (SELECT COUNT(*) FROM translated_sentence WHERE batch_details_id = :batchDetailsId AND second_review = 0) AS expertApprovedSentences," +
            "    (SELECT COUNT(*) FROM translated_sentence WHERE batch_details_id = :batchDetailsId AND second_review = 2) AS expertRejectedSentences",
            nativeQuery = true)
    BatchDetailsStatsDto getBatchDetailsStats(Long batchDetailsId);

    List<TranslatedSentenceEntity> findAllBySentenceIdAndBatchDetailsId(Long sentenceId, Long batchDetailsId);

    @Modifying
    @Query(value = "UPDATE translated_sentence ts0 SET ts0.deletion_status = 1  WHERE ts0.translated_sentence_id NOT IN " +
            "(SELECT MAX(ts.translated_sentence_id) FROM (SELECT * FROM translated_sentence) ts GROUP BY ts.sentence_id, ts.batch_details_id HAVING COUNT(*) > 1) " +
            "AND ts0.translated_sentence_id NOT IN (SELECT MAX(ts1.translated_sentence_id) FROM (SELECT * FROM translated_sentence) ts1 WHERE ts1.deletion_status != 1 GROUP BY ts1.sentence_id, ts1.batch_details_id HAVING COUNT(*) = 1);", nativeQuery = true
    )
    void deleteDuplicateTranslations();

    void deleteAllByBatchDetailsBatchDetailsId(Long batchDetailsId);

    Integer countAllByBatchDetailsBatchDetailsId(Long batchDetailsId);


}


