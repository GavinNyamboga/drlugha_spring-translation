package drlugha.translator.system.voice.repository;

import drlugha.translator.system.voice.model.PopulateAudioIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulateAudioIndexRepository extends JpaRepository<PopulateAudioIndexEntity, Long> {
    PopulateAudioIndexEntity getPopulateAudioIndexEntitiesByBucketName(String bucketName);
}
