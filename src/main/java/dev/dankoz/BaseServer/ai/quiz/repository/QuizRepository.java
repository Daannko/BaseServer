package dev.dankoz.BaseServer.ai.quiz.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.dankoz.BaseServer.ai.quiz.model.Quiz;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    /** Owner-scoped lookup so users can only touch their own quizzes. */
    Optional<Quiz> findByIdAndOwnerId(String id, Integer ownerId);
}
