package kmusau.translator.DTOs.stats;

import kmusau.translator.entity.UsersEntity;
import kmusau.translator.projections.*;
import lombok.Data;

@Data
public class UserStatsDto {
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

    public UserStatsDto(UsersEntity usersEntity){
        userId = usersEntity.getUserId();
        username = usersEntity.getUsername();
        email = usersEntity.getEmail();
    }

    public UserStatsDto(){

    }
}
