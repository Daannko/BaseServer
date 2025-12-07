package dev.dankoz.BaseServer.app.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.dankoz.BaseServer.app.board.model.Board;

@Repository
public interface BoardRepository extends MongoRepository<Board,Integer> {

    List<Board> findAllByAuthorId(Integer authorId);
    Optional<Board> findById(Integer boardId);

}
