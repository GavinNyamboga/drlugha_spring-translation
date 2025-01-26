package drlugha.translator.system.sentence.model;

import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.language.model.Language;
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
@Table(name = "assigned_sentences")
public class AssignedSentencesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @Column(name = "translator_id")
    private Long translatorId;

    @JoinColumn(name = "translator_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User translator; //Assign someone to translate.

    //    private String sentence;
    @Column(name = "sentence_id")
    private Long sentenceId;

    @JoinColumn(name = "sentence_id", referencedColumnName = "sentenceId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Sentence sentence;

    @JoinColumn(name = "translate_to_language", referencedColumnName = "languageId")
    @ManyToOne
    private Language translateToLanguage;

    private Date dateAssigned;

    @Enumerated(EnumType.STRING)
    private StatusTypes translationStatus;

    @Column(name = "assigned_to_review_id")
    private Long assignedToReviewId;

    @JoinColumn(name = "assigned_to_review_id", referencedColumnName = "userId", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedToReview; //Assign someone to review


}
