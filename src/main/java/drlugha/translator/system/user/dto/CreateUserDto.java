package drlugha.translator.system.user.dto;

import lombok.Data;

@Data
public class CreateUserDto {
    private String username;
    private String email;
    private String phoneNo;
    private String roles;
    private String password;
}
