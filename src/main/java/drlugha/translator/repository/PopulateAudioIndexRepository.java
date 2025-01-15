package drlugha.translator.repository;

import drlugha.translator.entity.PopulateAudioIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulateAudioIndexRepository extends JpaRepository<PopulateAudioIndexEntity, Long> {
    PopulateAudioIndexEntity getPopulateAudioIndexEntitiesByBucketName(String bucketName);
}
