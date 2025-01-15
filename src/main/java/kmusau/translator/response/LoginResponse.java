package kmusau.translator.response;

import kmusau.translator.entity.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private UsersEntity userDetails;

    private String message;
}
