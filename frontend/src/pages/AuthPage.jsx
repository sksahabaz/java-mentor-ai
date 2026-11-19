import { useState } from "react";
import { login, register, GOOGLE_LOGIN_URL, GOOGLE_REGISTER_URL } from "../api.js";
import Logo from "../components/Logo.jsx";

const COURSES = ["Core Java", "Advanced Java", "Spring Boot", "Java Full Stack", "DSA in Java"];
const LEVELS = ["Beginner", "Intermediate", "Advanced"];
const EMPTY = { name: "", email: "", password: "", phone: "", course: "Core Java", level: "Beginner" };

export default function AuthPage({ onLoggedIn, oauth }) {
  const [mode, setMode] = useState(oauth?.mode ?? "login");
  const [form, setForm] = useState({ ...EMPTY, email: oauth?.email ?? "" });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(oauth?.message ?? null);

  const isRegister = mode === "register";
  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const switchMode = (m) => {
    setMode(m);
    setError(null);
  };

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      if (isRegister) {
        await register({ ...form, phone: form.phone || null });
      } else {
        await login(form.email, form.password);
      }
      await onLoggedIn();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <div className="onboarding">
      <div className="onboarding-card auth-card">
        <Logo className="welcome-logo" />
        <h1>{isRegister ? "Create your account" : "Welcome back"}</h1>
        <p className="muted">Your personal AI mentor for learning Java.</p>

        <div className="auth-tabs">
          <button type="button" className={!isRegister ? "active" : ""} onClick={() => switchMode("login")}>
            Log in
          </button>
          <button type="button" className={isRegister ? "active" : ""} onClick={() => switchMode("register")}>
            Register
          </button>
        </div>

        <a className="btn-google" href={isRegister ? GOOGLE_REGISTER_URL : GOOGLE_LOGIN_URL}>
          <span className="google-g">G</span> {isRegister ? "Sign up with Google" : "Log in with Google"}
        </a>
        <div className="auth-divider">
          <span>or</span>
        </div>

        <form className="profile-form" onSubmit={submit}>
          {isRegister && (
            <label>
              Full name *
              <input value={form.name} onChange={set("name")} placeholder="e.g. Rahul Sharma" required />
            </label>
          )}
          <label>
            Email *
            <input type="email" value={form.email} onChange={set("email")} placeholder="you@example.com" required />
          </label>
          <label>
            Password *
            <input
              type="password"
              value={form.password}
              onChange={set("password")}
              placeholder={isRegister ? "At least 8 characters" : "Your password"}
              minLength={isRegister ? 8 : undefined}
              autoComplete={isRegister ? "new-password" : "current-password"}
              required
            />
          </label>
          {isRegister && (
            <div className="field-row">
              <label>
                What are you learning?
                <select value={form.course} onChange={set("course")}>
                  {COURSES.map((c) => (
                    <option key={c}>{c}</option>
                  ))}
                </select>
              </label>
              <label>
                Your level
                <select value={form.level} onChange={set("level")}>
                  {LEVELS.map((l) => (
                    <option key={l}>{l}</option>
                  ))}
                </select>
              </label>
            </div>
          )}

          {error && <div className="alert">⚠️ {error}</div>}

          {!isRegister && (
            <p className="muted small auth-switch">
              No account yet, or deleted yours?{" "}
              <button type="button" className="link-btn" onClick={() => switchMode("register")}>
                Register first
              </button>
            </p>
          )}

          <button className="btn-primary wide" disabled={busy}>
            {busy ? "Please wait…" : isRegister ? "Create account →" : "Log in →"}
          </button>
        </form>
      </div>
    </div>
  );
}
