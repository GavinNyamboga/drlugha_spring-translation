package kmusau.translator.entity;

import javax.persistence.*;

@Entity
@Table(name = "populate_audio_index")
public class PopulateAudioIndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bucketName;

    private Long lastAudioIndex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public long getLastAudioIndex() {
        return lastAudioIndex;
    }

    public void setLastAudioIndex(long lastAudioIndex) {
        this.lastAudioIndex = lastAudioIndex;
    }
}
