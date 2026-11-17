import { useEffect, useState } from "react";
import Markdown from "../components/Markdown.jsx";
import { getAttempts, getStudyTips } from "../api.js";

// Student dashboard = my performance.
// One GET /api/quiz/attempts/{id}; every number below is calculated here in React.
export default function Dashboard({ student, onNavigate }) {
  const [attempts, setAttempts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tips, setTips] = useState(null);
  const [tipsLoading, setTipsLoading] = useState(false);
  const [tipsError, setTipsError] = useState(null);

  useEffect(() => {
    getAttempts(student.id)
      .then(setAttempts) // newest first
      .catch(() => setError("Couldn't load your progress. Is the backend running on port 8080?"))
      .finally(() => setLoading(false));
  }, [student.id]);

  const loadTips = async () => {
    setTipsLoading(true);
    setTipsError(null);
    try {
      setTips(await getStudyTips(student.id)); // AI reads my quiz history
    } catch (err) {
      setTipsError(err.message);
    } finally {
      setTipsLoading(false);
    }
  };

  // ---------- Calculations ----------
  const quizzes = attempts.length;
  const answered = attempts.reduce((sum, a) => sum + a.total, 0);
  const correct = attempts.reduce((sum, a) => sum + a.score, 0);
  const accuracy = answered ? Math.round((correct / answered) * 100) : 0;
  const best = quizzes ? Math.max(...attempts.map((a) => a.percentage)) : 0;

  // Topic -> average %, weakest first
  const topics = Object.values(
    attempts.reduce((acc, a) => {
      const t = (acc[a.topic] ??= { topic: a.topic, score: 0, total: 0, quizzes: 0 });
      t.score += a.score;
      t.total += a.total;
      t.quizzes += 1;
      return acc;
    }, {})
  )
    .map((t) => ({ ...t, pct: Math.round((t.score / t.total) * 100) }))
    .sort((a, b) => a.pct - b.pct);

  const trend = attempts.slice(0, 10).reverse(); // last 10, oldest -> newest
  const firstName = student.name.split(" ")[0];

  return (
    <section className="page">
      <div className="page-inner">
        <div className="page-head">
          <div>
            <h2>
              {greeting()}, {firstName} 👋
            </h2>
            <p>
              {student.level || "Beginner"} · {student.course || "Java"} — here's how your learning is going.
            </p>
          </div>
          <div className="head-actions">
            <button className="btn-ghost" onClick={() => onNavigate("chat")}>
              💬 Ask mentor
            </button>
            <button className="btn-primary" onClick={() => onNavigate("quiz")}>
              📝 Take a quiz
            </button>
          </div>
        </div>

        {error && <div className="alert">⚠️ {error}</div>}

        <div className="stats">
          <Stat icon="📝" label="Quizzes taken" value={loading ? "…" : quizzes} />
          <Stat icon="🎯" label="Accuracy" value={loading ? "…" : `${accuracy}%`} />
          <Stat icon="🏆" label="Best score" value={loading ? "…" : `${best}%`} />
          <Stat icon="✅" label="Questions answered" value={loading ? "…" : answered} />
        </div>

        {!loading && quizzes === 0 ? (
          <div className="card empty-state">
            <div className="empty-icon">🚀</div>
            <h3>Your progress starts here</h3>
            <p className="muted">
              Take your first AI quiz. Your scores, strong topics and weak topics will appear on this dashboard.
            </p>
            <button className="btn-primary" onClick={() => onNavigate("quiz")}>
              Take my first quiz →
            </button>
          </div>
        ) : (
          <>
            <div className="grid-2">
              {/* Score trend */}
              <div className="card">
                <div className="card-head">
                  <h3>Score trend</h3>
                  <span className="muted small">last {trend.length} quizzes</span>
                </div>
                <div className="trend">
                  <div className="pass-line" title="60% = pass">
                    <span>60%</span>
                  </div>
                  {trend.map((a) => (
                    <div className="trend-col" key={a.id} title={`${a.topic}: ${a.score}/${a.total} (${a.percentage}%)`}>
                      <span className="trend-value">{a.percentage}%</span>
                      <div className="trend-bar" style={{ height: `${Math.max(a.percentage, 3)}%` }} />
                    </div>
                  ))}
                </div>
              </div>

              {/* Topic performance */}
              <div className="card">
                <div className="card-head">
                  <h3>Topic performance</h3>
                  <span className="muted small">weakest first</span>
                </div>
                <ul className="bars">
                  {topics.slice(0, 6).map((t) => (
                    <li key={t.topic}>
                      <div className="bar-head">
                        <span>
                          {t.topic}{" "}
                          {t.pct < 60 && <span className="tag weak">Needs work</span>}
                          {t.pct >= 80 && <span className="tag strong">Strong</span>}
                        </span>
                        <strong>{t.pct}%</strong>
                      </div>
                      <div className="bar-track">
                        <div className="bar-fill" style={{ width: `${Math.max(t.pct, 2)}%` }} />
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            <div className="grid-2">
              {/* AI study plan */}
              <div className="card tips-card">
                <div className="card-head">
                  <h3>🤖 AI study plan</h3>
                  {tips && (
                    <button className="link-btn" onClick={loadTips} disabled={tipsLoading}>
                      ↻ Refresh
                    </button>
                  )}
                </div>
                {tipsError && <div className="alert">⚠️ {tipsError}</div>}
                {tipsLoading ? (
                  <div className="inline-loading">
                    <div className="spinner small" /> JavaMentor is analysing your results…
                  </div>
                ) : tips ? (
                  <Markdown text={tips} />
                ) : (
                  <>
                    <p className="muted">
                      Let JavaMentor read your quiz results and tell you what to study next.
                    </p>
                    <button className="btn-primary" onClick={loadTips}>
                      Get my study plan
                    </button>
                  </>
                )}
              </div>

              {/* Recent quizzes */}
              <div className="card">
                <div className="card-head">
                  <h3>Recent quizzes</h3>
                </div>
                <ul className="recent">
                  {attempts.slice(0, 6).map((a) => (
                    <li key={a.id}>
                      <span className={`score-badge ${a.percentage >= 60 ? "pass" : "fail"}`}>{a.percentage}%</span>
                      <div className="recent-text">
                        <strong>{a.topic}</strong>
                        <small>
                          {a.level} · {a.score}/{a.total} correct · {formatDate(a.attemptedAt)}
                        </small>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </>
        )}
      </div>
    </section>
  );
}

function Stat({ icon, label, value }) {
  return (
    <div className="stat-card">
      <span className="stat-icon">{icon}</span>
      <div className="stat-text">
        <span className="stat-label">{label}</span>
        <span className="stat-value">{value}</span>
      </div>
    </div>
  );
}

function greeting() {
  const h = new Date().getHours();
  return h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" });
}
