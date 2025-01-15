package drlugha.translator.DTOs.userDTOs;

import drlugha.translator.entity.UsersEntity;
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

    public UsersEntity DtoToEntity(ChangePasswordDto changePasswordDto) {
        UsersEntity usersEntity = new UsersEntity();

        if (changePasswordDto == null) {
            return usersEntity;
        }

        if (changePasswordDto.newPassword != null)
            usersEntity.setHashedPassword(changePasswordDto.newPassword);

        return usersEntity;
    }
}
