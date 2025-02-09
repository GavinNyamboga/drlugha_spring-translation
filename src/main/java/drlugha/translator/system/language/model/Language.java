package drlugha.translator.system.language.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import drlugha.translator.shared.enums.DeletionStatus;
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
@Table(name = "languages")
public class Language {
    @Id
    @Column(name = "language_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long languageId;

    private String name;

    @JsonIgnore
    private DeletionStatus deletionStatus;

    public Language(Long languageId) {
        this.languageId = languageId;
    }
}
