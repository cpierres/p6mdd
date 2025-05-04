package com.mdd.back.mappers;

import com.mdd.back.entities.Topic;
import com.mdd.back.models.TopicDto;
import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TopicMapper {

    TopicDto topicToTopicDto(Topic topic);

    @Mapping(target = "priorityOrder", ignore = true)
    Topic topicDtoToTopic(TopicDto topicDto);

    @Mapping(target = "subscribed", ignore = true) // flag géré dynamiquement dans TopicServices
    @Mapping(target = "countPosts", ignore = true) // stats count gérées dynamiquement
    @Mapping(target = "countComments", ignore = true)
    @Mapping(source = "priorityOrder", target = "priorityOrder") // mapping explicite du champ priorityOrder
    TopicSubscribedForAuthUserDto topicToTopicSubscribedForAuthUserDto(Topic topic);
}
