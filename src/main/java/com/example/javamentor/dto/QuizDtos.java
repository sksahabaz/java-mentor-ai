package com.example.javamentor.dto;

import java.time.LocalDateTime;

import com.example.javamentor.entity.QuizAttempt;

public final class QuizDtos {
	private QuizDtos() {
	}

	public record GenerateRequest(String topic, String level, Integer count) {
	}

	public record AttemptRequest(String topic, String level, int score, int total) {
	}

	public record AttemptResponse(Long id, String topic, String level, int score, int total,
			int percentage, LocalDateTime attemptedAt) {
		public static AttemptResponse from(QuizAttempt a) {
			int pct = a.getTotal() == 0 ? 0 : Math.round(a.getScore() * 100f / a.getTotal());
			return new AttemptResponse(a.getId(), a.getTopic(), a.getLevel(), a.getScore(), a.getTotal(),
					pct, a.getAttemptedAt());
		}
	}
}
