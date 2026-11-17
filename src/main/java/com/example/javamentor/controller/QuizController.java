package com.example.javamentor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.javamentor.dto.Quiz;
import com.example.javamentor.dto.QuizDtos.AttemptRequest;
import com.example.javamentor.dto.QuizDtos.AttemptResponse;
import com.example.javamentor.dto.QuizDtos.GenerateRequest;
import com.example.javamentor.service.QuizService;

/**
 *   POST /api/quiz/generate            -> AI creates a quiz
 *   POST /api/quiz/attempts            -> save a finished quiz score (201)
 *   GET  /api/quiz/attempts/{studentId} -> my quiz history
 *   GET  /api/quiz/tips/{studentId}    -> AI study tips from my history
 */
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

	private final QuizService quizService;

	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}

	@PostMapping("/generate")
	public Quiz generate(@RequestBody GenerateRequest req) {
		return quizService.generate(req.topic(), req.level(), req.count());
	}

	@PostMapping("/attempts")
	@ResponseStatus(HttpStatus.CREATED)
	public AttemptResponse saveAttempt(@RequestBody AttemptRequest req) {
		return quizService.saveAttempt(req);
	}

	@GetMapping("/attempts/{studentId}")
	public List<AttemptResponse> attempts(@PathVariable Long studentId) {
		return quizService.getAttempts(studentId);
	}

	@GetMapping("/tips/{studentId}")
	public String tips(@PathVariable Long studentId) {
		return quizService.studyTips(studentId);
	}
}
