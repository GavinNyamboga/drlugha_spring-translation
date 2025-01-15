package kmusau.translator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kmusau.translator.DTOs.userDTOs.*;
import kmusau.translator.entity.UsersEntity;
import kmusau.translator.repository.UserRepository;
import kmusau.translator.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UsersService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender javaMailSender;

    public ResponseEntity<ResponseMessage> createUser(CreateUserDto userDto) {

        Optional<UsersEntity> optionalUser = userRepo.findByUsername(userDto.getUsername());

        if(userDto.getUsername() == null || userDto.getUsername().isBlank() || userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            ResponseMessage responseMessage = new ResponseMessage("Username or Email cannot be empty");
            return  ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(responseMessage);
        }
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()){
            ResponseMessage responseMessage = new ResponseMessage("Password cannot be empty");
            return  ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(responseMessage);
        }

        if(optionalUser.isPresent()) {
            ResponseMessage responseMessage = new ResponseMessage("Username "+userDto.getUsername()+" already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessage);
        }
        else {
            UsersEntity user = new UsersEntity();
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

            ResponseMessage responseMessage = new ResponseMessage("User "+userDto.getUsername()+" created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
        }

    }

    public ResponseEntity changePassword(ChangePasswordDto changePasswordDto, Long userId) {
        UsersEntity user = userRepo.findById(userId).get();

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getHashedPassword())) {
            ResponseMessage responseMessage = new ResponseMessage("Incorrect Old Password");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMessage);
        } else {
            if (Objects.nonNull(changePasswordDto.getNewPassword())) {
                UsersEntity userFromDto = new ChangePasswordDto().DtoToEntity(changePasswordDto);
                user.setHashedPassword(passwordEncoder.encode(userFromDto.getHashedPassword()));
                userRepo.save(user);
            }
        }
        ResponseMessage responseMessage = new ResponseMessage("Password changed successfully");
        return ResponseEntity.ok().body(responseMessage);
    }

    public UsersEntity updateUser(UsersEntity user, Long userId) throws JsonProcessingException {
        UsersEntity userEntity = userRepo.findById(userId).get();

        if(Objects.nonNull(user.getUsername())) {
            Optional<UsersEntity> existingUsers = userRepo.findByUsername(user.getUsername());
            if (existingUsers.isPresent()) {
                throw new RuntimeException();
            } else {
                userEntity.setUsername(user.getUsername());
            }
        }

        if(Objects.nonNull(user.getEmail())) {
            userEntity.setEmail(user.getEmail());
        }
        if(Objects.nonNull(user.getPhoneNo())) {
            userEntity.setPhoneNo(user.getPhoneNo());
        }
        if(Objects.nonNull(user.getRoles())) {
            userEntity.setRoles(user.getRoles());
        }

        userEntity.setDateModified(new Date());

        UsersEntity updatedUser = userRepo.save(userEntity);

        return updatedUser;
    }

    public UsersEntity updateUserStatus(UserStatusDTO userDto, Long userId) throws JsonProcessingException {
        UsersEntity existingUser = userRepo.findById(userId).get();

        userDto.setDateModified(new Date());
        UsersEntity user = new ModelMapper().map(userDto, UsersEntity.class);
        if(Objects.nonNull(userDto.isActive())) {
            existingUser.setActive(user.isActive());
        }
        if (Objects.nonNull(userDto.getDateModified())) {
            existingUser.setDateModified(user.getDateModified());
        }
        return userRepo.save(existingUser);

    }

    public ResponseEntity forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        String recoveryToken = UUID.randomUUID().toString();
        UsersEntity user = new ForgotPasswordDto().dtoToEntity(forgotPasswordDto);
        Optional<UsersEntity> users = userRepo.findByUsername(user.getUsername());
        if (users.isPresent()) {
            UsersEntity foundUser = users.get();
            foundUser.setResetToken(recoveryToken+foundUser.getUserId());
            userRepo.save(foundUser);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(foundUser.getEmail());

            msg.setSubject("Translator App - Password recovery token");
            msg.setText("Below is the recovery token,  \n " +foundUser.getResetToken());

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

        Optional<UsersEntity> users = userRepo.findByResetToken(verifyTokenDto.getResetToken());
        if (users.isPresent()) {
            UsersEntity usersEntity = users.get();
            usersEntity.setHashedPassword(passwordEncoder.encode(verifyTokenDto.getNewPassword()));
            usersEntity.setResetToken(null);

            userRepo.save(usersEntity);
            return ResponseEntity.ok("Password changed");
        } else {
            return ResponseEntity.badRequest().body("Enter a valid recovery token");
        }

    }

    public ResponseEntity<ResponseMessage> hashPassword() {
        List<UsersEntity> usersEntities = userRepo.findAll();
        for (UsersEntity user: usersEntities){
            String encodePassword = passwordEncoder.encode(user.getPassword());
            user.setHashedPassword(encodePassword);
        }
        userRepo.saveAll(usersEntities);
        return ResponseEntity.ok(new ResponseMessage("Hashing was successful"));
    }
}
