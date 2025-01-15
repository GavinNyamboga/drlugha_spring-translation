package kmusau.translator.entity;

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
@Table(name = "expert_comments")
public class ExpertCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @JoinColumn(name = "translated_sentence_id", referencedColumnName = "translatedSentenceId", unique = true)
    @OneToOne
    private TranslatedSentenceEntity translatedSentence;

    private String comment;
}
