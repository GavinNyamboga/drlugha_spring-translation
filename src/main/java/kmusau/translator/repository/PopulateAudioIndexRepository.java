package kmusau.translator.repository;

import kmusau.translator.entity.PopulateAudioIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulateAudioIndexRepository extends JpaRepository<PopulateAudioIndexEntity, Long> {
    PopulateAudioIndexEntity getPopulateAudioIndexEntitiesByBucketName(String bucketName);
}
