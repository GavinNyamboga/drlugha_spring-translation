package kmusau.translator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import kmusau.translator.DTOs.userDTOs.ChangePasswordDto;
import kmusau.translator.DTOs.userDTOs.CreateUserDto;
import kmusau.translator.DTOs.userDTOs.ForgotPasswordDto;
import kmusau.translator.DTOs.userDTOs.UserDto;
import kmusau.translator.DTOs.userDTOs.UserStatusDTO;
import kmusau.translator.DTOs.userDTOs.VerifyTokenDto;
import kmusau.translator.entity.AuthenticationRequest;
import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.entity.UsersEntity;
import kmusau.translator.entity.VoiceEntity;
import kmusau.translator.repository.TranslatedSentenceRepository;
import kmusau.translator.repository.UserRepository;
import kmusau.translator.repository.VoiceRepository;
import kmusau.translator.response.LoginResponse;
import kmusau.translator.response.ResponseMessage;
import kmusau.translator.service.MyUserDetailsService;
import kmusau.translator.service.UsersService;
import kmusau.translator.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsersService usersService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    UserRepository userRepo;

    @Autowired
    TranslatedSentenceRepository translatedSentenceRepo;

    @Autowired
    VoiceRepository voiceRepo;

    @Autowired
    Logger logger;

    @Autowired
    ObjectMapper objectMapper;

    private static final ModelMapper modelMapper = new ModelMapper();

    <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return (List<T>)source
                .stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    @PostMapping({"/authenticate"})
    public ResponseEntity<LoginResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        LoginResponse loginResponse = new LoginResponse();
        if (authenticationRequest.getUsername() == null || authenticationRequest.getUsername().isBlank() || authenticationRequest
                .getPassword() == null || authenticationRequest.getPassword().isBlank()) {
            loginResponse.setMessage("Username or Password cannot be empty");
            return ResponseEntity.badRequest().body(loginResponse);
        }
        try {
            this.authenticationManager.authenticate((Authentication)new UsernamePasswordAuthenticationToken(authenticationRequest
                    .getUsername(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            loginResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(loginResponse);
        }
        UserDetails userDetails = this.myUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        UsersEntity user = this.userRepo.findByUsername(authenticationRequest.getUsername()).get();
        String jwt = this.jwtTokenUtil.generateToken(userDetails);
        user.setFirstTimeLogin(false);
        this.userRepo.save(user);
        loginResponse.setToken(jwt);
        loginResponse.setUserDetails(user);
        loginResponse.setMessage("Logged in successfully");
        return ResponseEntity.ok().body(loginResponse);
    }

    @GetMapping({"/fetch/users"})
    public List<UserDto> allUsers() throws JsonProcessingException {
        List<UsersEntity> usersEntity = this.userRepo.findAll();
        this.logger.info("Fetch all users");
        List<UserDto> users = mapList(usersEntity, UserDto.class);
        return users;
    }

    @GetMapping({"/fetch/reviewers"})
    public List<UserDto> allReviewers() throws JsonProcessingException {
        List<UsersEntity> usersEntity = this.userRepo.findAllReviewers();
        return mapList(usersEntity, UserDto.class);
    }

    @PostMapping({"/forgotpassword"})
    public ResponseEntity forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        return this.usersService.forgotPassword(forgotPasswordDto);
    }

    @PostMapping({"/verifytoken"})
    public ResponseEntity verifyToken(@RequestBody VerifyTokenDto verifyTokenDto) {
        return this.usersService.verifyToken(verifyTokenDto);
    }

    @GetMapping({"/fetch/user/{id}"})
    public UserDto singleUsers(@PathVariable Long id) {
        UsersEntity user = this.userRepo.findById(id).get();
        this.logger.info("fetched user" + user.toString());
        UserDto userDto = (UserDto)modelMapper.map(user, UserDto.class);
        return userDto;
    }

    @PostMapping({"/create/user"})
    public ResponseEntity<ResponseMessage> addUser(@RequestBody CreateUserDto user) throws Exception {
        return this.usersService.createUser(user);
    }

    @PutMapping({"/update/user/{userId}"})
    public ResponseMessage updateUser(@RequestBody UsersEntity user, @PathVariable Long userId) throws JsonProcessingException {
        try {
            UsersEntity foundUser = this.usersService.updateUser(user, userId);
            return new ResponseMessage("Updated user details " + this.objectMapper.writeValueAsString(foundUser));
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/update/userstatus/{userId}"})
    public ResponseMessage updateUserStatus(@RequestBody UserStatusDTO userDto, @PathVariable Long userId) throws JsonProcessingException {
        try {
            UsersEntity foundUser = this.usersService.updateUserStatus(userDto, userId);
            return new ResponseMessage("Updated user Status to " + foundUser.isActive());
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/update/userpassword/{userId}"})
    public ResponseEntity updateOwnPassword(@RequestBody ChangePasswordDto changePasswordDto, @PathVariable Long userId) {
        return this.usersService.changePassword(changePasswordDto, userId);
    }

    @DeleteMapping({"/delete/user/{userId}"})
    public ResponseMessage deleteUser(@PathVariable Long userId) {
        try {
            this.userRepo.deleteById(userId);
            return new ResponseMessage("Deleted successfully");
        } catch (EmptyResultDataAccessException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @GetMapping({"/fetch/user/{userId}/translatedSentences"})
    public int usersTranslatedSentencesByDates(@RequestParam Date startDate, @RequestParam Date endDate, @PathVariable Long userId) {
        return this.translatedSentenceRepo.numberOfTranslatedSentencesByUser(userId, startDate, endDate).intValue();
    }

    @GetMapping({"/fetch/user/{userId}/approvedTranslatedSentence"})
    public List<TranslatedSentenceEntity> usersApprovedTranslatedSentences(@RequestParam Date startDate, @RequestParam Date endDate, @PathVariable Long userId) {
        return this.translatedSentenceRepo.numberOfApprovedTranslatedSentencesByUser(userId, startDate, endDate);
    }

    @GetMapping({"/fetch/user/{userId}/rejectedTranslatedSentence"})
    public List<TranslatedSentenceEntity> usersRejectedTranslatedSentences(@RequestParam Date startDate, @RequestParam Date endDate, @PathVariable Long userId) {
        return this.translatedSentenceRepo.numberOfRejectedTranslatedSentencesByUser(userId, startDate, endDate);
    }

    @GetMapping({"/fetch/user/{userId}/unreviewedTranslatedSentence"})
    public List<TranslatedSentenceEntity> usersUnreviewedTranslatedSentences(@RequestParam Date startDate, @RequestParam Date endDate, @PathVariable Long userId) {
        return this.translatedSentenceRepo.numberOfUnreviewedTranslatedSentencesByUser(userId, startDate, endDate);
    }

    @GetMapping({"/fetch/user/{userId}/audios"})
    public List<VoiceEntity> usersAudiosDone(@RequestParam Date startDate, @RequestParam Date endDate, @PathVariable Long userId) {
        return this.voiceRepo.usersAudiosDone(userId, startDate, endDate);
    }

    @PutMapping({"/hash-passwords"})
    public ResponseEntity<ResponseMessage> hashPasswords() {
        return this.usersService.hashPassword();
    }
}
