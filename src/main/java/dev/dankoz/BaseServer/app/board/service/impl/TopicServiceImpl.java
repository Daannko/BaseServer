package dev.dankoz.BaseServer.app.board.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.checkerframework.checker.units.qual.min;
import org.springframework.stereotype.Service;

import com.mongodb.DuplicateKeyException;

import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.model.Topic;
import dev.dankoz.BaseServer.app.board.repository.TopicRepository;
import dev.dankoz.BaseServer.app.board.service.BoardService;
import dev.dankoz.BaseServer.app.board.service.TopicService;
import dev.dankoz.BaseServer.config.exceptions.UnauthorizedException;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final BoardService boardService;

    public TopicServiceImpl(TopicRepository topicRepository, BoardService boardService){
        this.topicRepository = topicRepository;
        this.boardService = boardService;
    }
    
    @Override
    public List<Topic> getAllBoardTopics(String boardId, Integer authorId) {
        boardService.getBoard(boardId, authorId);
        return topicRepository.findAllByBoardId(boardId);
    }

    @Override
    public Topic getTopic(String topicId, Integer authorId) {
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if(optionalTopic.isEmpty() || !authorId.equals(optionalTopic.get().getAuthorId())){
            throw new UnauthorizedException("The topic does not exist or you do not have access to it");
        }
        return optionalTopic.get();
    }

    @Override
    public void updatTopicContent(String topicId, String content, Integer authorId) {
        Topic topic = getTopic(topicId, authorId);
        topic.setContent(content);

        topicRepository.save(topic);
    }

    @Override
    public void updatTopicTitle(String topicId, String title, Integer authorId) {
        Topic topic = getTopic(topicId, authorId);
        topic.setTitle(title);
        topicRepository.save(topic);
    }

    @Override
    public void updateTopic(String topicId, String title, String content, Integer authorId) {
        Topic topic =  getTopic(topicId, authorId);

        if(title != null  && !title.isBlank())
        topic.setTitle(title);
        if(content != null && !content.isBlank())   
        topic.setContent(content);
        topicRepository.save(topic);
    }

    @Override
    public void removeTopic(String topicId, Integer authorId) {
        Topic topic = getTopic(topicId, authorId);
        Board board = boardService.getBoard(topic.getBoardId(), authorId);

        board.removeTopic(topic.getId());
        boardService.saveBoard(board);

        topicRepository.deleteById(topic.getId());
    }

    @Override
    public void linkTopics(String firstTopicId, String secondTopicId, Integer authorId) {
        Topic topicA = getTopic(firstTopicId, authorId);
        Topic topicB = getTopic(secondTopicId, authorId);
        topicA.addLink(topicB.getId());
        topicB.addLink(topicA.getId());

        topicRepository.save(topicA);
        topicRepository.save(topicB);
    }

    @Override
    public void unlinkTopics(String firstTopicId, String secondTopicId, Integer authorId) {
        Topic topicA = getTopic(firstTopicId, authorId);
        Topic topicB = getTopic(secondTopicId, authorId);
        topicA.removeLink(topicB.getId());
        topicB.removeLink(topicA.getId());

        topicRepository.save(topicA);
        topicRepository.save(topicB);
    }

    @Override
    public Topic addTopic(String topicTitle, String topicContent, String boardId, List<String> links, Integer authorId) {
        Board board = boardService.getBoard(boardId, authorId);

        Topic newTopic = Topic.builder()
        .title(topicTitle)
        .content(topicContent)
        .authorId(authorId)
        .boardId(boardId)
        .tier(0L)
        .build();

        int attempts = 0;
        while(attempts < 3) {
            attempts++;
            newTopic.setId(new ObjectId().toHexString());
            try{
                topicRepository.insert(newTopic);
                break;
            } catch(DuplicateKeyException ex){
                if(attempts >= 3) throw ex;
            }
        }

        List<Topic> topicsToSave = new ArrayList<>();
        topicsToSave.add(newTopic);


        if(links != null && !links.isEmpty()){
            Long minTier = Long.MAX_VALUE;
            for(String link: links){
                Topic linkTopic = getTopic(link,authorId);

                if(minTier < linkTopic.getTier()) minTier = linkTopic.getTier();

                newTopic.addLink(link);
                linkTopic.addLink(newTopic.getId());
                topicsToSave.add(linkTopic);
            }
            newTopic.setTier(minTier);
        }

        topicRepository.saveAll(topicsToSave);
        
        board.addTopic(newTopic.getId());
        boardService.saveBoard(board);
        
        return newTopic;
    } 
        


}  
