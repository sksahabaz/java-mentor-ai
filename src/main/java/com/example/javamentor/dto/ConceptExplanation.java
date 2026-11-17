package com.example.javamentor.dto;

import java.util.List;

/**
 * Structured output: Gemini fills these fields (see ChatService.explainStructured).
 */
public record ConceptExplanation(
		String topic,
		String definition,
		List<String> keyPoints,
		String realWorldExample,
		String javaCode) {
}
