package com.example.javamentor.dto;

/**
 * DTO for chat requests sent as JSON in the request body.
 * Jackson fills it using the no-arg constructor + setters,
 * so the field names must match the JSON keys exactly.
 */
public class ChatRequest {

	private String conversationId;
	private String question;
	private String level;

	public ChatRequest() {
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
