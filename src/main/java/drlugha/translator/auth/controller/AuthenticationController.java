package drlugha.translator.auth.controller;

import drlugha.translator.auth.service.AuthenticationService;
import drlugha.translator.auth.dto.AuthenticationRequest;
import drlugha.translator.auth.dto.LoginResponse;
import drlugha.translator.shared.controller.BaseController;
import drlugha.translator.system.user.dto.ForgotPasswordDto;
import drlugha.translator.system.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController extends BaseController {

    private final AuthenticationService authenticationService;

    private final UsersService usersService;


    @PostMapping({"/authenticate"})
    public ResponseEntity<LoginResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        return entity(authenticationService.login(authenticationRequest));
    }

    @PostMapping({"/forgotpassword"})
    public ResponseEntity forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        return this.usersService.forgotPassword(forgotPasswordDto);
    }
}
