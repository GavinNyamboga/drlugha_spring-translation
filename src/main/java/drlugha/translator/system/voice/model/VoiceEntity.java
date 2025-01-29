package drlugha.translator.system.voice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "voice")
public class VoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voiceId;

    private Date dateCreated;

    private Date dateModified;

    private String fileUrl;

    @Column(name = "translated_sentence_id", insertable = false, updatable = false)
    private Long translatedSentenceId;

    @Column(length = 1200)
    private String presignedUrl;

    @Enumerated(EnumType.ORDINAL)
    private StatusTypes status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "translated_sentence_id", referencedColumnName = "translatedSentenceId")
    private TranslatedSentenceEntity translatedSentence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "batch_details_id")
    private Long batchDetailsId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_details_id", insertable = false, updatable = false)
    private BatchDetailsEntity batchDetails;
}




