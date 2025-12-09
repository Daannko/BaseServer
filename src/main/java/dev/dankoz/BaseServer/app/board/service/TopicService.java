package dev.dankoz.BaseServer.app.board.service;

import java.util.List;
import java.util.Set;

import dev.dankoz.BaseServer.app.board.model.Topic;

public interface TopicService {

    public Topic addTopic( String topicName, String topicContent, String boardId, List<String> links, Integer authorId);
    public List<Topic> getAllBoardTopics(String boardId, Integer authorId);
    public Topic getTopic(String topicId, Integer authorId);
    public void updatTopicContent(String topicId, String content, Integer authorId);
    public void updatTopicTitle(String topicId, String title, Integer authorId);
    public void updateTopic(String topicId, String title, String content, Integer authorId);
    public void removeTopic(String topicId, Integer authorId);
    public void linkTopics(String firstTopicId, String secondTopicId, Integer authorId);
    public void unlinkTopics(String firstTopicId, String secondTopicId, Integer authorId);

}
