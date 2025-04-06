package com.mdd.back.services;

import com.mdd.back.mappers.TopicMapper;
import com.mdd.back.models.TopicDto;
import com.mdd.back.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    /**
     * Récupère tous les Topics et les transforme en TopicDto
     *
     * @return Flux<TopicDto> contenant la liste des topics
     */
    public Flux<TopicDto> getAllTopics() {
        return topicRepository.findAll()
                .map(topicMapper::topicToTopicDto);
    }

}
