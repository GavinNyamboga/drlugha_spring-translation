package drlugha.translator.system.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drlugha.translator.system.sentence.model.TranslatedSentenceEntity;
import drlugha.translator.system.user.model.User;
import drlugha.translator.system.voice.model.VoiceEntity;
import drlugha.translator.system.sentence.repository.TranslatedSentenceRepository;
import drlugha.translator.system.user.dto.*;
import drlugha.translator.system.user.repository.UserRepository;
import drlugha.translator.system.voice.repository.VoiceRepository;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.system.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    private final UserRepository userRepo;

    private final TranslatedSentenceRepository translatedSentenceRepo;

    private final VoiceRepository voiceRepo;

    @Autowired
    Logger logger;

    @Autowired
    ObjectMapper objectMapper;

    private static final ModelMapper modelMapper = new ModelMapper();

    <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source
                .stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }



    @GetMapping({"/fetch/users"})
    public List<UserDTO> allUsers() throws JsonProcessingException {
        List<User> user = this.userRepo.findAll();
        this.logger.info("Fetch all users");
        List<UserDTO> users = mapList(user, UserDTO.class);
        return users;
    }

    @GetMapping({"/fetch/reviewers"})
    public List<UserDTO> allReviewers() throws JsonProcessingException {
        List<User> user = this.userRepo.findAllReviewers();
        return mapList(user, UserDTO.class);
    }

    @PostMapping({"/verifytoken"})
    public ResponseEntity verifyToken(@RequestBody VerifyTokenDto verifyTokenDto) {
        return this.usersService.verifyToken(verifyTokenDto);
    }

    @GetMapping({"/fetch/user/{id}"})
    public UserDTO singleUsers(@PathVariable Long id) {
        User user = this.userRepo.findById(id).get();
        this.logger.info("fetched user" + user.toString());
        UserDTO userDto = (UserDTO) modelMapper.map(user, UserDTO.class);
        return userDto;
    }

    @PostMapping({"/create/user"})
    public ResponseEntity<ResponseMessage> addUser(@RequestBody CreateUserDto user) throws Exception {
        return this.usersService.createUser(user);
    }

    @PutMapping({"/update/user/{userId}"})
    public ResponseMessage updateUser(@RequestBody User user, @PathVariable Long userId) throws JsonProcessingException {
        try {
            User foundUser = this.usersService.updateUser(user, userId);
            return new ResponseMessage("Updated user details " + this.objectMapper.writeValueAsString(foundUser));
        } catch (NoSuchElementException e) {
            return new ResponseMessage(e.getMessage());
        }
    }

    @PutMapping({"/update/userstatus/{userId}"})
    public ResponseMessage updateUserStatus(@RequestBody UserStatusDTO userDto, @PathVariable Long userId) throws JsonProcessingException {
        try {
            User foundUser = this.usersService.updateUserStatus(userDto, userId);
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
