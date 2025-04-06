package com.mdd.back.mappers;

import com.mdd.back.entities.Topic;
import com.mdd.back.models.TopicDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TopicMapper {

    TopicDto topicToTopicDto(Topic topic);

    Topic topicDtoToTopic(TopicDto topicDto);
}
