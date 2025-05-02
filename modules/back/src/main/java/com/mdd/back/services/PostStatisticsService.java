package com.mdd.back.services;

import com.mdd.back.models.TopicStatsDto;
import com.mdd.back.repositories.PostCommentRepository;
import com.mdd.back.repositories.PostRepository;
import com.mdd.back.repositories.TopicRepository;
import com.mdd.back.services.interfaces.IPostStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

/**
 * Implémentation du service responsable du calcul et de la gestion des statistiques des posts.
 */
@Service
public class PostStatisticsService implements IPostStatisticsService {
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final TopicStatsNotifier topicStatsNotifier;

    @Autowired
    public PostStatisticsService(TopicRepository topicRepository,
                                 PostRepository postRepository,
                                 PostCommentRepository commentRepository,
                                 TopicStatsNotifier topicStatsNotifier) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.topicStatsNotifier = topicStatsNotifier;
    }

    @Override
    public Flux<TopicStatsDto> getTopicStats() {
        return topicRepository.findAll()
                .flatMap(topic -> {
                    Mono<Long> postCount = postRepository.countByTopicId(topic.getId());
                    Mono<Long> commentCount = commentRepository.countByTopicId(topic.getId());

                    return Mono.zip(postCount, commentCount)
                            .map(tuple -> TopicStatsDto.builder()
                                    .id(topic.getId())
                                    .title(topic.getTitle())
                                    .countPosts(tuple.getT1())
                                    .countComments(tuple.getT2())
                                    .build());
                })
                .sort(Comparator.comparing(TopicStatsDto::getCountPosts)
                        .thenComparing(TopicStatsDto::getCountComments)
                        .reversed());
    }

    @Override
    public Flux<TopicStatsDto> updateAndNotifyTopicStats() {
        return getTopicStats()
                .collectList()
                .doOnSuccess(topicStatsNotifier::updateTopicStats)
                .flatMapMany(Flux::fromIterable);
    }
}