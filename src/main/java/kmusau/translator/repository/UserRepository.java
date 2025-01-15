package kmusau.translator.repository;

import java.util.List;
import java.util.Optional;

import kmusau.translator.DTOs.stats.StatsDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import kmusau.translator.entity.UsersEntity;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UsersEntity, Long>{

	Optional<UsersEntity> findByUsername(String username);
	Optional<UsersEntity> findByResetToken(String resetToken);

	@Query("SELECT u FROM UsersEntity u WHERE u.roles = 'ROLE_ADMIN' OR u.roles = 'ROLE_MODERATOR' ")
	List<UsersEntity> findAllReviewers();

	/**
	 *
	 * @return
	 */
	@Query(value = "SELECT user_id, email, username, " +
			"       IFNULL((SELECT COUNT(*) FROM translated_sentence " +
			"            CROSS JOIN batch_details bd on bd.batch_details_id = translated_sentence.batch_details_id " +
			"                  WHERE translated_by = users.user_id " +
			"        GROUP BY translated_by), 0) as translated, " +
			"       IFNULL((SELECT COUNT(*) FROM translated_sentence " +
			"             CROSS JOIN batch_details bd on bd.batch_details_id = translated_sentence.batch_details_id " +
			"        WHERE translation_verified_by_id = users.user_id AND review_status = 0 " +
			"        GROUP BY translation_verified_by_id), 0) as translationsVerified, " +
			"       IFNULL((SELECT COUNT(*) FROM translated_sentence " +
			"             CROSS JOIN batch_details bd on bd.batch_details_id = translated_sentence.batch_details_id " +
			"        WHERE second_reviewer_id = users.user_id AND second_review = 0 " +
			"        GROUP BY second_reviewer_id), 0) as translationsExpertVerified, " +
			"       IFNULL((SELECT COUNT(*) FROM translated_sentence " +
			"                                                         CROSS JOIN batch_details bd on bd.batch_details_id = translated_sentence.batch_details_id " +
			"        WHERE recorded_by_id = users.user_id AND recorded_status = 4 " +
			"        GROUP BY recorded_by_id), 0) as recorded, " +
			"       IFNULL((SELECT COUNT(*) FROM voice " +
			"           CROSS JOIN translated_sentence ts on voice.translated_sentence_id = ts.translated_sentence_id " +
			"                                                         CROSS JOIN batch_details bd on bd.batch_details_id = ts.batch_details_id " +
			"        WHERE audio_verified_by_id = users.user_id AND status = 0 " +
			"        GROUP BY audio_verified_by_id), 0) as audiosVerified, " +
			"       IFNULL((SELECT COUNT(*) FROM voice " +
			"           CROSS JOIN translated_sentence ts on voice.translated_sentence_id = ts.translated_sentence_id " +
			"                                                         CROSS JOIN batch_details bd on bd.batch_details_id = ts.batch_details_id " +
			"        WHERE audio_verified_by_id = users.user_id AND status = 2 " +
			"        GROUP BY audio_verified_by_id), 0) as audiosRejected " +
			"FROM users ORDER BY user_id", nativeQuery = true)
	List<StatsDTO> generateStats();

}
