package drlugha.translator.system.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.system.batch.model.BatchDetailsEntity;
import drlugha.translator.system.batch.model.BatchEntity;
import drlugha.translator.system.sentence.model.AssignedSentencesEntity;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.user.enums.Gender;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {

    @Transient
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

    @Transient
    public static final String CURRENT_USERNAME = "CURRENT_USERNAME";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private Date dateCreated;

    private Date dateModified;

    @Column(unique = true)
    private String username;

    private String password;

    private String hashedPassword;

    private String email;

    @Column(nullable = false)
    private String phoneNo;

    private String roles;

    @Column(name = "active", columnDefinition = "boolean default true")
    private boolean isActive;

    @Column(name = "first_login", columnDefinition = "boolean default true")
    private boolean isFirstTimeLogin;

    private String resetToken;

    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @JsonIgnore
    @OneToMany(mappedBy = "translator")
    private List<AssignedSentencesEntity> assignments;

    //Someone to verify the translation
    @JsonIgnore
    @OneToMany(mappedBy = "assignedToReview")
    private List<AssignedSentencesEntity> assignmentsList;

    //Someone to record audio for the translation
    @JsonIgnore
    @OneToMany(mappedBy = "assignedRecorder")
    private List<TranslatedSentenceEntity> translatedSentence;

    //Someone to review recorded audio for the translation
    @JsonIgnore
    @OneToMany(mappedBy = "assignedAudioReviewer")
    private List<TranslatedSentenceEntity> translatedSentenceAudio;

    //User who uploads a batch of sentences
    @JsonIgnore
    @OneToMany(mappedBy = "uploader")
    private List<BatchEntity> batchEntities;

    @JsonIgnore
    @OneToMany(mappedBy = "translatedBy")
    private List<BatchDetailsEntity> batchTranslations;

    @JsonIgnore
    @OneToMany(mappedBy = "translationVerifiedBy")
    private List<BatchDetailsEntity> batchTextVerifications;

    @JsonIgnore
    @OneToMany(mappedBy = "secondReviewer")
    private List<BatchDetailsEntity> secondReview;

    @JsonIgnore
    @OneToMany(mappedBy = "recordedBy")
    private List<BatchDetailsEntity> batchRecordings;

    @JsonIgnore
    @OneToMany(mappedBy = "audioVerifiedBy")
    private List<BatchDetailsEntity> batchRecordingsVerification;

}
