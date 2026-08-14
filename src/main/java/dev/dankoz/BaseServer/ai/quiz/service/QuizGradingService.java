package dev.dankoz.BaseServer.ai.quiz.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dankoz.BaseServer.ai.AiService;
import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.quiz.model.AnswerKey;
import dev.dankoz.BaseServer.ai.quiz.model.Question;
import org.springframework.stereotype.Service;

/** Grades a single answer per question type. OPEN / rubric FILL_BLANK use an AI judge. */
@Service
public class QuizGradingService {

    /** Threshold above which an AI-graded answer counts as {@code correct}. */
    private static final double OPEN_PASS = 0.6;

    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizGradingService(AiService aiService) {
        this.aiService = aiService;
    }

    /** {@code userId} attributes any AI-judge token usage to that user; may be null. */
    public GradeResult grade(Question question, Object answer, Integer userId) {
        AnswerKey key = question.getAnswerKey();
        if (key == null) {
            return new GradeResult(0.0, false, null, null);
        }
        return switch (question.getType()) {
            case SINGLE_CHOICE, TRUE_FALSE -> gradeSingle(key, answer);
            case MULTIPLE_ANSWER -> gradeMultiple(key, answer);
            case ORDERING -> gradeOrdering(key, answer);
            case MATCHING -> gradeMatching(key, answer);
            case FILL_BLANK -> gradeFillBlank(question, key, answer, userId);
            case OPEN -> gradeOpen(question, key, answer, userId);
        };
    }

    // --- exact-match types ---

    private GradeResult gradeSingle(AnswerKey key, Object answer) {
        String expected = first(key.getKeys());
        boolean ok = expected != null && expected.equalsIgnoreCase(str(answer));
        return new GradeResult(ok ? 1.0 : 0.0, ok, key.getKeys(), null);
    }

    private GradeResult gradeMultiple(AnswerKey key, Object answer) {
        Set<String> correct = lowerSet(key.getKeys());
        Set<String> picked = lowerSet(strList(answer));
        if (correct.isEmpty()) {
            return new GradeResult(0.0, false, key.getKeys(), null);
        }
        long right = picked.stream().filter(correct::contains).count();
        long wrong = picked.stream().filter(p -> !correct.contains(p)).count();
        double score = Math.max(0.0, (double) (right - wrong) / correct.size());
        score = Math.min(1.0, score);
        boolean exact = picked.equals(correct);
        return new GradeResult(score, exact, key.getKeys(), null);
    }

    private GradeResult gradeOrdering(AnswerKey key, Object answer) {
        List<String> expected = key.getOrder();
        List<String> got = strList(answer);
        if (expected == null || expected.isEmpty()) {
            return new GradeResult(0.0, false, expected, null);
        }
        int correct = 0;
        for (int i = 0; i < expected.size(); i++) {
            if (i < got.size() && expected.get(i).equalsIgnoreCase(got.get(i))) {
                correct++;
            }
        }
        double score = (double) correct / expected.size();
        return new GradeResult(score, correct == expected.size() && got.size() == expected.size(),
                expected, null);
    }

    private GradeResult gradeMatching(AnswerKey key, Object answer) {
        Map<String, String> expected = key.getPairs();
        Map<String, String> got = strMap(answer);
        if (expected == null || expected.isEmpty()) {
            return new GradeResult(0.0, false, expected, null);
        }
        long correct = expected.entrySet().stream()
                .filter(e -> e.getValue() != null
                        && e.getValue().equalsIgnoreCase(got.get(e.getKey())))
                .count();
        double score = (double) correct / expected.size();
        return new GradeResult(score, correct == expected.size(), expected, null);
    }

    private GradeResult gradeFillBlank(Question question, AnswerKey key, Object answer, Integer userId) {
        // Rubric present => let the AI tolerate synonyms.
        if (key.getRubric() != null && !key.getRubric().isBlank()) {
            return gradeOpen(question, key, answer, userId);
        }
        List<String> expected = key.getKeys();
        List<String> got = strList(answer);
        if (expected == null || expected.isEmpty()) {
            return new GradeResult(0.0, false, expected, null);
        }
        int correct = 0;
        for (int i = 0; i < expected.size(); i++) {
            if (i < got.size() && expected.get(i) != null
                    && expected.get(i).strip().equalsIgnoreCase(got.get(i).strip())) {
                correct++;
            }
        }
        double score = (double) correct / expected.size();
        return new GradeResult(score, correct == expected.size(), expected, null);
    }

    // --- AI-graded ---

    private GradeResult gradeOpen(Question question, AnswerKey key, Object answer, Integer userId) {
        String userAnswer = str(answer);
        Object canonical = key.getSampleAnswer() != null ? key.getSampleAnswer() : key.getKeys();
        if (userAnswer == null || userAnswer.isBlank()) {
            return new GradeResult(0.0, false, canonical, "No answer provided.");
        }
        try {
            ChatResponse reply = aiService.chatStatelessFor(new ChatRequest(
                    List.of(
                            ChatMessage.system(judgeSystemPrompt()),
                            ChatMessage.user(judgeUserPrompt(question, key, userAnswer))),
                    null, 0.0, null, null, "json_object", null, null),
                    userId, "QUIZ_EVALUATE");
            JsonNode node = objectMapper.readTree(reply.content());
            double score = clamp01(node.path("score").asDouble(0.0));
            String feedback = node.path("feedback").asText(null);
            return new GradeResult(score, score >= OPEN_PASS, canonical, feedback);
        } catch (Exception e) {
            // Don't fail the whole evaluation on a judge error — mark unscored.
            return new GradeResult(0.0, false, canonical, "Could not grade automatically.");
        }
    }

    private String judgeSystemPrompt() {
        return """
                You grade a student's answer against a rubric. Respond with a single JSON object only:
                {"score": number between 0 and 1, "feedback": short string explaining the score}.
                Be fair: award partial credit when the answer is partially correct.
                In feedback you may use a markdown subset (inline `code`, fenced ```code blocks```,
                **bold**, *italic*, newlines); wrap code/identifiers in inline code. When the answer is
                about code/programming and a short snippet would clarify the feedback (e.g. the correct
                form), include a minimal example in a fenced ```lang ... ``` block (a few correct lines);
                never add code for non-coding answers. Keep markdown inside the JSON string. Return JSON only.
                """;
    }

    private String judgeUserPrompt(Question question, AnswerKey key, String userAnswer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Question: ").append(question.getQuestion()).append("\n");
        if (key.getRubric() != null && !key.getRubric().isBlank()) {
            sb.append("Rubric (what a correct answer must contain): ").append(key.getRubric()).append("\n");
        }
        if (key.getSampleAnswer() != null && !key.getSampleAnswer().isBlank()) {
            sb.append("Reference answer: ").append(key.getSampleAnswer()).append("\n");
        }
        sb.append("Student answer: ").append(userAnswer).append("\n");
        sb.append("Grade it as json with score (0-1) and feedback.");
        return sb.toString();
    }

    // --- coercion helpers ---

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private List<String> strList(Object o) {
        if (o instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (o == null) {
            return List.of();
        }
        return List.of(String.valueOf(o));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> strMap(Object o) {
        if (o instanceof Map<?, ?> map) {
            return map.entrySet().stream().collect(Collectors.toMap(
                    e -> String.valueOf(e.getKey()),
                    e -> String.valueOf(e.getValue())));
        }
        return Map.of();
    }

    private String first(List<String> list) {
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private Set<String> lowerSet(List<String> list) {
        Set<String> set = new HashSet<>();
        if (list != null) {
            for (String s : list) {
                if (s != null) {
                    set.add(s.strip().toLowerCase());
                }
            }
        }
        return set;
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /** Result of grading one question. */
    public record GradeResult(double score, boolean correct, Object correctAnswer, String feedback) {
    }
}
