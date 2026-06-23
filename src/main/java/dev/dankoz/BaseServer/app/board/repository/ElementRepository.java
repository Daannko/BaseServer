package dev.dankoz.BaseServer.app.board.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.dankoz.BaseServer.app.board.model.Element;

@Repository
public interface ElementRepository extends MongoRepository<Element, String> {

    List<Element> findAllByBoardId(String boardId);
}
