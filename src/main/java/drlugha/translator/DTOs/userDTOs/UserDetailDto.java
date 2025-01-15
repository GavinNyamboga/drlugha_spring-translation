package drlugha.translator.DTOs.userDTOs;

import drlugha.translator.entity.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDto {

    private Long userId;
    private String username;
    private String email;
    private Long voiceId;
    private String fileUrl;

    public UserDetailDto(String username) {
        this.username = username;
    }

    public UsersEntity fromDto(UserDetailDto dto) {
        UsersEntity user = new UsersEntity();
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

    public UserDetailDto toDto(UsersEntity usersEntity) {
        UserDetailDto userDto = new UserDetailDto();
        if (usersEntity == null)
            return userDto;

        if (usersEntity.getUserId() != null)
            userDto.setUserId(usersEntity.getUserId());
        if (usersEntity.getUsername() != null)
            userDto.setUsername(usersEntity.getUsername());
        if (usersEntity.getEmail() != null)
            userDto.setEmail(usersEntity.getEmail());

        return userDto;
    }
}



