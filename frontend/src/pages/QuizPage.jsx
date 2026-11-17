import { useState } from "react";
import Markdown from "../components/Markdown.jsx";
import { generateQuiz, saveAttempt } from "../api.js";

const TOPICS = [
  "Variables & Data Types", "Operators", "If-Else & Switch", "Loops", "Arrays",
  "Strings", "Methods", "OOP Basics", "Inheritance", "Polymorphism",
  "Interfaces & Abstract Classes", "Exception Handling", "Collections", "Java 8 Streams",
];
const LEVELS = ["Beginner", "Intermediate", "Advanced"];
const COUNTS = [5, 10];
const LETTERS = ["A", "B", "C", "D"];

// Stages: setup -> loading -> playing -> result
export default function QuizPage({ student, onNavigate }) {
  const [stage, setStage] = useState("setup");
  const [topic, setTopic] = useState("");
  const [level, setLevel] = useState(student.level || "Beginner");
  const [count, setCount] = useState(5);
  const [quiz, setQuiz] = useState(null);
  const [index, setIndex] = useState(0); // current question
  const [answers, setAnswers] = useState([]); // chosen option per question
  const [error, setError] = useState(null);
  const [saveState, setSaveState] = useState(null); // saving | saved | failed

  const start = async (chosenTopic = topic) => {
    const t = chosenTopic.trim();
    if (!t) return setError("Pick a topic or type your own.");
    setTopic(t);
    setError(null);
    setStage("loading");
    try {
      const q = await generateQuiz(t, level, count); // POST /api/quiz/generate
      if (!q.questions?.length) throw new Error("The quiz came back empty. Please try again.");
      setQuiz(q);
      setIndex(0);
      setAnswers([]);
      setStage("playing");
    } catch (err) {
      setError(err.message);
      setStage("setup");
    }
  };

  const choose = (optionIndex) => {
    if (answers[index] !== undefined) return; // already answered
    const next = [...answers];
    next[index] = optionIndex;
    setAnswers(next);
  };

  const score = quiz ? answers.filter((a, i) => a === quiz.questions[i].correctIndex).length : 0;

  const nextQuestion = async () => {
    if (index < quiz.questions.length - 1) {
      setIndex(index + 1);
      return;
    }
    // Last question -> show result and save the score (feeds the Dashboard)
    setStage("result");
    setSaveState("saving");
    try {
      await saveAttempt({ studentId: student.id, topic, level, score, total: quiz.questions.length });
      setSaveState("saved");
    } catch {
      setSaveState("failed");
    }
  };

  const reset = () => {
    setStage("setup");
    setQuiz(null);
    setSaveState(null);
  };

  // ---------- Setup ----------
  if (stage === "setup") {
    return (
      <section className="page">
        <div className="page-inner narrow">
          <div className="page-head">
            <div>
              <h2>AI Quiz 📝</h2>
              <p>JavaMentor writes a fresh quiz for you. Every score is saved to your dashboard.</p>
            </div>
          </div>

          <div className="card">
            <h3>1. Choose a topic</h3>
            <div className="chips">
              {TOPICS.map((t) => (
                <button key={t} className={`chip ${topic === t ? "selected" : ""}`} onClick={() => setTopic(t)}>
                  {t}
                </button>
              ))}
            </div>
            <input
              className="topic-input"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && start()}
              placeholder="…or type any Java topic, e.g. “static keyword”"
            />

            <div className="quiz-options">
              <div>
                <h3>2. Level</h3>
                <div className="segmented">
                  {LEVELS.map((l) => (
                    <button key={l} className={level === l ? "selected" : ""} onClick={() => setLevel(l)}>
                      {l}
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <h3>3. Questions</h3>
                <div className="segmented">
                  {COUNTS.map((c) => (
                    <button key={c} className={count === c ? "selected" : ""} onClick={() => setCount(c)}>
                      {c}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {error && <div className="alert">⚠️ {error}</div>}

            <button className="btn-primary wide" onClick={() => start()}>
              Start quiz →
            </button>
          </div>
        </div>
      </section>
    );
  }

  // ---------- Loading ----------
  if (stage === "loading") {
    return (
      <section className="page">
        <div className="loading-state">
          <div className="spinner" />
          <h3>JavaMentor is writing your quiz…</h3>
          <p className="muted">
            {count} {level.toLowerCase()} questions on <strong>{topic}</strong>. This takes a few seconds.
          </p>
        </div>
      </section>
    );
  }

  // ---------- Playing ----------
  if (stage === "playing") {
    const q = quiz.questions[index];
    const chosen = answers[index];
    const answered = chosen !== undefined;
    const isLast = index === quiz.questions.length - 1;

    return (
      <section className="page">
        <div className="page-inner narrow">
          <div className="quiz-top">
            <span className="pill accent">{topic}</span>
            <span className="muted">
              Question {index + 1} of {quiz.questions.length}
            </span>
          </div>
          <div className="progress">
            <div style={{ width: `${((index + (answered ? 1 : 0)) / quiz.questions.length) * 100}%` }} />
          </div>

          <div className="card question-card">
            <Markdown text={q.question} className="question-text" />

            <div className="options">
              {q.options.map((opt, i) => {
                let state = "";
                if (answered) {
                  if (i === q.correctIndex) state = "correct";
                  else if (i === chosen) state = "wrong";
                  else state = "dim";
                }
                return (
                  <button key={i} className={`option ${state}`} onClick={() => choose(i)} disabled={answered}>
                    <span className="letter">{LETTERS[i]}</span>
                    <Markdown text={opt} inline />
                    {state === "correct" && <span className="mark">✓</span>}
                    {state === "wrong" && <span className="mark">✕</span>}
                  </button>
                );
              })}
            </div>

            {answered && (
              <div className={`explanation ${chosen === q.correctIndex ? "good" : "bad"}`}>
                <strong>{chosen === q.correctIndex ? "🎉 Correct!" : "Not quite."}</strong>{" "}
                <Markdown text={q.explanation} inline />
              </div>
            )}

            {answered && (
              <button className="btn-primary wide" onClick={nextQuestion}>
                {isLast ? "See my result →" : "Next question →"}
              </button>
            )}
          </div>
        </div>
      </section>
    );
  }

  // ---------- Result ----------
  const total = quiz.questions.length;
  const pct = Math.round((score / total) * 100);
  const verdict =
    pct >= 80 ? "Excellent work! 🏆" : pct >= 60 ? "Good job! Keep going 💪" : "Keep practising, you'll get there 📚";

  return (
    <section className="page">
      <div className="page-inner narrow">
        <div className="card result-card">
          <div className={`score-ring ${pct >= 60 ? "pass" : "fail"}`} style={{ "--pct": pct }}>
            <span>{pct}%</span>
          </div>
          <h2>{verdict}</h2>
          <p className="muted">
            You got <strong>{score}</strong> of <strong>{total}</strong> right on {topic} ({level}).
          </p>
          <p className="muted small">
            {saveState === "saving" && "Saving your score…"}
            {saveState === "saved" && "✓ Saved to your dashboard"}
            {saveState === "failed" && "⚠️ Couldn't save this score (is the backend running?)"}
          </p>
          <div className="result-actions">
            <button className="btn-primary" onClick={() => start(topic)}>
              ↻ New quiz on {topic}
            </button>
            <button className="btn-ghost" onClick={reset}>
              Choose another topic
            </button>
            <button className="btn-ghost" onClick={() => onNavigate("dashboard")}>
              📊 View my progress
            </button>
          </div>
        </div>

        <h3 className="review-title">Review your answers</h3>
        {quiz.questions.map((q, i) => {
          const right = answers[i] === q.correctIndex;
          return (
            <div key={i} className={`card review-item ${right ? "good" : "bad"}`}>
              <div className="review-head">
                <span className="review-mark">{right ? "✓" : "✕"}</span>
                <Markdown text={q.question} />
              </div>
              {!right && (
                <p className="small">
                  Your answer: <Markdown text={q.options[answers[i]]} inline />
                </p>
              )}
              <p className="small">
                Correct answer: <strong><Markdown text={q.options[q.correctIndex]} inline /></strong>
              </p>
              <p className="muted small">
                <Markdown text={q.explanation} inline />
              </p>
            </div>
          );
        })}
      </div>
    </section>
  );
}
