package drlugha.translator.system.batch.repository;

import drlugha.translator.shared.enums.YesNo;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.system.batch.model.BatchDetailsStatsEntity;
import drlugha.translator.system.batch.projections.*;
import drlugha.translator.system.stats.dto.BatchDetailsStatsMapping;
import drlugha.translator.system.stats.dto.TotalUserStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BatchDetailsStatsRepository extends JpaRepository<BatchDetailsStatsEntity, Long> {
    Optional<BatchDetailsStatsEntity> findByBatchDetailsBatchDetailsId(Long batchDetailsId);

    @Query(value = "SELECT * FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on b.batch_no = bd.batch_id " +
            "CROSS JOIN users u on u.user_id = bd.translated_by " +
            "WHERE u.user_id = :userId AND bd.batch_details_id IN (SELECT batch_details_id FROM batch_details " +
            "WHERE translated_by = :userId AND batch_details.deletion_status = 0 AND b.batch_type = :batchType);", nativeQuery = true)
    List<BatchDetailsStatsEntity> findTranslatorStatsPerBatchDetails(Long userId, String batchType);

    @Query(value = "SELECT * FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on b.batch_no = bd.batch_id " +
            "CROSS JOIN users u on u.user_id = bd.translation_verified_by_id " +
            "WHERE u.user_id = :userId AND bd.batch_details_id IN (SELECT batch_details_id FROM batch_details " +
            "WHERE translation_verified_by_id = :userId AND batch_details.deletion_status = 0 AND b.batch_type = :batchType);", nativeQuery = true)
    List<BatchDetailsStatsEntity> findModeratorStatsPerBatchDetails(Long userId, String batchType);

    @Query(value = "SELECT * FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on b.batch_no = bd.batch_id " +
            "CROSS JOIN users u on u.user_id = bd.second_reviewer_id " +
            "WHERE u.user_id = :userId AND bd.batch_details_id IN (SELECT batch_details_id FROM batch_details " +
            "WHERE bd.second_reviewer_id = :userId AND batch_details.deletion_status = 0 AND b.batch_type = :batchType);", nativeQuery = true)
    List<BatchDetailsStatsEntity> findExpertStatsPerBatchDetails(Long userId, String batchType);

    @Query(value = "SELECT * FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN users u on u.user_id = bd.recorded_by_id " +
            "WHERE u.user_id = :userId AND bd.batch_details_id IN (SELECT batch_details_id FROM batch_details " +
            "WHERE bd.recorded_by_id = :userId AND batch_details.deletion_status = 0);", nativeQuery = true)
    List<BatchDetailsStatsEntity> findRecorderStatsPerBatchDetails(Long userId);

    @Query(value = "SELECT * FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN users u on u.user_id = bd.audio_verified_by_id " +
            "WHERE u.user_id = :userId AND bd.batch_details_id IN (SELECT batch_details_id FROM batch_details " +
            "WHERE bd.audio_verified_by_id = :userId AND batch_details.deletion_status = 0);", nativeQuery = true)
    List<BatchDetailsStatsEntity> findAudioModeratorStatsPerBatchDetails(Long userId);

    @Query(value = "SELECT SUM(sentences_translated) as sentencesTranslated, SUM(sentences_approved) as sentencesApproved, SUM(sentences_rejected) as sentencesRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on b.batch_no = bd.batch_id " +
            "CROSS JOIN users u on u.user_id = bd.translated_by " +
            "WHERE u.user_id = :userId AND b.batch_type = :batchType", nativeQuery = true)
    TranslatorStats findTranslatorStats(Long userId, String batchType);

    @Query(value = "SELECT SUM(sentences_approved) as sentencesApproved, SUM(sentences_rejected) as sentencesRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on b.batch_no = bd.batch_id " +
            "CROSS JOIN users u on u.user_id = bd.translation_verified_by_id " +
            "WHERE u.user_id = :userId AND b.batch_type = :batchType", nativeQuery = true)
    ModeratorStats findModeratorStats(Long userId, String batchType);

    @Query(value = "SELECT SUM(sentences_expert_approved) as sentencesExpertApproved, SUM(sentences_expert_rejected) as sentencesExpertRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on b.batch_no = bd.batch_id " +
            "CROSS JOIN users u on u.user_id = bd.second_reviewer_id " +
            "WHERE u.user_id = :userId AND b.batch_type = :batchType", nativeQuery = true)
    ExpertStats findExpertsStats(Long userId, String batchType);

    @Query(value = "SELECT SUM(audios_recorded) as audiosRecorded, SUM(audios_approved) as audiosApproved, SUM(batch_details_stats.audios_rejected) as audiosRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN users u on u.user_id = bd.recorded_by_id " +
            "WHERE u.user_id = :userId", nativeQuery = true)
    RecorderStats findRecorderStats(Long userId);

    @Query(value = "SELECT SUM(audios_approved) as audiosApproved, SUM(batch_details_stats.audios_rejected) as audiosRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN users u on u.user_id = bd.audio_verified_by_id " +
            "WHERE u.user_id = :userId", nativeQuery = true)
    AudioModeratorStats findAudioModeratorStats(Long userId);

    @Query(value = "SELECT u.user_id as userId,  SUM(sentences_translated) as sentencesTranslated, SUM(sentences_approved) as sentencesApproved, SUM(sentences_rejected) as sentencesRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "CROSS JOIN users u on u.user_id = bd.translated_by WHERE b.batch_type = :batchType GROUP BY user_id", nativeQuery = true)
    List<TranslatorStats> findAllTranslatorsStats(String batchType);

    @Query(value = "SELECT u.user_id as userId,  SUM(sentences_approved) as sentencesApproved, SUM(sentences_rejected) as sentencesRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "CROSS JOIN users u on u.user_id = bd.translation_verified_by_id WHERE b.batch_type = :batchType GROUP BY user_id", nativeQuery = true)
    List<ModeratorStats> findAllModeratorStats(String batchType);

    @Query(value = "SELECT u.user_id as userId,  SUM(sentences_expert_approved) as sentencesExpertApproved, SUM(sentences_expert_rejected) as sentencesExpertRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "CROSS JOIN users u on u.user_id = bd.second_reviewer_id WHERE b.batch_type = :batchType  GROUP BY user_id", nativeQuery = true)
    List<ExpertStats> findAllExpertStats(String batchType);

    @Query(value = "SELECT u.user_id as userId,  SUM(audios_recorded) as audiosRecorded, SUM(audios_approved) as audiosApproved, SUM(batch_details_stats.audios_rejected) as audiosRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "CROSS JOIN users u on u.user_id = bd.recorded_by_id WHERE b.batch_type = :batchType  GROUP BY user_id", nativeQuery = true)
    List<RecorderStats> findAllRecorderStats(String batchType);

    @Query(value = "SELECT u.user_id as userId,  SUM(audios_approved) as audiosApproved, SUM(batch_details_stats.audios_rejected) as audiosRejected FROM batch_details_stats " +
            "CROSS JOIN batch_details bd on bd.batch_details_id = batch_details_stats.batch_details_id " +
            "CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "CROSS JOIN users u on u.user_id = bd.audio_verified_by_id WHERE b.batch_type = :batchType GROUP BY user_id", nativeQuery = true)
    List<AudioModeratorStats> findAllAudioModeratorStats(String batchType);

    @Modifying
    @Query("UPDATE BatchDetailsStatsEntity b SET b.deletionStatus = 1 WHERE b.batchDetails.batchDetailsId = :batchDetailsId")
    void deleteAllByBatchDetailsBatchDetailsId(Long batchDetailsId);

    @Query(value = "SELECT u.user_id AS userId, u.username AS username, " +
            "       ( " +
            "           SELECT count(ts.date_created) FROM users us " +
            "                                                 CROSS JOIN batch_details bd on us.user_id = bd.translated_by " +
            "                                                 CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "                                                 CROSS JOIN translated_sentence ts on bd.batch_details_id = ts.batch_details_id " +
            "           WHERE us.user_id = u.user_id AND ts.date_created BETWEEN CONVERT_TZ(:startDate, :serverTimeZone, '+03:00') AND CONVERT_TZ(:endDate, :serverTimeZone, '+03:00') AND b.batch_type = :batchType " +
            "        ) AS sentencesTranslated, " +
            "       ( " +
            "           SELECT count(tsl.date_moderated) FROM users us " +
            "                                                 CROSS JOIN batch_details bd on us.user_id = bd.translation_verified_by_id " +
            "                                                 CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "                                                 CROSS JOIN translated_sentence ts on bd.batch_details_id = ts.batch_details_id " +
            "                                                 CROSS JOIN translated_sentence_logs tsl on ts.translated_sentence_id = tsl.translated_sentence_id " +
            "           WHERE us.user_id = u.user_id AND tsl.date_moderated BETWEEN CONVERT_TZ(:startDate, :serverTimeZone, '+03:00') AND CONVERT_TZ(:endDate, :serverTimeZone, '+03:00') AND b.batch_type = :batchType " +
            "       ) AS sentencesModerated, " +
            "       ( " +
            "           SELECT count(tsl.date_expert_moderated) FROM users us " +
            "                                                 CROSS JOIN batch_details bd on us.user_id = bd.second_reviewer_id " +
            "                                                 CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "                                                 CROSS JOIN translated_sentence ts on bd.batch_details_id = ts.batch_details_id " +
            "                                                 CROSS JOIN translated_sentence_logs tsl on ts.translated_sentence_id = tsl.translated_sentence_id " +
            "           WHERE us.user_id = u.user_id AND tsl.date_expert_moderated BETWEEN CONVERT_TZ(:startDate, :serverTimeZone, '+03:00') AND CONVERT_TZ(:endDate, :serverTimeZone, '+03:00') AND b.batch_type = :batchType " +
            "       ) AS sentencesExpertModerated, " +
            "       ( " +
            "           SELECT COUNT(v.date_created) FROM users us " +
            "                                            CROSS JOIN batch_details bd on us.user_id = bd.recorded_by_id " +
            "                                            CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "                                            CROSS JOIN translated_sentence t on bd.batch_details_id = t.batch_details_id " +
            "                                            CROSS JOIN voice v on t.translated_sentence_id = v.translated_sentence_id " +
            "           WHERE us.user_id = u.user_id AND v.date_created BETWEEN CONVERT_TZ(:startDate, :serverTimeZone, '+03:00') AND CONVERT_TZ(:endDate, :serverTimeZone, '+03:00') AND b.batch_type = :batchType " +
            "       ) as audiosRecorded, " +
            "       ( " +
            "           SELECT count(tsl.date_audio_moderated) FROM users us " +
            "                                                        CROSS JOIN batch_details bd on us.user_id = bd.audio_verified_by_id " +
            "                                                        CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "                                                        CROSS JOIN translated_sentence ts on bd.batch_details_id = ts.batch_details_id " +
            "                                                        CROSS JOIN translated_sentence_logs tsl on ts.translated_sentence_id = tsl.translated_sentence_id " +
            "           WHERE us.user_id = u.user_id AND tsl.date_audio_moderated BETWEEN CONVERT_TZ(:startDate, :serverTimeZone, '+03:00') AND CONVERT_TZ(:endDate, :serverTimeZone, '+03:00') AND b.batch_type = :batchType " +
            "       ) AS audioModerated " +
            "FROM users u ORDER BY u.user_id", nativeQuery = true)
    List<TotalUserStatsDto> getTotalUserStats(String batchType, Date startDate, Date endDate, String serverTimeZone);


    @Modifying
    @Query(value = "UPDATE batch_details_stats SET deletion_status = 1 WHERE batch_details_id IN (SELECT batch_details_id FROM batch_details WHERE batch_id = :batchId )", nativeQuery = true)
    void deleteAllByBatchId(Long batchId);

    @Query("SELECT b FROM BatchDetailsStatsEntity b WHERE b.batchDetails.batch.batchType = :batchType ")
    List<BatchDetailsStatsEntity> findAllByBatchType(BatchType batchType, Sort by);

    @Query("SELECT b FROM BatchDetailsStatsEntity b WHERE b.batchDetails.batch.batchType = :batchType and b.batchDetails.batch.fromFeedback=:fromFeedback")
    List<BatchDetailsStatsEntity> findAllByBatchTypeAndFromFeedback(BatchType batchType, YesNo fromFeedback, Sort by);

    @Query(value = "WITH SentenceStats AS (SELECT bd.batch_details_id," +
            "                              COUNT(s.sentence_id)                AS numberOfSentences," +
            "                              COUNT(ts.translated_sentence_id)    AS sentencesTranslated," +
            "                              SUM(IF(ts.review_status = 0, 1, 0)) AS sentencesApproved," +
            "                              SUM(IF(ts.review_status = 2, 1, 0)) AS sentencesRejected," +
            "                              SUM(IF(ts.second_review = 0, 1, 0)) AS sentencesExpertApproved," +
            "                              SUM(IF(ts.second_review = 2, 1, 0)) AS sentencesExpertRejected" +
            "                       FROM batch_details bd" +
            "                                JOIN batches b ON bd.batch_id = b.batch_no" +
            "                                JOIN sentences s ON s.batch_no = b.batch_no AND s.deletion_status = 0" +
            "                                LEFT JOIN translated_sentence ts" +
            "                                          ON s.sentence_id = ts.sentence_id" +
            "                                              AND ts.batch_details_id = bd.batch_details_id" +
            "                                              AND ts.deletion_status = 0" +
            "                       WHERE b.deletion_status = 0" +
            "                         AND b.batch_type = 'TEXT'" +
            "                         AND b.from_feedback = 'NO'" +
            "                         AND bd.deletion_status = 0" +
            "                       GROUP BY bd.batch_details_id)," +
            "     UserAssignments AS (SELECT ua.batch_details_id," +
            "                                MAX(CASE WHEN ua.batch_role = 'TEXT_TRANSLATOR' THEN u.username END)      AS translator," +
            "                                MAX(CASE WHEN ua.batch_role = 'TEXT_VERIFIER' THEN u.username END)        AS moderator," +
            "                                MAX(CASE WHEN ua.batch_role = 'EXPERT_TEXT_REVIEWER' THEN u.username END) AS expert," +
            "                                MAX(CASE WHEN ua.batch_role = 'AUDIO_VERIFIER' THEN u.username END)       AS audioModerator," +
            "                                MAX(CASE WHEN ua.batch_role = 'AUDIO_RECORDER' THEN u.username END)       AS recorder" +
            "                         FROM batch_user_assignments ua" +
            "                                  LEFT JOIN users u ON ua.user_id = u.user_id" +
            "                         WHERE ua.batch_role IN" +
            "                               ('TEXT_TRANSLATOR', 'TEXT_VERIFIER', 'EXPERT_TEXT_REVIEWER', 'AUDIO_VERIFIER'," +
            "                                'AUDIO_RECORDER')" +
            "                         GROUP BY ua.batch_details_id)," +
            "     AudioStats AS (SELECT batch_details_id," +
            "                           COUNT(voice_id)           AS audiosRecorded," +
            "                           SUM(IF(status = 0, 1, 0)) AS audiosApproved," +
            "                           SUM(IF(status = 2, 1, 0)) AS audiosRejected" +
            "                    FROM voice" +
            "                    WHERE batch_details_id IS NOT NULL" +
            "                    GROUP BY batch_details_id) " +
            "SELECT SQL_CALC_FOUND_ROWS d.batch_details_id                      AS batchDetailsId," +
            "       b.batch_no                              AS batchNo," +
            "       b.source                                AS source," +
            "       l.name                                  AS 'language'," +
            "       d.batch_status                          AS status," +
            "       COALESCE(ss.numberOfSentences, 0)       AS numberOfSentences," +
            "       COALESCE(ss.sentencesTranslated, 0)     AS sentencesTranslated," +
            "       COALESCE(ss.sentencesApproved, 0)       AS sentencesApproved," +
            "       COALESCE(ss.sentencesRejected, 0)       AS sentencesRejected," +
            "       COALESCE(ss.sentencesExpertApproved, 0) AS sentencesExpertApproved," +
            "       COALESCE(ss.sentencesExpertRejected, 0) AS sentencesExpertRejected," +
            "       COALESCE(aud.audiosRecorded, 0)         AS audiosRecorded," +
            "       COALESCE(aud.audiosApproved, 0)         AS audiosApproved," +
            "       COALESCE(aud.audiosRejected, 0)         AS audiosRejected," +
            "       ua.translator," +
            "       ua.moderator," +
            "       ua.expert," +
            "       ua.recorder," +
            "       ua.audioModerator," +
            "       COUNT(*) OVER() AS total_count " + // This is crucial for pagination
            "FROM batch_details d" +
            "         JOIN batches b ON d.batch_id = b.batch_no" +
            "         JOIN languages l ON d.language = l.language_id" +
            "         LEFT JOIN SentenceStats ss ON d.batch_details_id = ss.batch_details_id" +
            "         LEFT JOIN UserAssignments ua ON d.batch_details_id = ua.batch_details_id" +
            "         LEFT JOIN AudioStats aud ON d.batch_details_id = aud.batch_details_id " +
            "WHERE b.deletion_status = 0" +
            "  AND d.deletion_status = 0" +
            "  AND b.batch_type = 'TEXT'" +
            "  AND (:languageId IS NULL OR d.language = :languageId) " +
            "  AND (:status IS NULL OR d.batch_status = :status) " +
            "  AND b.from_feedback = 'NO' " +
            "ORDER BY d.batch_id " +
            "LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}",
            countQuery = "SELECT FOUND_ROWS()", nativeQuery = true)
    Page<BatchDetailsStatsMapping> getBatchDetailsStatsText(Pageable pageable, @Param("languageId") Long languageId,
                                                            @Param("status") Integer status);

    @Query(value = "WITH SentenceStats AS (SELECT bd.batch_details_id," +
            "                              COUNT(s.sentence_id)                AS numberOfSentences," +
            "                              COUNT(ts.translated_sentence_id)    AS sentencesTranslated," +
            "                              SUM(IF(ts.review_status = 0, 1, 0)) AS sentencesApproved," +
            "                              SUM(IF(ts.review_status = 2, 1, 0)) AS sentencesRejected," +
            "                              SUM(IF(ts.second_review = 0, 1, 0)) AS sentencesExpertApproved," +
            "                              SUM(IF(ts.second_review = 2, 1, 0)) AS sentencesExpertRejected" +
            "                       FROM batch_details bd" +
            "                                JOIN batches b ON bd.batch_id = b.batch_no" +
            "                                JOIN sentences s ON s.batch_no = b.batch_no AND s.deletion_status = 0" +
            "                                LEFT JOIN translated_sentence ts" +
            "                                          ON s.sentence_id = ts.sentence_id" +
            "                                              AND ts.batch_details_id = bd.batch_details_id" +
            "                                              AND ts.deletion_status = 0" +
            "                       WHERE b.deletion_status = 0" +
            "                         AND b.batch_type = 'TEXT'" +
            "                         AND b.from_feedback = 'NO'" +
            "                         AND bd.deletion_status = 0" +
            "                       GROUP BY bd.batch_details_id)," +
            "     UserAssignments AS (SELECT ua.batch_details_id," +
            "                                MAX(CASE WHEN ua.batch_role = 'TEXT_TRANSLATOR' THEN u.username END)      AS translator," +
            "                                MAX(CASE WHEN ua.batch_role = 'TEXT_VERIFIER' THEN u.username END)        AS moderator," +
            "                                MAX(CASE WHEN ua.batch_role = 'EXPERT_TEXT_REVIEWER' THEN u.username END) AS expert," +
            "                                MAX(CASE WHEN ua.batch_role = 'AUDIO_VERIFIER' THEN u.username END)       AS audioModerator," +
            "                                MAX(CASE WHEN ua.batch_role = 'AUDIO_RECORDER' THEN u.username END)       AS recorder" +
            "                         FROM batch_user_assignments ua" +
            "                                  LEFT JOIN users u ON ua.user_id = u.user_id" +
            "                         WHERE ua.batch_role IN" +
            "                               ('TEXT_TRANSLATOR', 'TEXT_VERIFIER', 'EXPERT_TEXT_REVIEWER', 'AUDIO_VERIFIER'," +
            "                                'AUDIO_RECORDER')" +
            "                         GROUP BY ua.batch_details_id)," +
            "     AudioStats AS (SELECT batch_details_id," +
            "                           COUNT(voice_id)           AS audiosRecorded," +
            "                           SUM(IF(status = 0, 1, 0)) AS audiosApproved," +
            "                           SUM(IF(status = 2, 1, 0)) AS audiosRejected" +
            "                    FROM voice" +
            "                    WHERE batch_details_id IS NOT NULL" +
            "                    GROUP BY batch_details_id) " +
            "SELECT d.batch_details_id                      AS batchDetailsId," +
            "       b.batch_no                              AS batchNo," +
            "       b.source                                AS source," +
            "       l.name                                  AS 'language'," +
            "       d.batch_status                          AS status," +
            "       COALESCE(ss.numberOfSentences, 0)       AS numberOfSentences," +
            "       COALESCE(ss.sentencesTranslated, 0)     AS sentencesTranslated," +
            "       COALESCE(ss.sentencesApproved, 0)       AS sentencesApproved," +
            "       COALESCE(ss.sentencesRejected, 0)       AS sentencesRejected," +
            "       COALESCE(ss.sentencesExpertApproved, 0) AS sentencesExpertApproved," +
            "       COALESCE(ss.sentencesExpertRejected, 0) AS sentencesExpertRejected," +
            "       COALESCE(aud.audiosRecorded, 0)         AS audiosRecorded," +
            "       COALESCE(aud.audiosApproved, 0)         AS audiosApproved," +
            "       COALESCE(aud.audiosRejected, 0)         AS audiosRejected," +
            "       ua.translator," +
            "       ua.moderator," +
            "       ua.expert," +
            "       ua.recorder," +
            "       ua.audioModerator " +
            "FROM batch_details d" +
            "         JOIN batches b ON d.batch_id = b.batch_no" +
            "         JOIN languages l ON d.language = l.language_id" +
            "         LEFT JOIN SentenceStats ss ON d.batch_details_id = ss.batch_details_id" +
            "         LEFT JOIN UserAssignments ua ON d.batch_details_id = ua.batch_details_id" +
            "         LEFT JOIN AudioStats aud ON d.batch_details_id = aud.batch_details_id " +
            "WHERE b.deletion_status = 0" +
            "  AND d.deletion_status = 0" +
            "  AND b.batch_type = 'TEXT'" +
            "  AND b.from_feedback ='YES' " +
            "  AND (:languageId IS NULL OR d.language = :languageId) " +
            "  AND (:status IS NULL OR d.batch_status = :status) " +
            "ORDER BY d.batch_id " +
            "LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}",
            countQuery = "SELECT FOUND_ROWS()", nativeQuery = true)
    Page<BatchDetailsStatsMapping> getBatchDetailsStatsFeedbackText(Pageable pageable, @Param("languageId") Long languageId,
                                                                    @Param("status") Integer status);


    @Query(value = "WITH SentenceStats AS (SELECT bd.batch_details_id," +
            "                              COUNT(s.sentence_id)                AS numberOfSentences," +
            "                              COUNT(ts.translated_sentence_id)    AS sentencesTranslated," +
            "                              SUM(IF(ts.review_status = 0, 1, 0)) AS sentencesApproved," +
            "                              SUM(IF(ts.review_status = 2, 1, 0)) AS sentencesRejected," +
            "                              SUM(IF(ts.second_review = 0, 1, 0)) AS sentencesExpertApproved," +
            "                              SUM(IF(ts.second_review = 2, 1, 0)) AS sentencesExpertRejected" +
            "                       FROM batch_details bd" +
            "                                JOIN batches b ON bd.batch_id = b.batch_no" +
            "                                JOIN sentences s ON s.batch_no = b.batch_no AND s.deletion_status = 0" +
            "                                LEFT JOIN translated_sentence ts" +
            "                                          ON s.sentence_id = ts.sentence_id" +
            "                                              AND ts.batch_details_id = bd.batch_details_id" +
            "                                              AND ts.deletion_status = 0" +
            "                       WHERE b.deletion_status = 0" +
            "                         AND b.batch_type = 'TEXT'" +
            "                         AND b.from_feedback = 'NO'" +
            "                         AND bd.deletion_status = 0" +
            "                       GROUP BY bd.batch_details_id)," +
            "     UserAssignments AS (SELECT ua.batch_details_id," +
            "                                MAX(CASE WHEN ua.batch_role = 'TEXT_TRANSLATOR' THEN u.username END)      AS translator," +
            "                                MAX(CASE WHEN ua.batch_role = 'TEXT_VERIFIER' THEN u.username END)        AS moderator," +
            "                                MAX(CASE WHEN ua.batch_role = 'EXPERT_TEXT_REVIEWER' THEN u.username END) AS expert," +
            "                                MAX(CASE WHEN ua.batch_role = 'AUDIO_VERIFIER' THEN u.username END)       AS audioModerator," +
            "                                MAX(CASE WHEN ua.batch_role = 'AUDIO_RECORDER' THEN u.username END)       AS recorder" +
            "                         FROM batch_user_assignments ua" +
            "                                  LEFT JOIN users u ON ua.user_id = u.user_id" +
            "                         WHERE ua.batch_role IN" +
            "                               ('TEXT_TRANSLATOR', 'TEXT_VERIFIER', 'EXPERT_TEXT_REVIEWER', 'AUDIO_VERIFIER'," +
            "                                'AUDIO_RECORDER')" +
            "                         GROUP BY ua.batch_details_id)," +
            "     AudioStats AS (SELECT batch_details_id," +
            "                           COUNT(voice_id)           AS audiosRecorded," +
            "                           SUM(IF(status = 0, 1, 0)) AS audiosApproved," +
            "                           SUM(IF(status = 2, 1, 0)) AS audiosRejected" +
            "                    FROM voice" +
            "                    WHERE batch_details_id IS NOT NULL" +
            "                    GROUP BY batch_details_id) " +
            "SELECT d.batch_details_id                      AS batchDetailsId," +
            "       b.batch_no                              AS batchNo," +
            "       b.source                                AS source," +
            "       l.name                                  AS 'language'," +
            "       d.batch_status                          AS status," +
            "       COALESCE(ss.numberOfSentences, 0)       AS numberOfSentences," +
            "       COALESCE(ss.sentencesTranslated, 0)     AS sentencesTranslated," +
            "       COALESCE(ss.sentencesApproved, 0)       AS sentencesApproved," +
            "       COALESCE(ss.sentencesRejected, 0)       AS sentencesRejected," +
            "       COALESCE(ss.sentencesExpertApproved, 0) AS sentencesExpertApproved," +
            "       COALESCE(ss.sentencesExpertRejected, 0) AS sentencesExpertRejected," +
            "       COALESCE(aud.audiosRecorded, 0)         AS audiosRecorded," +
            "       COALESCE(aud.audiosApproved, 0)         AS audiosApproved," +
            "       COALESCE(aud.audiosRejected, 0)         AS audiosRejected," +
            "       ua.translator," +
            "       ua.moderator," +
            "       ua.expert," +
            "       ua.recorder," +
            "       ua.audioModerator " +
            "FROM batch_details d" +
            "         JOIN batches b ON d.batch_id = b.batch_no" +
            "         JOIN languages l ON d.language = l.language_id" +
            "         LEFT JOIN SentenceStats ss ON d.batch_details_id = ss.batch_details_id" +
            "         LEFT JOIN UserAssignments ua ON d.batch_details_id = ua.batch_details_id" +
            "         LEFT JOIN AudioStats aud ON d.batch_details_id = aud.batch_details_id " +
            "WHERE b.deletion_status = 0" +
            "  AND d.deletion_status = 0" +
            "  AND b.batch_type = 'AUDIO'" +
            "  AND b.from_feedback = 'NO' " +
            "  AND (:languageId IS NULL OR d.language = :languageId) " +
            "  AND (:status IS NULL OR d.batch_status = :status) " +
            "ORDER BY d.batch_id " +
            "LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}",
            countQuery = "SELECT FOUND_ROWS()", nativeQuery = true)
    Page<BatchDetailsStatsMapping> getBatchDetailsStatsAudio(Pageable pageable, @Param("languageId") Long languageId,
                                                             @Param("status") Integer status);
}
