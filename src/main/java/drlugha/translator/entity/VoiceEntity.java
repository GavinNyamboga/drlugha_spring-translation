package drlugha.translator.entity;

import drlugha.translator.enums.StatusTypes;
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
    private UsersEntity user; // Assuming UsersEntity is correctly defined in your application

    // Constructors, getters, and setters
}




