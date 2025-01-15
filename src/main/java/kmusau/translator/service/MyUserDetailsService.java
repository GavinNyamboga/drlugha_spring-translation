package kmusau.translator.service;

import java.util.*;


import kmusau.translator.DTOs.userDTOs.ChangePasswordDto;
import kmusau.translator.DTOs.userDTOs.ForgotPasswordDto;
import kmusau.translator.DTOs.userDTOs.UserStatusDTO;
import kmusau.translator.DTOs.userDTOs.VerifyTokenDto;
import kmusau.translator.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kmusau.translator.entity.UsersEntity;
import kmusau.translator.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService{

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

		 if (user.isPresent()){
			 return user.map(UserDetailsImpl::new).get();
	        }else{
	            throw new UsernameNotFoundException(username);
	        }
	}
	

}