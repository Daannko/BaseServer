package dev.dankoz.BaseServer.ai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.dankoz.BaseServer.ai.model.ChatSession;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {

    /** Owner-scoped lookup so users can only touch their own sessions. */
    Optional<ChatSession> findByIdAndOwnerId(String id, Integer ownerId);

    List<ChatSession> findAllByOwnerId(Integer ownerId);

    /** Board chat sessions for a user, newest activity first. */
    List<ChatSession> findByOwnerIdAndBoardIdOrderByUpdatedAtDesc(Integer ownerId, String boardId);
}
