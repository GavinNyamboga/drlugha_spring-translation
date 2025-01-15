package kmusau.translator.DTOs.userDTOs;

import kmusau.translator.entity.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordDto {

    private String username;
    
    public UsersEntity dtoToEntity(ForgotPasswordDto forgotPasswordDto) {
        UsersEntity user = new UsersEntity();
        if (forgotPasswordDto == null) {
            return user;
        }

        if (forgotPasswordDto.getUsername() != null)
            user.setUsername(forgotPasswordDto.getUsername());
        return user;
    }
}
