package com.mdd.back.mappers;

import com.mdd.back.entities.Post;
import com.mdd.back.models.PostDto;
import com.mdd.back.models.PostImportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

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

    @Mapping(source = "createdByUsername", target = "username")
    PostImportDto postDtoToPostImportDto(PostDto postDto);

    List<PostImportDto> postDtoListToPostImportDtoList(List<PostDto> postDtos);

}
