package drlugha.translator.auth.service;

import drlugha.translator.system.user.model.User;
import drlugha.translator.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepo.findByUsername(username);

        if (user.isPresent()) {
            return user.map(UserDetailsImpl::new).get();
        } else {
            throw new UsernameNotFoundException(username);
        }
    }


}