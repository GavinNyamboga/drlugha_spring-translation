package drlugha.translator.system.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.user.dto.*;
import drlugha.translator.system.user.repository.UserRepository;
import drlugha.translator.shared.dto.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UserRepository userRepo;

    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender javaMailSender;

    public ResponseEntity<ResponseMessage> createUser(CreateUserDto userDto) {

        Optional<User> optionalUser = userRepo.findByUsername(userDto.getUsername());

        if (userDto.getUsername() == null || userDto.getUsername().isBlank() || userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            ResponseMessage responseMessage = new ResponseMessage("Username or Email cannot be empty");
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(responseMessage);
        }
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            ResponseMessage responseMessage = new ResponseMessage("Password cannot be empty");
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(responseMessage);
        }

        if (optionalUser.isPresent()) {
            ResponseMessage responseMessage = new ResponseMessage("Username " + userDto.getUsername() + " already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessage);
        } else {
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPhoneNo(userDto.getPhoneNo());
            user.setHashedPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setRoles(userDto.getRoles());
            user.setDateCreated(new Date());
            user.setDateModified(new Date());
            user.setActive(true);
            user.setFirstTimeLogin(true);
            userRepo.save(user);

            ResponseMessage responseMessage = new ResponseMessage("User " + userDto.getUsername() + " created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
        }

    }

    public ResponseEntity changePassword(ChangePasswordDto changePasswordDto, Long userId) {
        User user = userRepo.findById(userId).get();

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getHashedPassword())) {
            ResponseMessage responseMessage = new ResponseMessage("Incorrect Old Password");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMessage);
        } else {
            if (Objects.nonNull(changePasswordDto.getNewPassword())) {
                User userFromDto = new ChangePasswordDto().DtoToEntity(changePasswordDto);
                user.setHashedPassword(passwordEncoder.encode(userFromDto.getHashedPassword()));
                userRepo.save(user);
            }
        }
        ResponseMessage responseMessage = new ResponseMessage("Password changed successfully");
        return ResponseEntity.ok().body(responseMessage);
    }

    public User updateUser(User user, Long userId) throws JsonProcessingException {
        User userEntity = userRepo.findById(userId).get();

        if (Objects.nonNull(user.getUsername())) {
            Optional<User> existingUsers = userRepo.findByUsername(user.getUsername());
            if (existingUsers.isPresent()) {
                throw new RuntimeException();
            } else {
                userEntity.setUsername(user.getUsername());
            }
        }

        if (Objects.nonNull(user.getEmail())) {
            userEntity.setEmail(user.getEmail());
        }
        if (Objects.nonNull(user.getPhoneNo())) {
            userEntity.setPhoneNo(user.getPhoneNo());
        }
        if (Objects.nonNull(user.getRoles())) {
            userEntity.setRoles(user.getRoles());
        }

        userEntity.setDateModified(new Date());

        User updatedUser = userRepo.save(userEntity);

        return updatedUser;
    }

    public User updateUserStatus(UserStatusDTO userDto, Long userId) throws JsonProcessingException {
        User existingUser = userRepo.findById(userId).get();

        userDto.setDateModified(new Date());
        User user = new ModelMapper().map(userDto, User.class);
        if (Objects.nonNull(userDto.isActive())) {
            existingUser.setActive(user.isActive());
        }
        if (Objects.nonNull(userDto.getDateModified())) {
            existingUser.setDateModified(user.getDateModified());
        }
        return userRepo.save(existingUser);

    }

    public ResponseEntity forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        String recoveryToken = UUID.randomUUID().toString();
        User user = new ForgotPasswordDto().dtoToEntity(forgotPasswordDto);
        Optional<User> users = userRepo.findByUsername(user.getUsername());
        if (users.isPresent()) {
            User foundUser = users.get();
            foundUser.setResetToken(recoveryToken + foundUser.getUserId());
            userRepo.save(foundUser);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(foundUser.getEmail());

            msg.setSubject("Translator App - Password recovery token");
            msg.setText("Below is the recovery token,  \n " + foundUser.getResetToken());

            javaMailSender.send(msg);
            return ResponseEntity.ok("Email sent..");

        } else {
            return ResponseEntity.badRequest().body("Username not found");
        }

    }

    public ResponseEntity verifyToken(VerifyTokenDto verifyTokenDto) {
        if (verifyTokenDto.getResetToken().equals("") || verifyTokenDto.getNewPassword().equals("")) {
            return ResponseEntity.badRequest().body("Please enter all the fields");
        }

        Optional<User> users = userRepo.findByResetToken(verifyTokenDto.getResetToken());
        if (users.isPresent()) {
            User user = users.get();
            user.setHashedPassword(passwordEncoder.encode(verifyTokenDto.getNewPassword()));
            user.setResetToken(null);

            userRepo.save(user);
            return ResponseEntity.ok("Password changed");
        } else {
            return ResponseEntity.badRequest().body("Enter a valid recovery token");
        }

    }

    public ResponseEntity<ResponseMessage> hashPassword() {
        List<User> usersEntities = userRepo.findAll();
        for (User user : usersEntities) {
            String encodePassword = passwordEncoder.encode(user.getPassword());
            user.setHashedPassword(encodePassword);
        }
        userRepo.saveAll(usersEntities);
        return ResponseEntity.ok(new ResponseMessage("Hashing was successful"));
    }
}
