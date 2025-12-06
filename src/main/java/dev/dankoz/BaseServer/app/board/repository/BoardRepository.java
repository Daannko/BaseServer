package dev.dankoz.BaseServer.app.board.repository;

import com.google.common.base.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import dev.dankoz.BaseServer.app.board.model.Board;

public interface BoardRepository extends MongoRepository<Board,Integer> {

    Optional<Board> findByAuthorId(Integer authorId);
    Optional<Board> findByAuthorIdAndName(Integer autorId,String nameString);

}
