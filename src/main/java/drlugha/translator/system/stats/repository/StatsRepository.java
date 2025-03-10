package drlugha.translator.system.stats.repository;

import drlugha.translator.system.batch.model.BatchDetailsUserAssignment;
import drlugha.translator.system.stats.dto.LanguageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StatsRepository extends JpaRepository<BatchDetailsUserAssignment, Long> {

    @Query(value = "select l.name as languageName," +
            "       l.language_id as languageId," +
            "       b.batch_type as batchType," +
            "       sum(b.sentences_or_audio_count) as totalBatchSentencesOrAudios," +
            "       count(distinct bd.batch_details_id) as batchCount," +
            "       sum(coalesce(ua.translated, 0))            as totalTranslated," +
            "       sum(coalesce(ua.text_approved, 0))         as totalTextApproved," +
            "       sum(coalesce(ua.text_rejected, 0))         as totalTextRejected," +
            "       sum(coalesce(ua.text_expert_approved, 0))  as totalTextExpertApproved," +
            "       sum(coalesce(ua.text_expert_rejected, 0))  as totalTextExpertRejected," +
            "       sum(coalesce(ua.recorded, 0))              as totalAudioRecorded," +
            "       sum(coalesce(ua.audio_approved, 0))        as totalAudioApproved," +
            "       sum(coalesce(ua.audio_rejected, 0))        as totalAudioRejected," +
            "       sum(coalesce(ua.audio_expert_approved, 0)) as totalAudioExpertApproved," +
            "       sum(coalesce(ua.audio_expert_rejected, 0)) as totalExpertAudioRejected " +
            "from languages l" +
            "         inner join batch_details bd on l.language_id = bd.language" +
            "         inner join batches b on bd.batch_id = b.batch_no" +
            "         left join batch_user_assignments ua on bd.batch_details_id = ua.batch_details_id" +
            "         left join users u on ua.user_id = u.user_id " +
            "where b.deletion_status = 0" +
            "  and bd.deletion_status = 0" +
            "  and l.deletion_status = 0 " +
            "group by l.language_id,b.batch_type", nativeQuery = true)
    List<LanguageStats> getLanguageStatistics();
}
