package com.mdd.back.mappers;

import com.mdd.back.entities.Post;
import com.mdd.back.models.PostDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "topicTitle", ignore = true)
    @Mapping(target = "createdByUsername", ignore = true)
    @Mapping(target = "updatable", ignore = true)
    PostDto postToPostDto(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post postDtoToPost(PostDto postDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePostFromDto(PostDto postDto, @MappingTarget Post post);
}
