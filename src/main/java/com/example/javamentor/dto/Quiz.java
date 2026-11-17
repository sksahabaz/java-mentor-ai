package com.example.javamentor.dto;

import java.util.List;

/**
 * Structured output: Gemini fills this object directly (.entity(Quiz.class)).
 * Spring AI turns these records into a JSON schema and tells the model to follow it.
 */
public record Quiz(String topic, List<Question> questions) {

	/** One multiple-choice question. correctIndex is 0..3 (position in options). */
	public record Question(String question, List<String> options, int correctIndex, String explanation) {
	}
}
