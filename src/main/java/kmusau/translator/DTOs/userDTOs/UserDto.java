package kmusau.translator.DTOs.userDTOs;

import kmusau.translator.entity.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long userId;

    private String username;

    private String email;

    private String phoneNo;

    private String roles;

    public UsersEntity fromDto(UserDto dto) {
        UsersEntity user = new UsersEntity();
        if (dto == null)
            return user;
        if(dto.getEmail() != null)
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

    public UserDto toDto(UsersEntity usersEntity) {
        UserDto userDto = new UserDto();
        if (usersEntity == null)
            return userDto;
        if (usersEntity.getUserId() != null)
            userDto.setUserId(usersEntity.getUserId());
        if (usersEntity.getEmail() != null)
            userDto.setEmail(usersEntity.getEmail());
        if (usersEntity.getUsername() != null)
            userDto.setUsername(usersEntity.getUsername());
        if (usersEntity.getPhoneNo() != null)
            userDto.setPhoneNo(usersEntity.getPhoneNo());
        if (usersEntity.getRoles() != null){
            userDto.setRoles(usersEntity.getRoles());
        }

        return userDto;
    }
}
