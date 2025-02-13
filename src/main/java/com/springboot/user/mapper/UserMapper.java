package com.springboot.user.mapper;

import com.springboot.user.dto.UserPatchDto;
import com.springboot.user.dto.UserPostDto;
import com.springboot.user.dto.UserResponseDto;
import com.springboot.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User userPostDtoToUser(UserPostDto userPostDto);
    User userPatchDtoToUser(UserPatchDto userPatchDto);
    UserResponseDto userToUserResponseDto(User user);
    List<UserResponseDto> usersToUsersResponses(List<User> users);
}
