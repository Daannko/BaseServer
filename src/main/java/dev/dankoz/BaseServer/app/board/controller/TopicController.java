package dev.dankoz.BaseServer.app.board.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.dankoz.BaseServer.app.board.dto.TopicCreateRequestDTO;
import dev.dankoz.BaseServer.app.board.dto.TopicRequestDTO;
import dev.dankoz.BaseServer.app.board.model.Topic;
import dev.dankoz.BaseServer.app.board.service.TopicService;
import dev.dankoz.BaseServer.general.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/topic")
public class TopicController {

    private final TopicService topicService;
    private final UserService userService;

    public TopicController(TopicService topicService, UserService userService){
        this.topicService = topicService;
        this.userService = userService;
    }

    @PostMapping
    public Topic addTopic(@Valid @RequestBody TopicCreateRequestDTO request) {
        Integer userId = getUserId();
        return topicService.addTopic(request.title(), request.content(), request.boardId(), request.relatedTopics(), userId);
    }

    @PatchMapping("/{id}")
    public void updateTopic(@PathVariable String id, @Valid @RequestBody TopicRequestDTO request) {
        Integer userId = getUserId();
        topicService.updateTopic(id, request.title(), request.content(), userId);
    }

    @DeleteMapping("/{id}")
    public void removeTopic(@PathVariable String id) {
        Integer userId = getUserId();
        topicService.removeTopic(id, userId);
    }

    @GetMapping("/{id}")
    public Topic getTopic(@PathVariable String id) {
        Integer userId = getUserId();
        return topicService.getTopic(id, userId);
    }

    @GetMapping("/board/{boardId}")
    public List<Topic> getBoardTopics(@PathVariable String boardId) {
        Integer userId = getUserId();
        return topicService.getAllBoardTopics(boardId, userId);
    }

    @PostMapping("/{id}/link/{otherId}")
    public void linkTopics(@PathVariable String id, @PathVariable String otherId) {
        Integer userId = getUserId();
        topicService.linkTopics(id, otherId, userId);
    }

    @DeleteMapping("/{id}/unlink/{otherId}")
    public void unlinkTopics(@PathVariable String id, @PathVariable String otherId) {
        Integer userId = getUserId();
        topicService.unlinkTopics(id, otherId, userId);
    }

    private Integer getUserId(){
        return userService.getCurrentUser().getId();
    }
}
