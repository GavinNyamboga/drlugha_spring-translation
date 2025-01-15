package kmusau.translator.DTOs.userDTOs;

import kmusau.translator.entity.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTokenDto {

    private String resetToken;

    private String newPassword;

//    public UsersEntity dtoToEntity(VerifyTokenDto verifyTokenDto) {
//        UsersEntity usersEntity = new UsersEntity();
//
//        if (verifyTokenDto == null) {
//            return usersEntity;
//        }
//
//
//        if (verifyTokenDto.resetToken != null)
//            usersEntity.setResetToken(verifyTokenDto.getResetToken());
//        if (verifyTokenDto.getNewPassword() != null)
//            usersEntity.setPassword(verifyTokenDto.newPassword);
//        return usersEntity;
//    }
}
