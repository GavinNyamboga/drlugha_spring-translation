package kmusau.translator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import kmusau.translator.entity.TranslatedSentenceEntity;
import kmusau.translator.entity.VoiceEntity;
import kmusau.translator.enums.StatusTypes;
import kmusau.translator.repository.TranslatedSentenceRepository;
import kmusau.translator.repository.VoiceRepository;
import kmusau.translator.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@CrossOrigin(origins = "*")
@RestController
public class Controller {
	
	@Autowired
	TranslatedSentenceRepository translatedRepo;

	@Autowired
	VoiceRepository voiceRepo;
	
	@Autowired 
	private JwtUtil jwtTokenUtil;


	private static String UPLOADED_FOLDER = "/home/kelvin/eclipse-workspace/hackerrank/";



	@GetMapping("/v1/test")
	public String welcome() {
		return "Welcome";	
	}

	

	

	//Voice Endpoints

	@PostMapping("/record/voice/{translatedSentenceId}")
	public ResponseEntity<String> singleFileUpload(@RequestParam("file") MultipartFile file, 
			@RequestHeader (HttpHeaders.AUTHORIZATION) String authorizationHeader, 
			RedirectAttributes redirectAttributes, VoiceEntity voice, @PathVariable Long translatedSentenceId) throws JsonProcessingException {

		Date date = new Date();
		File filepath = null;
		String username = null;
		String jwt;

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			username = jwtTokenUtil.extractUsername(jwt);
		}		 
		
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
			return ResponseEntity.badRequest().body("Please select a file to upload");
		}

		try {

			// Get the file and save it somewhere
			byte[] bytes = file.getBytes();
			Path path = Paths.get(UPLOADED_FOLDER + "audio "+username+" "+date);
			filepath = path.toFile();
			Files.write(path, bytes);

			redirectAttributes.addFlashAttribute("message",
					"You successfully uploaded '" + file.getOriginalFilename() + "'");

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (voice.getDateCreated() == null) {
			voice.setDateCreated(new Date());
		}
		if (voice.getDateModified() == null) {
			voice.setDateModified(new Date());
		}
		if (voice.getStatus() == null) {
			voice.setStatus(StatusTypes.unreviewed);
		}
		
		TranslatedSentenceEntity translatedSentence = translatedRepo.findById(translatedSentenceId).get();

		translatedSentence.setRecordedStatus(StatusTypes.recorded);

		voice.setTranslatedSentence(translatedSentence);
//		voice.setFilepath(filepath);
			voiceRepo.save(voice);
		return ResponseEntity.ok().body("Uploaded successfully to ");
	}

 }