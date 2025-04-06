package com.mdd.back.controller;

import com.mdd.back.models.TopicDto;
import com.mdd.back.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /**
     * Endpoint pour obtenir la liste des topics
     *
     * @return ResponseEntity avec le statut HTTP 200 et la liste des topics
     */
    @GetMapping
    public Mono<ResponseEntity<List<TopicDto>>> getAllTopics() {
        return topicService.getAllTopics()
                .collectList() // Convertir Flux<TopicDto> en List<TopicDto>
                .map(ResponseEntity::ok) // Retourner une réponse 200 avec le corps
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }


}

