package drlugha.translator.system.batch.repository;

import drlugha.translator.system.sentence.dto.SentenceItemDto;
import drlugha.translator.system.stats.dto.TotalSentencesDto;
import drlugha.translator.system.stats.dto.TotalTranslatedSentencesDto;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.sentence.model.Sentence;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.system.batch.enums.BatchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BatchDetailsRepository extends JpaRepository<BatchDetailsEntity, Long> {

    List<BatchDetailsEntity> findByTranslatedByIdAndBatchStatus(Long translatorId, BatchStatus batchStatus);

    List<BatchDetailsEntity> findByTranslatedByIdAndBatchDetailsId(Long translatorId, Long batchDetailsId);

    List<BatchDetailsEntity> findByTranslationVerifiedByIdAndBatchStatus(Long reviewerId, BatchStatus batchStatus);

    List<BatchDetailsEntity> findByTranslationVerifiedByIdAndBatchDetailsId(Long reviewerId, Long batchDetailsId);


    List<BatchDetailsEntity> findBySecondReviewerIdAndBatchStatus(Long secondReviewerId, BatchStatus batchStatus);

    List<BatchDetailsEntity> findBySecondReviewerIdAndBatchDetailsId(Long secondReviewerId, Long batchDetailsId);

    List<BatchDetailsEntity> findByRecordedByIdAndBatchStatus(Long recorderId, BatchStatus batchStatus);

    List<BatchDetailsEntity> findByRecordedByIdAndBatchDetailsId(Long recorderId, Long batchDetailsId);

    List<BatchDetailsEntity> findByBatchId(Long batchId);

    List<BatchDetailsEntity> findAllByTranslatedByIdAndBatch_BatchType(Long userId, BatchType batchType);

    List<BatchDetailsEntity> findAllByTranslationVerifiedByIdAndBatch_BatchType(Long userId, BatchType batchType);

    List<BatchDetailsEntity> findAllBySecondReviewerIdAndBatch_BatchType(long userId, BatchType batchType);

    List<BatchDetailsEntity> findAllByRecordedById(long userId);

    List<BatchDetailsEntity> findAllByAudioVerifiedById(long userId);

    List<BatchDetailsEntity> findAllByBatchDetailsIdAndAudioVerifiedById(Long batchDetailsId, Long audioVerifiedById);

    void deleteAllByBatchId(Long batchNo);

    @Query(value = "SELECT COUNT(CASE WHEN (s.sentence_text IS NOT NULL) THEN 1  END) as totalSentences, " +
            "       COUNT(CASE WHEN (s.audio_link IS NOT NULL) THEN 1  END) as totalUploadedAudios " +
            "FROM batches b LEFT JOIN sentences s on b.batch_no = s.batch_no " +
            "where s.deletion_status = 0 AND b.deletion_status = 0", nativeQuery = true)
    TotalSentencesDto getTotalSentences();

    @Query(value = "SELECT count(CASE WHEN (b.batch_type = 'TEXT') THEN 1 END) AS totalTranslatedSentences," +
            "       count(CASE WHEN (b.batch_type = 'AUDIO') THEN 1 END) AS totalTranscribedAudios " +
            "FROM translated_sentence ts " +
            "    CROSS JOIN batch_details bd on bd.batch_details_id = ts.batch_details_id " +
            "    CROSS JOIN batches b on bd.batch_id = b.batch_no " +
            "    WHERE ts.deletion_status = 0 and bd.deletion_status = 0 AND b.deletion_status = 0", nativeQuery = true)
    TotalTranslatedSentencesDto getTotalTranslatedSentences();

    @Query("SELECT bd.batchDetailsId FROM BatchDetailsEntity bd")
    List<Long> findAllBatchDetailsId();

    @Query(value = "SELECT s.sentence_id AS sentenceId, s.sentence_text AS sentenceText, t.translated_sentence_id AS translatedSentenceId, t.translated_text AS translatedText, " +
            "(SELECT v.file_url FROM voice v WHERE v.translated_sentence_id = t.translated_sentence_id ORDER BY v.voice_id DESC LIMIT 1) AS audioUrl  " +
            "FROM translated_sentence t " +
            "CROSS JOIN sentences s ON s.sentence_id = t.sentence_id " +
            "CROSS JOIN batch_details b on b.batch_details_id = t.batch_details_id " +
            "CROSS JOIN  languages l ON l.language_id = b.language " +
            "WHERE l.language_id = :languageId AND b.batch_status >= :batchStatus", nativeQuery = true)
    List<SentenceItemDto> getAllSentencesInLanguagePerBatchDetailsStatus(Long languageId, Integer batchStatus);

    @Query(value = "SELECT s.sentence_id AS sentenceId, s.sentence_text AS sentenceText, s.audio_link AS transcriptionAudioUrl, t.translated_sentence_id AS translatedSentenceId, t.translated_text AS translatedText, " +
            "(SELECT v.file_url FROM voice v WHERE v.translated_sentence_id = t.translated_sentence_id ORDER BY v.voice_id DESC LIMIT 1) AS audioUrl  " +
            "FROM translated_sentence t " +
            "CROSS JOIN sentences s ON s.sentence_id = t.sentence_id " +
            "CROSS JOIN batch_details b on b.batch_details_id = t.batch_details_id " +
            "WHERE b.batch_details_id = :batchDetailsId", nativeQuery = true)
    List<SentenceItemDto> getAllSentencesInBatchDetails(Long batchDetailsId);

    @Query(value = "SELECT s.sentence_id AS sentenceId, " +
            "s.sentence_text AS sentenceText, " +
            "s.audio_link AS transcriptionAudioUrl, " +
            "t.translated_sentence_id AS translatedSentenceId, " +
            "t.translated_text AS translatedText, " +
            "v.file_url AS audioUrl " +
            "FROM translated_sentence t " +
            "JOIN sentences s ON s.sentence_id = t.sentence_id " +
            "JOIN batch_details b ON b.batch_details_id = t.batch_details_id " +
            "LEFT JOIN voice v ON v.translated_sentence_id = t.translated_sentence_id " +
            "WHERE b.batch_details_id = :batchDetailsId " +
            "AND v.file_url = :audioUrl " +
            "AND v.file_url IS NOT NULL " +
            "GROUP BY s.sentence_id, s.sentence_text, s.audio_link, t.translated_sentence_id, t.translated_text, v.file_url " +
            "ORDER BY v.voice_id DESC " +
            "LIMIT 1", nativeQuery = true)
    Sentence findSentenceByAudioUrl(@Param("batchDetailsId") Long batchDetailsId, @Param("audioUrl") String audioUrl);


    @Query("SELECT new Sentence(" +
            "   s.sentenceId, s.sentenceText, s.audioLink, " +
            "   t.translatedSentenceId, t.translatedText, " +
            "   MAX(v.fileUrl)" + // Using MAX to get the latest fileUrl per translatedSentenceId
            ") " +
            "FROM TranslatedSentenceEntity t " +
            "JOIN Sentence s ON s.sentenceId = t.sentenceId " +
            "LEFT JOIN VoiceEntity v ON v.translatedSentence.translatedSentenceId = t.translatedSentenceId " +
            "JOIN BatchDetailsEntity b ON b.batchDetailsId = t.batchDetailsId " +
            "WHERE b.batchDetailsId = :batchDetailsId " +
            "GROUP BY s.sentenceId, s.sentenceText, s.audioLink, t.translatedSentenceId, t.translatedText")
    List<Sentence> findAllSentencesByBatchDetailsId(@Param("batchDetailsId") Long batchDetailsId);

    @Query("SELECT b FROM BatchDetailsEntity b WHERE b.batchId=:batchNo and b.language.languageId=:languageId")
    BatchDetailsEntity findByBatchIdAndLanguageId(Long batchNo, Long languageId);

}
