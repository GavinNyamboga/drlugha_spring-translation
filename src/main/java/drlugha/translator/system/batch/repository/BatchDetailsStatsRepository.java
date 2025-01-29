package drlugha.translator.system.batch.repository;

import drlugha.translator.shared.enums.YesNo;
import drlugha.translator.system.batch.enums.BatchType;
import drlugha.translator.system.batch.model.BatchDetailsStatsEntity;
import drlugha.translator.system.batch.projections.*;
import drlugha.translator.system.stats.dto.BatchDetailsStatsMapping;
import drlugha.translator.system.stats.dto.TotalUserStatsDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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


    @Query(value = "SELECT d.batch_details_id                      AS batchDetailsId," +
            "       b.batch_no                              AS batchNo," +
            "       b.source                                AS source," +
            "       l.name                                  AS language," +
            "       d.batch_status                          AS status," +
            "       coalesce(ss.numberOfSentences, 0)       as numberOfSentences," +
            "       coalesce(ss.sentencesTranslated, 0)     as sentencesTranslated," +
            "       coalesce(ss.sentencesApproved, 0)       as sentencesApproved," +
            "       coalesce(ss.sentencesRejected, 0)       as sentencesRejected," +
            "       coalesce(ss.sentencesExpertApproved, 0) as sentencesExpertApproved," +
            "       coalesce(ss.sentencesExpertRejected, 0) as sentencesExpertRejected," +
            "       coalesce(aud.audiosRecorded, 0)         as audiosRecorded," +
            "       coalesce(aud.audiosApproved, 0)         as audiosApproved," +
            "       coalesce(aud.audiosRejected, 0)         as audiosRejected," +
            "       ua.translator," +
            "       ua.moderator," +
            "       ua.expert," +
            "       ua.recorder," +
            "       ua.audioModerator " +
            "FROM batch_details d" +
            "         INNER JOIN batches b ON d.batch_id = b.batch_no" +
            "         INNER JOIN languages l ON d.language = l.language_id" +
            "         LEFT JOIN (SELECT ts.batch_details_id," +
            "                           COUNT(DISTINCT s.sentence_id)                              AS numberOfSentences," +
            "                           COUNT(DISTINCT ts.translated_sentence_id)                  AS sentencesTranslated," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.review_status = 0" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesApproved," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.review_status = 2" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesRejected," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.second_review = 0" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesExpertApproved," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.second_review = 2" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesExpertRejected" +
            "                    FROM sentences s" +
            "                             LEFT JOIN translated_sentence ts ON s.sentence_id = ts.sentence_id" +
            "                    WHERE s.deletion_status = 0" +
            "                      and ts.deletion_status = 0" +
            "                    GROUP BY ts.batch_details_id) ss ON d.batch_details_id = ss.batch_details_id" +
            "         LEFT JOIN (SELECT ua.batch_details_id," +
            "                           ua2.user_id                                                                 AS recorder_id," +
            "                           MAX(CASE WHEN ua1.batch_role = 'TEXT_TRANSLATOR' THEN u1.username END)      AS translator," +
            "                           MAX(CASE WHEN ua1.batch_role = 'TEXT_VERIFIER' THEN u1.username END)        AS moderator," +
            "                           MAX(CASE WHEN ua1.batch_role = 'EXPERT_TEXT_REVIEWER' THEN u1.username END) AS expert," +
            "                           u2.username                                                                 AS recorder," +
            "                           MAX(CASE WHEN ua1.batch_role = 'AUDIO_VERIFIER' THEN u1.username END)       AS audioModerator" +
            "                    FROM batch_user_assignments ua" +
            "                             LEFT JOIN batch_user_assignments ua1 ON ua.batch_details_id = ua1.batch_details_id" +
            "                             LEFT JOIN users u1 ON ua1.user_id = u1.user_id" +
            "                             LEFT JOIN batch_user_assignments ua2 ON ua.batch_details_id = ua2.batch_details_id AND" +
            "                                                                     ua2.batch_role = 'AUDIO_RECORDER'" +
            "                             LEFT JOIN users u2 ON ua2.user_id = u2.user_id" +
            "                    GROUP BY ua.batch_details_id, ua2.user_id) ua ON d.batch_details_id = ua.batch_details_id" +
            "         LEFT JOIN (SELECT ua.batch_details_id," +
            "                           ua.user_id," +
            "                           COUNT(DISTINCT v.voice_id)                                 AS audiosRecorded," +
            "                           COUNT(DISTINCT CASE WHEN v.status = 0 THEN v.voice_id END) AS audiosApproved," +
            "                           COUNT(DISTINCT CASE WHEN v.status = 2 THEN v.voice_id END) AS audiosRejected" +
            "                    FROM batch_user_assignments ua" +
            "                             LEFT JOIN voice v ON v.user_id = ua.user_id" +
            "                        AND v.batch_details_id = ua.batch_details_id" +
            "                    WHERE ua.batch_role = 'AUDIO_RECORDER'" +
            "                    GROUP BY ua.batch_details_id, ua.user_id) aud ON d.batch_details_id = aud.batch_details_id" +
            "    AND aud.user_id = ua.recorder_id " +
            "WHERE b.deletion_status = 0" +
            "  and d.deletion_status = 0 " +
            " and b.batch_type='TEXT'" +
            " and b.from_feedback ='NO' " +
            "ORDER BY d.batch_id, ua.recorder", nativeQuery = true)
    List<BatchDetailsStatsMapping> getBatchDetailsStatsText();


    @Query(value = "SELECT d.batch_details_id                      AS batchDetailsId," +
            "       b.batch_no                              AS batchNo," +
            "       b.source                                AS source," +
            "       l.name                                  AS language," +
            "       d.batch_status                          AS status," +
            "       coalesce(ss.numberOfSentences, 0)       as numberOfSentences," +
            "       coalesce(ss.sentencesTranslated, 0)     as sentencesTranslated," +
            "       coalesce(ss.sentencesApproved, 0)       as sentencesApproved," +
            "       coalesce(ss.sentencesRejected, 0)       as sentencesRejected," +
            "       coalesce(ss.sentencesExpertApproved, 0) as sentencesExpertApproved," +
            "       coalesce(ss.sentencesExpertRejected, 0) as sentencesExpertRejected," +
            "       coalesce(aud.audiosRecorded, 0)         as audiosRecorded," +
            "       coalesce(aud.audiosApproved, 0)         as audiosApproved," +
            "       coalesce(aud.audiosRejected, 0)         as audiosRejected," +
            "       ua.translator," +
            "       ua.moderator," +
            "       ua.expert," +
            "       ua.recorder," +
            "       ua.audioModerator " +
            "FROM batch_details d" +
            "         INNER JOIN batches b ON d.batch_id = b.batch_no" +
            "         INNER JOIN languages l ON d.language = l.language_id" +
            "         LEFT JOIN (SELECT ts.batch_details_id," +
            "                           COUNT(DISTINCT s.sentence_id)                              AS numberOfSentences," +
            "                           COUNT(DISTINCT ts.translated_sentence_id)                  AS sentencesTranslated," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.review_status = 0" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesApproved," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.review_status = 2" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesRejected," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.second_review = 0" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesExpertApproved," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.second_review = 2" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesExpertRejected" +
            "                    FROM sentences s" +
            "                             LEFT JOIN translated_sentence ts ON s.sentence_id = ts.sentence_id" +
            "                    WHERE s.deletion_status = 0" +
            "                      and ts.deletion_status = 0" +
            "                    GROUP BY ts.batch_details_id) ss ON d.batch_details_id = ss.batch_details_id" +
            "         LEFT JOIN (SELECT ua.batch_details_id," +
            "                           ua2.user_id                                                                 AS recorder_id," +
            "                           MAX(CASE WHEN ua1.batch_role = 'TEXT_TRANSLATOR' THEN u1.username END)      AS translator," +
            "                           MAX(CASE WHEN ua1.batch_role = 'TEXT_VERIFIER' THEN u1.username END)        AS moderator," +
            "                           MAX(CASE WHEN ua1.batch_role = 'EXPERT_TEXT_REVIEWER' THEN u1.username END) AS expert," +
            "                           u2.username                                                                 AS recorder," +
            "                           MAX(CASE WHEN ua1.batch_role = 'AUDIO_VERIFIER' THEN u1.username END)       AS audioModerator" +
            "                    FROM batch_user_assignments ua" +
            "                             LEFT JOIN batch_user_assignments ua1 ON ua.batch_details_id = ua1.batch_details_id" +
            "                             LEFT JOIN users u1 ON ua1.user_id = u1.user_id" +
            "                             LEFT JOIN batch_user_assignments ua2 ON ua.batch_details_id = ua2.batch_details_id AND" +
            "                                                                     ua2.batch_role = 'AUDIO_RECORDER'" +
            "                             LEFT JOIN users u2 ON ua2.user_id = u2.user_id" +
            "                    GROUP BY ua.batch_details_id, ua2.user_id) ua ON d.batch_details_id = ua.batch_details_id" +
            "         LEFT JOIN (SELECT ua.batch_details_id," +
            "                           ua.user_id," +
            "                           COUNT(DISTINCT v.voice_id)                                 AS audiosRecorded," +
            "                           COUNT(DISTINCT CASE WHEN v.status = 0 THEN v.voice_id END) AS audiosApproved," +
            "                           COUNT(DISTINCT CASE WHEN v.status = 2 THEN v.voice_id END) AS audiosRejected" +
            "                    FROM batch_user_assignments ua" +
            "                             LEFT JOIN voice v ON v.user_id = ua.user_id" +
            "                        AND v.batch_details_id = ua.batch_details_id" +
            "                    WHERE ua.batch_role = 'AUDIO_RECORDER'" +
            "                    GROUP BY ua.batch_details_id, ua.user_id) aud ON d.batch_details_id = aud.batch_details_id" +
            "    AND aud.user_id = ua.recorder_id " +
            "WHERE b.deletion_status = 0" +
            "  and d.deletion_status = 0 " +
            " and b.batch_type='TEXT'" +
            " and b.from_feedback ='YES' " +
            "ORDER BY d.batch_id, ua.recorder", nativeQuery = true)
    List<BatchDetailsStatsMapping> getBatchDetailsStatsFeedbackText();


    @Query(value = "SELECT d.batch_details_id                      AS batchDetailsId," +
            "       b.batch_no                              AS batchNo," +
            "       b.source                                AS source," +
            "       l.name                                  AS language," +
            "       d.batch_status                          AS status," +
            "       coalesce(ss.numberOfSentences, 0)       as numberOfSentences," +
            "       coalesce(ss.sentencesTranslated, 0)     as sentencesTranslated," +
            "       coalesce(ss.sentencesApproved, 0)       as sentencesApproved," +
            "       coalesce(ss.sentencesRejected, 0)       as sentencesRejected," +
            "       coalesce(ss.sentencesExpertApproved, 0) as sentencesExpertApproved," +
            "       coalesce(ss.sentencesExpertRejected, 0) as sentencesExpertRejected," +
            "       coalesce(aud.audiosRecorded, 0)         as audiosRecorded," +
            "       coalesce(aud.audiosApproved, 0)         as audiosApproved," +
            "       coalesce(aud.audiosRejected, 0)         as audiosRejected," +
            "       ua.translator," +
            "       ua.moderator," +
            "       ua.expert," +
            "       ua.recorder," +
            "       ua.audioModerator " +
            "FROM batch_details d" +
            "         INNER JOIN batches b ON d.batch_id = b.batch_no" +
            "         INNER JOIN languages l ON d.language = l.language_id" +
            "         LEFT JOIN (SELECT ts.batch_details_id," +
            "                           COUNT(DISTINCT s.sentence_id)                              AS numberOfSentences," +
            "                           COUNT(DISTINCT ts.translated_sentence_id)                  AS sentencesTranslated," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.review_status = 0" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesApproved," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.review_status = 2" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesRejected," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.second_review = 0" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesExpertApproved," +
            "                           COUNT(DISTINCT CASE" +
            "                                              WHEN ts.second_review = 2" +
            "                                                  THEN ts.translated_sentence_id END) AS sentencesExpertRejected" +
            "                    FROM sentences s" +
            "                             LEFT JOIN translated_sentence ts ON s.sentence_id = ts.sentence_id" +
            "                    WHERE s.deletion_status = 0" +
            "                      and ts.deletion_status = 0" +
            "                    GROUP BY ts.batch_details_id) ss ON d.batch_details_id = ss.batch_details_id" +
            "         LEFT JOIN (SELECT ua.batch_details_id," +
            "                           ua2.user_id                                                                 AS recorder_id," +
            "                           MAX(CASE WHEN ua1.batch_role = 'TEXT_TRANSLATOR' THEN u1.username END)      AS translator," +
            "                           MAX(CASE WHEN ua1.batch_role = 'TEXT_VERIFIER' THEN u1.username END)        AS moderator," +
            "                           MAX(CASE WHEN ua1.batch_role = 'EXPERT_TEXT_REVIEWER' THEN u1.username END) AS expert," +
            "                           u2.username                                                                 AS recorder," +
            "                           MAX(CASE WHEN ua1.batch_role = 'AUDIO_VERIFIER' THEN u1.username END)       AS audioModerator" +
            "                    FROM batch_user_assignments ua" +
            "                             LEFT JOIN batch_user_assignments ua1 ON ua.batch_details_id = ua1.batch_details_id" +
            "                             LEFT JOIN users u1 ON ua1.user_id = u1.user_id" +
            "                             LEFT JOIN batch_user_assignments ua2 ON ua.batch_details_id = ua2.batch_details_id AND" +
            "                                                                     ua2.batch_role = 'AUDIO_RECORDER'" +
            "                             LEFT JOIN users u2 ON ua2.user_id = u2.user_id" +
            "                    GROUP BY ua.batch_details_id, ua2.user_id) ua ON d.batch_details_id = ua.batch_details_id" +
            "         LEFT JOIN (SELECT ua.batch_details_id," +
            "                           ua.user_id," +
            "                           COUNT(DISTINCT v.voice_id)                                 AS audiosRecorded," +
            "                           COUNT(DISTINCT CASE WHEN v.status = 0 THEN v.voice_id END) AS audiosApproved," +
            "                           COUNT(DISTINCT CASE WHEN v.status = 2 THEN v.voice_id END) AS audiosRejected" +
            "                    FROM batch_user_assignments ua" +
            "                             LEFT JOIN voice v ON v.user_id = ua.user_id" +
            "                        AND v.batch_details_id = ua.batch_details_id" +
            "                    WHERE ua.batch_role = 'AUDIO_RECORDER'" +
            "                    GROUP BY ua.batch_details_id, ua.user_id) aud ON d.batch_details_id = aud.batch_details_id" +
            "    AND aud.user_id = ua.recorder_id " +
            "WHERE b.deletion_status = 0" +
            "  and d.deletion_status = 0 " +
            " and b.batch_type='AUDIO'" +
            " and b.from_feedback ='NO' " +
            "ORDER BY d.batch_id, ua.recorder", nativeQuery = true)
    List<BatchDetailsStatsMapping> getBatchDetailsStatsAudio();
}
