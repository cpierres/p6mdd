package com.mdd.back.mappers;

import com.mdd.back.entities.User;
import com.mdd.back.models.RegisterRequest;
import com.mdd.back.models.UpdateAuthenticatedUserRequest;
import com.mdd.back.models.UserDto;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User registerRequestToUser(RegisterRequest request, String encodedPassword);

    // pour mises à jour partielles avec encodage du mot de passe (encoder récupéré du contexte par injection)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", expression = "java(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : user.getPassword())")
    @Mapping(target = "id", ignore = false)
    @Mapping(source = "created_at", target = "createdAt")
    @Mapping(source = "updated_at", target = "updatedAt")
    void updateAuthenticatedUserFromRequest(UpdateAuthenticatedUserRequest request, @MappingTarget User user, @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "created_at", source = "createdAt")
    @Mapping(target = "updated_at", source = "updatedAt")
    UserDto userToUserDto(User user);

}
