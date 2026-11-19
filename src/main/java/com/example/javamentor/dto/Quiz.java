package com.example.javamentor.dto;

import java.util.List;

public record Quiz(String topic, List<Question> questions) {
	public record Question(String question, List<String> options, int correctIndex, String explanation) {
	}
}
