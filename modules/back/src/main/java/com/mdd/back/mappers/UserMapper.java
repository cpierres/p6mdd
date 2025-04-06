package com.mdd.back.mappers;

import com.mdd.back.entities.User;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.models.UpdateAuthenticatedUserRequest;
import com.mdd.back.models.UserDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    User registerRequestToUser(RegisterRequest request, String encodedPassword);

    // pour mises à jour partielles avec encodage du mot de passe (encoder récupéré du contexte par injection)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", expression = "java(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : user.getPassword())")
    void updateAuthenticatedUserFromRequest(UpdateAuthenticatedUserRequest request, @MappingTarget User user, @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "created_at", source = "createdAt")
    @Mapping(target = "updated_at", source = "updatedAt")
    UserDto userToUserDto(User user);

}
