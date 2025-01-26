package drlugha.translator.system.stats.dto;

import drlugha.translator.system.user.model.User;
import drlugha.translator.system.batch.projections.*;
import lombok.Data;

@Data
public class UserStatsDTO {
    private Long userId;
    private String username;
    private String email;
    private TranslatorStats translator;
    private TranslatorStats transcriber;
    private ModeratorStats moderator;
    private ModeratorStats transcriptionModerator;
    private ExpertStats expert;
    private ExpertStats transcriptionExpert;
    private RecorderStats recorder;
    private AudioModeratorStats audioModerator;

    public UserStatsDTO(User user) {
        userId = user.getUserId();
        username = user.getUsername();
        email = user.getEmail();
    }

    public UserStatsDTO() {

    }
}
