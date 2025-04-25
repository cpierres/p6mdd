package com.mdd.back.services;

import com.mdd.back.models.TopicStatsDto;

import java.util.List;

public interface TopicStatsNotifier {
    void updateTopicStats(List<TopicStatsDto> topicStats);
}
