package drlugha.translator.system.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDTO {

    private Date dateModified;

    @Column(name = "active", columnDefinition = "boolean default true")
    private boolean isActive;

}
