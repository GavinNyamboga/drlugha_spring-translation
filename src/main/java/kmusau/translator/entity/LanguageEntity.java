package kmusau.translator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kmusau.translator.enums.DeletionStatus;
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
public class LanguageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long languageId;

    private String name;

    @JsonIgnore
    private DeletionStatus deletionStatus;

    public LanguageEntity(Long languageId){
        this.languageId = languageId;
    }
}
