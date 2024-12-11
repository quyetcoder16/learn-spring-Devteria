package quyet.learn.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.dto.response.UserResponse;
import quyet.learn.spring.entity.Users;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "dateOfBirth", source = "dob")
    Users toUsers(UserCreationRequest userCreationRequest);

    UserResponse toUserResponse(Users users);

    @Mapping(target = "dateOfBirth", source = "dob")
    @Mapping(target = "roles", ignore = true)
    void updateUsers(@MappingTarget Users users, UserUpdateRequest userUpdateRequest);
}
