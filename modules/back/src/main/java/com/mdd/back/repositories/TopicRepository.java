package com.mdd.back.repositories;

import com.mdd.back.entities.Topic;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TopicRepository extends ReactiveCrudRepository<Topic, UUID> {
}

