package drlugha.translator.auth.service;

import drlugha.translator.auth.dto.AuthenticationRequest;
import drlugha.translator.auth.dto.LoginResponse;
import drlugha.translator.shared.exception.AccessDeniedException;
import drlugha.translator.shared.exception.BadRequestException;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.user.repository.UserRepository;
import drlugha.translator.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final MyUserDetailsService myUserDetailsService;

    private final JwtUtil jwtTokenUtil;

    private final HttpServletRequest servletRequest;

    public LoginResponse login(AuthenticationRequest authenticationRequest) {
        LoginResponse loginResponse = new LoginResponse();
        if (authenticationRequest.getUsername() == null || authenticationRequest.getUsername().isBlank() || authenticationRequest
                .getPassword() == null || authenticationRequest.getPassword().isBlank()) {
            throw new BadRequestException("Username or Password cannot be empty");
        }
        try {
            this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest
                    .getUsername(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            loginResponse.setMessage(e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }
        UserDetails userDetails = this.myUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        User user = this.userRepository.findByUsername(authenticationRequest.getUsername()).orElse(null);
        if (user == null) {
            throw new BadRequestException("Username not found");
        }
        String jwt = this.jwtTokenUtil.generateToken(userDetails);
        user.setFirstTimeLogin(false);
        this.userRepository.save(user);

        loginResponse.setToken(jwt);
        loginResponse.setUserDetails(user);
        loginResponse.setMessage("Logged in successfully");
        return loginResponse;
    }

    public String getCurrentUsername() {
        return servletRequest.getSession().getAttribute(User.CURRENT_USERNAME).toString();
    }

    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null)
            return null;
        return this.userRepository.findByUsername(username).orElse(null);
    }
}
