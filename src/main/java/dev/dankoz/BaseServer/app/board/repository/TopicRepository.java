package dev.dankoz.BaseServer.app.board.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.google.common.base.Optional;

import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.model.Topic;

@Repository
public interface TopicRepository extends MongoRepository<Topic,Integer>{

    Optional<Topic> findByBoard(Board board);
    


}
