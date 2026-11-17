package com.example.javamentor.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.javamentor.dto.ChatRequest;
import com.example.javamentor.dto.ConceptExplanation;
import com.example.javamentor.service.ChatService;

/**
 * Controller = HTTP only: read the request, call the service, return the result.
 * Same URLs as before, so index.html and your Postman tests keep working.
 */
@RestController
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@GetMapping("/chat")
	public String chat(@RequestParam String question) {
		return chatService.ask(question);
	}

	@GetMapping("/chat/system")
	public String chatSystem(@RequestParam String question) {
		return chatService.askAsTrainer(question);
	}

	@GetMapping("/chat/template")
	public String chatTemplate(@RequestParam String topic, @RequestParam String level) {
		return chatService.explainTopic(topic, level);
	}

	@GetMapping("/chat/system/template")
	public String chatSystemTemplate(@RequestParam String topic, @RequestParam String level) {
		return chatService.explainTopicAsTrainer(topic, level);
	}

	@PostMapping("/chat/post")
	public String chatPost(@RequestBody Map<String, String> data) {
		return chatService.ask(data.get("question"));
	}

	@PostMapping("/chat/memory")
	public String chatMemory(@RequestBody ChatRequest request) {
		return chatService.chatWithMemory(request.getConversationId(), request.getQuestion(), request.getLevel());
	}

	@PostMapping("/chat/structured")
	public ConceptExplanation chatStructured(@RequestBody ChatRequest request) {
		return chatService.explainStructured(request.getQuestion(), request.getLevel());
	}
}
