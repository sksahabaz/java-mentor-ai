package com.example.javamentor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.javamentor.dto.Quiz;
import com.example.javamentor.dto.QuizDtos.AttemptRequest;
import com.example.javamentor.dto.QuizDtos.AttemptResponse;
import com.example.javamentor.dto.QuizDtos.GenerateRequest;
import com.example.javamentor.security.CurrentUser;
import com.example.javamentor.service.QuizService;

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
	public AttemptResponse saveAttempt(@AuthenticationPrincipal Jwt jwt, @RequestBody AttemptRequest req) {
		return quizService.saveAttempt(CurrentUser.studentId(jwt), req);
	}

	@GetMapping("/attempts")
	public List<AttemptResponse> attempts(@AuthenticationPrincipal Jwt jwt) {
		return quizService.getAttempts(CurrentUser.studentId(jwt));
	}

	@GetMapping("/tips")
	public String tips(@AuthenticationPrincipal Jwt jwt) {
		return quizService.studyTips(CurrentUser.studentId(jwt));
	}
}
