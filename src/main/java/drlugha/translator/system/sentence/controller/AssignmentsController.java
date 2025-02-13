package drlugha.translator.system.sentence.controller;

import drlugha.translator.system.sentence.dto.AssignmentDTO;
import drlugha.translator.system.sentence.dto.SentencesToTranslateDto;
import drlugha.translator.system.sentence.model.AssignedSentencesEntity;
import drlugha.translator.system.batch.enums.BatchStatus;
import drlugha.translator.shared.enums.StatusTypes;
import drlugha.translator.system.sentence.repository.TranslateAssignmentRepository;
import drlugha.translator.shared.dto.ResponseMessage;
import drlugha.translator.system.sentence.service.AssignedSentencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AssignmentsController {

    private final TranslateAssignmentRepository assignmentRepo;

    private final AssignedSentencesService assignmentSvc;


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
    public ResponseEntity<ResponseMessage> assignTask(@RequestBody AssignmentDTO assignmentDto) {
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
            @RequestParam(defaultValue = "ASSIGNED_TRANSLATOR") BatchStatus batchStatus,
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
