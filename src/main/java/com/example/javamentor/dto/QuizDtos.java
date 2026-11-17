package com.example.javamentor.dto;

import java.time.LocalDateTime;

import com.example.javamentor.entity.QuizAttempt;

/**
 * Small request/response shapes for the quiz API.
 * We never send the QuizAttempt entity itself as JSON (it has a lazy Student inside).
 */
public final class QuizDtos {

	private QuizDtos() {
	}

	/** POST /api/quiz/generate */
	public record GenerateRequest(String topic, String level, Integer count) {
	}

	/** POST /api/quiz/attempts */
	public record AttemptRequest(Long studentId, String topic, String level, int score, int total) {
	}

	/** What the frontend gets back for each attempt */
	public record AttemptResponse(Long id, String topic, String level, int score, int total,
			int percentage, LocalDateTime attemptedAt) {

		public static AttemptResponse from(QuizAttempt a) {
			int pct = a.getTotal() == 0 ? 0 : Math.round(a.getScore() * 100f / a.getTotal());
			return new AttemptResponse(a.getId(), a.getTopic(), a.getLevel(), a.getScore(), a.getTotal(),
					pct, a.getAttemptedAt());
		}
	}
}
