package drlugha.translator.system.user.dto;

import drlugha.translator.system.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {

    private String oldPassword;

    private String newPassword;

    public User DtoToEntity(ChangePasswordDto changePasswordDto) {
        User user = new User();

        if (changePasswordDto == null) {
            return user;
        }

        if (changePasswordDto.newPassword != null)
            user.setHashedPassword(changePasswordDto.newPassword);

        return user;
    }
}
