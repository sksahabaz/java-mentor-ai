package com.example.javamentor.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * One finished AI quiz = one row in "quiz_attempts".
 * Many attempts belong to one student (@ManyToOne -> foreign key student_id).
 * These rows are the data behind "My Performance".
 */
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@Column(nullable = false)
	private String topic;

	private String level;

	private int score; // correct answers

	private int total; // number of questions

	@Column(nullable = false)
	private LocalDateTime attemptedAt;

	// Runs automatically just before INSERT
	@PrePersist
	void onCreate() {
		attemptedAt = LocalDateTime.now();
	}

	public QuizAttempt() {
	}

	public Long getId() {
		return id;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public LocalDateTime getAttemptedAt() {
		return attemptedAt;
	}
}
