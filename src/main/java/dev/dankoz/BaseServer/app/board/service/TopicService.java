package dev.dankoz.BaseServer.app.board.service;

import java.util.List;

import dev.dankoz.BaseServer.app.board.model.Topic;

public interface TopicService {

    public List<Topic> findBoardTopics(Integer boardId, Integer authorId);
    public void updatTopicContent(Integer topicId, String content, Integer authorId);
    public void updatTopicTitle(Integer topicId, String title, Integer authorId);
    public void updateTopic(Integer topicId, String title, String content, Integer authorId);
    public void removeTopic(Integer topicId, Integer authorId);
    public void linkTopics(Integer firstTopicId, Integer secondTopicId, Integer authorId);
    public void unlinkTopics(Integer firstTopicId, Integer secondTopicId, Integer authorId);

}
