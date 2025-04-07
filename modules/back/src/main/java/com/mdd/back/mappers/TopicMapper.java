package com.mdd.back.mappers;

import com.mdd.back.entities.Topic;
import com.mdd.back.models.TopicDto;
import com.mdd.back.models.TopicSubscribedForAuthUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TopicMapper {

    TopicDto topicToTopicDto(Topic topic);

    Topic topicDtoToTopic(TopicDto topicDto);

    @Mapping(target = "subscribed", ignore = true) // flag géré dynamiquement dans TopicServices
    TopicSubscribedForAuthUserDto topicToTopicSubscribedForAuthUserDto(Topic topic);
}
