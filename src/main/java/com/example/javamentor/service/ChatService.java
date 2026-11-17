package com.example.javamentor.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import com.example.javamentor.dto.ConceptExplanation;

/**
 * Service layer for everything AI.
 * The controller only handles HTTP; all Spring AI logic lives here.
 */
@Service
public class ChatService {

	// One place for the trainer rules, reused by several methods
	private static final String JAVA_TRAINER_RULES = """
			You are a friendly Core Java trainer for beginner students.
			Explain every concept in this order:
			1. Definition (2 lines)
			2. Key points (bullet list)
			3. A real-world example
			4. A short Java code example
			Always use Java, never any other language.
			If the question is not about programming, politely say you only help with Java.
			""";

	// JavaMentor AI's personality, used by the React chat (/chat/memory).
	// {level} is filled with the student's level from their profile.
	private static final String MENTOR_RULES = """
			You are JavaMentor AI, a patient and encouraging mentor for students
			learning Java from scratch. The student's level is: {level}.

			How you teach:
			- Explain step by step in simple words; define every new term the first time you use it.
			- Use a small real-life analogy when it helps understanding.
			- Code examples are always Java, short and complete, in ```java blocks, with comments.
			- Match the student's level: beginners get basics and no jargon,
			  advanced students get deeper details and best practices.
			- When the student shares code with an error, point out the exact line,
			  explain why it fails, and show the fixed code.
			- After teaching a new concept, end with one small "Try it yourself" exercise.
			- For short follow-up questions, give short direct answers.
			- Be warm and motivating; never make the student feel bad for not knowing something.

			Scope: Java, OOP, DSA in Java, Spring Boot and general programming.
			If the question is unrelated, politely say you are a Java mentor and steer back to Java.
			Format answers in Markdown.
			""";

	private static final String SHORT_TRAINER_RULES = """
			You are a friendly Core Java trainer.
			Explain in this order: definition, key points,
			real-world example, short Java code.
			""";

	private final ChatClient chatClient;
	private final MessageChatMemoryAdvisor memoryAdvisor;

	// ChatMemory now stores its messages in MySQL (JDBC chat memory repository)
	public ChatService(ChatClient.Builder builder, ChatMemory chatMemory) {
		this.chatClient = builder.build();
		this.memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
	}

	/** Plain question, no rules, no memory. */
	public String ask(String question) {
		return chatClient.prompt()
				.user(question)
				.call()
				.content();
	}

	/** System prompting: answer follows the trainer rules. */
	public String askAsTrainer(String question) {
		return chatClient.prompt()
				.system(JAVA_TRAINER_RULES)
				.user(question)
				.call()
				.content();
	}

	/** Template prompting: {topic} and {level} filled at runtime. */
	public String explainTopic(String topic, String level) {
		return chatClient.prompt()
				.user(u -> u.text("Explain {topic} for a {level} level student.")
						.param("topic", topic)
						.param("level", level))
				.call()
				.content();
	}

	/** System + template prompting together. */
	public String explainTopicAsTrainer(String topic, String level) {
		return chatClient.prompt()
				.system(SHORT_TRAINER_RULES)
				.user(u -> u.text("Explain {topic} for a {level} level student.")
						.param("topic", topic)
						.param("level", level))
				.call()
				.content();
	}

	/** Chat with memory: earlier messages of this conversation are loaded from MySQL. */
	public String chatWithMemory(String conversationId, String question, String level) {
		String lvl = (level == null || level.isBlank()) ? "Beginner" : level;
		return chatClient.prompt()
				.system(s -> s.text(MENTOR_RULES).param("level", lvl))
				.advisors(memoryAdvisor)
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.user(question)
				.call()
				.content();
	}

	/** Structured output: Gemini fills a ConceptExplanation object. */
	public ConceptExplanation explainStructured(String question, String level) {
		return chatClient.prompt()
				.system("You are a friendly Core Java trainer. Always use Java.")
				.user(u -> u.text("Explain {question} for a {level} level student.")
						.param("question", question)
						.param("level", level))
				.call()
				.entity(ConceptExplanation.class);
	}
}
