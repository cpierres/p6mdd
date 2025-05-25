package com.mdd.back.mappers;

import com.mdd.back.entities.PostComment;
import com.mdd.back.models.PostCommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostCommentMapper {
    @Mapping(target = "createdByUsername", ignore = true)
    PostCommentDto commentToCommentDto(PostComment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PostComment commentDtoToComment(PostCommentDto commentDto);

}
