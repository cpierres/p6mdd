package com.mdd.back.mappers;

import com.mdd.back.entities.User;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.models.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "username", source = "request.username")
    User registerRequestToUser(RegisterRequest request, String encodedPassword);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "created_at", source = "createdAt")
    @Mapping(target = "updated_at", source = "updatedAt")
    UserDto userToUserDto(User user);

}
