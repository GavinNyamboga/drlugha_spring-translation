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
public class UserDTO {

    private Long userId;

    private String username;

    private String email;

    private String phoneNo;

    private String roles;

    public User fromDto(UserDTO dto) {
        User user = new User();
        if (dto == null)
            return user;
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getPhoneNo() != null)
            user.setPhoneNo(dto.getPhoneNo());
        if (dto.getRoles() != null)
            user.setRoles(dto.getRoles());
        if (dto.getUsername() != null)
            user.setUsername(dto.getUsername());
        if (dto.getUserId() != null)
            user.setUserId(dto.getUserId());

        return user;

    }

    public UserDTO toDto(User user) {
        UserDTO userDto = new UserDTO();
        if (user == null)
            return userDto;
        if (user.getUserId() != null)
            userDto.setUserId(user.getUserId());
        if (user.getEmail() != null)
            userDto.setEmail(user.getEmail());
        if (user.getUsername() != null)
            userDto.setUsername(user.getUsername());
        if (user.getPhoneNo() != null)
            userDto.setPhoneNo(user.getPhoneNo());
        if (user.getRoles() != null) {
            userDto.setRoles(user.getRoles());
        }

        return userDto;
    }
}
