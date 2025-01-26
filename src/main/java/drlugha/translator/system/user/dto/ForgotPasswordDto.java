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
public class ForgotPasswordDto {

    private String username;

    public User dtoToEntity(ForgotPasswordDto forgotPasswordDto) {
        User user = new User();
        if (forgotPasswordDto == null) {
            return user;
        }

        if (forgotPasswordDto.getUsername() != null)
            user.setUsername(forgotPasswordDto.getUsername());
        return user;
    }
}
