package dev.dankoz.BaseServer.ai.quiz.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import dev.dankoz.BaseServer.ai.quiz.dto.AnswerSubmission;
import dev.dankoz.BaseServer.ai.quiz.dto.EvaluateRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.EvaluateResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.ExtractTagsRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.ExtractTagsResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.GenerateQuizRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.OneOffEvaluateRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.QuestionResult;
import dev.dankoz.BaseServer.ai.quiz.dto.QuizListResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.QuizResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.QuizSummaryResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.TagCount;
import dev.dankoz.BaseServer.ai.quiz.dto.TagListResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.UpdateQuizRequest;
import dev.dankoz.BaseServer.ai.quiz.model.Question;
import dev.dankoz.BaseServer.ai.quiz.model.Quiz;
import dev.dankoz.BaseServer.ai.quiz.repository.QuizRepository;
import dev.dankoz.BaseServer.ai.quiz.service.QuizGradingService.GradeResult;
import dev.dankoz.BaseServer.config.exceptions.BadRequestException;
import dev.dankoz.BaseServer.config.exceptions.NotFoundException;

/** Quiz orchestration: generate/store, list/filter, fetch, edit, delete, evaluate. */
@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final MongoTemplate mongoTemplate;
    private final QuizGenerationService generationService;
    private final QuizGradingService gradingService;
    private final NoteTagService noteTagService;

    public QuizService(QuizRepository quizRepository, MongoTemplate mongoTemplate,
                       QuizGenerationService generationService, QuizGradingService gradingService,
                       NoteTagService noteTagService) {
        this.quizRepository = quizRepository;
        this.mongoTemplate = mongoTemplate;
        this.generationService = generationService;
        this.gradingService = gradingService;
        this.noteTagService = noteTagService;
    }

    // --- generate ---

    public QuizResponse generate(GenerateQuizRequest request, Integer ownerId) {
        List<String> avoidQuestions = List.of();
        if (Boolean.TRUE.equals(request.avoidExisting())) {
            avoidQuestions = existingQuestionTexts(noteIdsOf(request), ownerId);
        }

        // "Extract tags" mode: resolve per-note topic tags (cached/persisted), then generate from
        // their union instead of the raw note text.
        NoteTagService.NoteTags tagMode = null;
        if (Boolean.TRUE.equals(request.extractTopics())) {
            if (request.notes() == null || request.notes().isEmpty()) {
                throw new BadRequestException("At least one note is required");
            }
            tagMode = noteTagService.extract(request.notes(), ownerId);
        }

        Quiz quiz = generationService.generate(request, ownerId, avoidQuestions, tagMode);
        boolean persist = request.persist() == null || request.persist();
        if (persist) {
            quiz = quizRepository.save(quiz);
        }
        // Taking mode — never expose answer keys in the generate response.
        return QuizResponse.of(quiz, false);
    }

    /** Action 1 of the "extract tags" flow: resolve (cached or freshly extracted) topic tags per note. */
    public ExtractTagsResponse extractTags(ExtractTagsRequest request, Integer ownerId) {
        if (request.notes() == null || request.notes().isEmpty()) {
            throw new BadRequestException("At least one note is required");
        }
        NoteTagService.NoteTags result = noteTagService.extract(request.notes(), ownerId);
        return new ExtractTagsResponse(result.byNote(), result.union());
    }

    /** Prompts of questions from quizzes already linked to these notes (for {@code avoidExisting}). */
    private List<String> existingQuestionTexts(List<String> noteIds, Integer ownerId) {
        if (noteIds.isEmpty()) {
            return List.of();
        }
        Query query = new Query(Criteria.where("ownerId").is(ownerId)
                .and("sourceNoteIds").in(noteIds));
        return mongoTemplate.find(query, Quiz.class).stream()
                .filter(z -> z.getQuestions() != null)
                .flatMap(z -> z.getQuestions().stream())
                .map(Question::getQuestion)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private List<String> noteIdsOf(GenerateQuizRequest request) {
        if (request.notes() == null) {
            return List.of();
        }
        return request.notes().stream()
                .map(dev.dankoz.BaseServer.ai.quiz.dto.NoteInput::id)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // --- list / tags ---

    public QuizListResponse list(String boardId, List<String> noteIds, List<String> tags,
                                 String q, int limit, int offset, Integer ownerId) {
        Criteria criteria = Criteria.where("ownerId").is(ownerId);
        if (boardId != null) {
            criteria.and("boardId").is(boardId);
        }
        if (tags != null && !tags.isEmpty()) {
            criteria.and("tags").all(tags);
        }
        if (noteIds != null && !noteIds.isEmpty()) {
            criteria.and("sourceNoteIds").in(noteIds);
        }
        if (q != null && !q.isBlank()) {
            String regex = ".*" + java.util.regex.Pattern.quote(q.strip()) + ".*";
            criteria.orOperator(
                    Criteria.where("title").regex(regex, "i"),
                    Criteria.where("tags").regex(regex, "i"));
        }

        Query query = new Query(criteria);
        long total = mongoTemplate.count(query, Quiz.class);

        query.with(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .skip(Math.max(0, offset))
                .limit(Math.max(0, limit));
        List<Quiz> quizzes = mongoTemplate.find(query, Quiz.class);

        List<QuizSummaryResponse> summaries = quizzes.stream()
                .map(QuizSummaryResponse::of)
                .toList();
        return new QuizListResponse(summaries, total);
    }

    public TagListResponse listTags(Integer ownerId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("ownerId").is(ownerId)),
                Aggregation.unwind("tags"),
                Aggregation.group("tags").count().as("count"),
                Aggregation.project("count").and("_id").as("tag").andExclude("_id"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")));

        AggregationResults<TagCount> results =
                mongoTemplate.aggregate(aggregation, Quiz.class, TagCount.class);
        return new TagListResponse(results.getMappedResults());
    }

    // --- fetch / edit / delete ---

    public QuizResponse get(String id, Integer ownerId, boolean reveal) {
        return QuizResponse.of(load(id, ownerId), reveal);
    }

    public QuizResponse update(String id, UpdateQuizRequest request, Integer ownerId) {
        Quiz quiz = load(id, ownerId);
        if (request.title() != null) {
            quiz.setTitle(request.title());
        }
        if (request.tags() != null) {
            quiz.setTags(request.tags());
        }
        if (request.questions() != null) {
            quiz.setQuestions(ensureIds(request.questions()));
        }
        quiz.setUpdatedAt(Instant.now());
        quiz = quizRepository.save(quiz);
        return QuizResponse.of(quiz, true);
    }

    public void delete(String id, Integer ownerId) {
        Quiz quiz = load(id, ownerId);
        quizRepository.delete(quiz);
    }

    // --- backfill per-option explanations on an existing quiz ---

    /** Generate per-option explanations for every closed question in the quiz. Returns reveal shape. */
    public QuizResponse explainOptions(String id, Integer ownerId) {
        Quiz quiz = load(id, ownerId);
        if (quiz.getQuestions() != null) {
            quiz.getQuestions().forEach(q -> generationService.explainOptions(q, ownerId));
        }
        quiz.setExplainOptions(true);
        quiz.setUpdatedAt(Instant.now());
        return QuizResponse.of(quizRepository.save(quiz), true);
    }

    /** Generate per-option explanations for one question of the quiz. Returns reveal shape. */
    public QuizResponse explainQuestionOptions(String id, String questionId, Integer ownerId) {
        Quiz quiz = load(id, ownerId);
        Question target = quiz.getQuestions() == null ? null : quiz.getQuestions().stream()
                .filter(q -> questionId.equals(q.getId()))
                .findFirst()
                .orElse(null);
        if (target == null) {
            throw new NotFoundException("Question not found: " + questionId);
        }
        generationService.explainOptions(target, ownerId);
        quiz.setExplainOptions(true);
        quiz.setUpdatedAt(Instant.now());
        return QuizResponse.of(quizRepository.save(quiz), true);
    }

    // --- evaluate ---

    public EvaluateResponse evaluateStored(String id, EvaluateRequest request, Integer ownerId) {
        Quiz quiz = load(id, ownerId);
        return evaluate(quiz.getQuestions(), request.answers(), ownerId);
    }

    public EvaluateResponse evaluateOneOff(OneOffEvaluateRequest request, Integer ownerId) {
        if (request.quiz() == null || request.quiz().questions() == null) {
            throw new BadRequestException("quiz.questions is required");
        }
        return evaluate(request.quiz().questions(), request.answers(), ownerId);
    }

    private EvaluateResponse evaluate(List<Question> questions, List<AnswerSubmission> answers, Integer ownerId) {
        if (questions == null || questions.isEmpty()) {
            throw new BadRequestException("Quiz has no questions");
        }
        Map<String, Object> byId = answers == null ? Map.of() : answers.stream()
                .filter(a -> a.questionId() != null)
                .collect(Collectors.toMap(AnswerSubmission::questionId, a -> a.answer() == null ? "" : a.answer(),
                        (a, b) -> b));

        List<QuestionResult> results = new ArrayList<>();
        double weightedSum = 0.0;
        int totalPoints = 0;

        for (Question question : questions) {
            Object answer = byId.get(question.getId());
            GradeResult grade = gradingService.grade(question, answer, ownerId);
            int weight = question.weight();
            weightedSum += grade.score() * weight;
            totalPoints += weight;

            results.add(new QuestionResult(
                    question.getId(),
                    grade.correct(),
                    round2(grade.score()),
                    grade.correctAnswer(),
                    grade.feedback(),
                    question.getExplanation(),
                    // Post-answer: include options with their per-option explanations.
                    question.getOptions(),
                    question.getSourceNoteIds()));
        }

        double weighted = round2(weightedSum);
        int percentage = totalPoints == 0 ? 0 : (int) Math.round(100.0 * weighted / totalPoints);
        return new EvaluateResponse(results, format(weighted) + "/" + totalPoints, percentage);
    }

    // --- helpers ---

    private Quiz load(String id, Integer ownerId) {
        return quizRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Quiz not found: " + id));
    }

    private List<Question> ensureIds(List<Question> questions) {
        for (Question q : questions) {
            if (q.getId() == null || q.getId().isBlank()) {
                q.setId(UUID.randomUUID().toString());
            }
            if (q.getPoints() == null) {
                q.setPoints(1);
            }
        }
        return questions;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String format(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
