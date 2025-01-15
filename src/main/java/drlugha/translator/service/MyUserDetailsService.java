package drlugha.translator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import drlugha.translator.entity.UsersEntity;
import drlugha.translator.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender javaMailSender;

    private static final ModelMapper modelMapper = new ModelMapper();
    Logger logger = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<UsersEntity> user = userRepo.findByUsername(username);

        if (user.isPresent()) {
            return user.map(UserDetailsImpl::new).get();
        } else {
            throw new UsernameNotFoundException(username);
        }
    }


}