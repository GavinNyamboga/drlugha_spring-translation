package drlugha.translator.system.sentence.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "moderator_comments")
public class ModeratorCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @JoinColumn(name = "translated_sentence_id", referencedColumnName = "translated_sentence_id", unique = true)
    @OneToOne
    private TranslatedSentenceEntity translatedSentence;

    private String comment;
}
