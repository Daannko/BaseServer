package dev.dankoz.BaseServer.app.board.repository;

import org.springframework.stereotype.Repository;

@Repository
public class BoardRepository extends CrudRepository<Board,Integer> {

    Optional<Board> findByAuthorId(Integer authorId);

}
