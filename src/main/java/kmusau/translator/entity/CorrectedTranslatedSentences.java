package kmusau.translator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "corrected_translated_sentences")
@Entity
public class CorrectedTranslatedSentences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long correctedSentenceId;

    private String originalTranslation;

    @Column(name = "translated_sentence_id")
    private Long translatedSentenceId;

    @JsonIgnore
    @JoinColumn(name = "translated_sentence_id", referencedColumnName = "translatedSentenceId", updatable = false, insertable = false)
    @ManyToOne()
    private TranslatedSentenceEntity translatedSentence;

}
