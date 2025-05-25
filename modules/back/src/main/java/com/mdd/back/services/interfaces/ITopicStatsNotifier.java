package com.mdd.back.services.interfaces;

import com.mdd.back.models.TopicStatsDto;

import java.util.List;

public interface ITopicStatsNotifier {
    void updateTopicStats(List<TopicStatsDto> topicStats);
}
