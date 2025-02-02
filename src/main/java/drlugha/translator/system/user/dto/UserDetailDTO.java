package drlugha.translator.system.user.dto;

import drlugha.translator.system.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {

    private Long userId;
    private Long translatedSentenceId;
    private String username;
    private String email;
    private Long voiceId;
    private String fileUrl;

    public UserDetailDTO(String username) {
        this.username = username;
    }

    public User fromDto(UserDetailDTO dto) {
        User user = new User();
        if (dto == null)
            return user;
        if (dto.getUsername() != null)
            user.setUsername(dto.getUsername());
        if (dto.getUserId() != null)
            user.setUserId(dto.getUserId());
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());

        return user;
    }

    public UserDetailDTO toDto(User user) {
        UserDetailDTO userDto = new UserDetailDTO();
        if (user == null)
            return userDto;

        if (user.getUserId() != null)
            userDto.setUserId(user.getUserId());
        if (user.getUsername() != null)
            userDto.setUsername(user.getUsername());
        if (user.getEmail() != null)
            userDto.setEmail(user.getEmail());

        return userDto;
    }
}



