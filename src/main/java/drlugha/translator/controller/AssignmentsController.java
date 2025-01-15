package drlugha.translator.controller;

import drlugha.translator.DTOs.assignmentDTOs.AssignmentDto;
import drlugha.translator.DTOs.sentenceDTOs.SentencesToTranslateDto;
import drlugha.translator.entity.AssignedSentencesEntity;
import drlugha.translator.enums.BatchStatus;
import drlugha.translator.enums.StatusTypes;
import drlugha.translator.repository.TranslateAssignmentRepository;
import drlugha.translator.response.ResponseMessage;
import drlugha.translator.service.AssignedSentencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AssignmentsController {

    @Autowired
    TranslateAssignmentRepository assignmentRepo;

    @Autowired
    AssignedSentencesService assignmentSvc;


    //Assigning tasks

    //fetch assignments
    @GetMapping("/fetch/assignments")
    public List<AssignedSentencesEntity> getTranslateAssignments() {
        return assignmentRepo.findAll();
    }

//	@PostMapping("/create/assignment/{assignId}/{reviewerId}")
//	public AssignedSentencesEntity assignTasks(@RequestBody AssignedSentencesEntity assignment, @PathVariable long assignId, @PathVariable long reviewerId) {
//		return assignmentSvc.createAssignment(assignment, assignId, reviewerId);
//	}

    //assign sentences to a translator and a moderator
    @PostMapping("assign/sentences")
    public ResponseEntity<ResponseMessage> assignTask(@RequestBody AssignmentDto assignmentDto) {
        return assignmentSvc.createTasks(assignmentDto);
    }

    //fetch users pending assignment
    @GetMapping("/users/translation/assignments")
    //User's finished work -- change param (translationStatus) to completed
    public List<AssignedSentencesEntity> usersAssignedTasks(@RequestParam(defaultValue = "assigned") StatusTypes translationStatus, Long userId) {
        return assignmentRepo.findByTranslationStatusAndTranslatorUserId(translationStatus, userId);
    }

    @GetMapping("/user/translation/assignments")
    public SentencesToTranslateDto userAssignedAssignments(
            @RequestParam(defaultValue = "assignedTranslator") BatchStatus batchStatus,
            @RequestParam Long userId,
            @RequestParam(required = false) Long batchDetailsId) {
        return assignmentSvc.fetchAssignedSentences(userId, batchStatus, batchDetailsId);
    }


    //fetch users completed assignments
    @GetMapping("/users/completed/assignments") //User's finished work -- change param (translationStatus) to completed
    public List<AssignedSentencesEntity> usersCompletedTasks(@RequestParam(defaultValue = "completed") StatusTypes translationStatus, Long userId) {
        return assignmentRepo.findByTranslationStatusAndTranslatorUserId(translationStatus, userId);
    }

    //delete a task assigned to a certain user
    @DeleteMapping("/delete/assignment/{assignmentId}")
    public String deleteAssignment(@PathVariable Long assignmentId) {
        assignmentRepo.deleteById(assignmentId);
        return "Deleted successfully";
    }

}
