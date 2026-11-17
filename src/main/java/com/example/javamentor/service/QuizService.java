package com.example.javamentor.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.javamentor.dto.Quiz;
import com.example.javamentor.dto.QuizDtos.AttemptRequest;
import com.example.javamentor.dto.QuizDtos.AttemptResponse;
import com.example.javamentor.entity.QuizAttempt;
import com.example.javamentor.entity.Student;
import com.example.javamentor.repository.QuizAttemptRepository;

/**
 * AI Quiz + My Performance.
 *   generate()  -> Gemini creates MCQs (structured output)
 *   saveAttempt -> store the score in MySQL
 *   getAttempts -> history for the Performance page
 *   studyTips() -> Gemini reads the history and gives advice
 */
@Service
public class QuizService {

	private static final String QUIZ_RULES = """
			You are JavaMentor AI, writing practice quizzes for students learning Java.
			Rules:
			- Questions test understanding, not trick wording; match the requested level.
			- Mix concept questions with "what does this code print?" questions.
			- Put code inside the question using Markdown backticks.
			- Every question has exactly 4 options.
			- Exactly one option is correct; correctIndex is its position (0, 1, 2 or 3).
			- Vary the position of the correct answer.
			- explanation: 1-2 sentences on why the answer is correct.
			- Any code must be Java.
			""";

	private final ChatClient chatClient;
	private final QuizAttemptRepository attemptRepository;
	private final StudentService studentService;

	public QuizService(ChatClient.Builder builder, QuizAttemptRepository attemptRepository,
			StudentService studentService) {
		this.chatClient = builder.build();
		this.attemptRepository = attemptRepository;
		this.studentService = studentService;
	}

	public Quiz generate(String topic, String level, Integer count) {
		int n = (count == null) ? 5 : Math.max(3, Math.min(count, 10)); // 3..10 questions
		String lvl = (level == null || level.isBlank()) ? "beginner" : level;

		// The AI sometimes returns broken JSON or is briefly overloaded -> try up to 3 times
		RuntimeException lastError = null;
		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				Quiz quiz = chatClient.prompt()
						.system(QUIZ_RULES)
						.user(u -> u.text("Create {count} multiple-choice questions about {topic} for a {level} student.")
								.param("count", n)
								.param("topic", topic)
								.param("level", lvl))
						.call()
						.entity(Quiz.class);

				// Never trust AI output blindly: drop broken questions
				List<Quiz.Question> valid = quiz.questions().stream()
						.filter(q -> q.options() != null && q.options().size() == 4)
						.filter(q -> q.correctIndex() >= 0 && q.correctIndex() < 4)
						.toList();
				if (!valid.isEmpty()) {
					return new Quiz(topic, valid);
				}
			} catch (RuntimeException e) {
				lastError = e;
			}
		}
		throw new IllegalStateException("AI could not create a quiz. Please try again.", lastError);
	}

	public AttemptResponse saveAttempt(AttemptRequest req) {
		Student student = studentService.getById(req.studentId()); // 404 if unknown
		QuizAttempt a = new QuizAttempt();
		a.setStudent(student);
		a.setTopic(req.topic());
		a.setLevel(req.level());
		a.setScore(req.score());
		a.setTotal(req.total());
		return AttemptResponse.from(attemptRepository.save(a));
	}

	public List<AttemptResponse> getAttempts(Long studentId) {
		studentService.getById(studentId); // 404 if unknown
		return attemptRepository.findByStudentIdOrderByAttemptedAtDesc(studentId).stream()
				.map(AttemptResponse::from)
				.toList();
	}

	public String studyTips(Long studentId) {
		Student student = studentService.getById(studentId);
		List<AttemptResponse> attempts = getAttempts(studentId);
		if (attempts.isEmpty()) {
			return "Take your first AI quiz, and I'll give you personal study tips based on your results.";
		}

		// Turn the history into plain text lines the model can read
		String history = attempts.stream()
				.limit(30)
				.map(a -> "- %s (%s): %d/%d = %d%%".formatted(a.topic(), a.level(), a.score(), a.total(), a.percentage()))
				.collect(Collectors.joining("\n"));

		return chatClient.prompt()
				.system("""
						You are JavaMentor AI, a supportive Java mentor. Analyse the student's quiz results.
						Reply in Markdown with three short sections:
						**Strengths**, **Needs work**, **Next steps** (3 concrete study actions).
						Keep it under 150 words and speak directly to the student.
						""")
				.user(u -> u.text("Student: {name}, course: {course}.\nQuiz results (newest first):\n{history}")
						.param("name", student.getName())
						.param("course", student.getCourse() == null ? "not set" : student.getCourse())
						.param("history", history))
				.call()
				.content();
	}
}
