import { useState } from "react";
import { createStudent, updateStudent, deleteStudent } from "../api.js";
import { initials } from "../components/Sidebar.jsx";
import Logo from "../components/Logo.jsx";

const COURSES = ["Core Java", "Advanced Java", "Spring Boot", "Java Full Stack", "DSA in Java"];
const LEVELS = [
  { id: "Beginner", hint: "I'm new to programming" },
  { id: "Intermediate", hint: "I know the basics" },
  { id: "Advanced", hint: "I want deeper concepts" },
];

const EMPTY = { name: "", email: "", phone: "", course: "Core Java", level: "Beginner" };

// mode="create": onboarding screen (Create)
// mode="edit":   My Profile page (Read, Update, Delete)
export default function ProfilePage({ mode, student, onCreated, onSaved, onDeleted }) {
  const isCreate = mode === "create";
  const [form, setForm] = useState(isCreate ? EMPTY : { ...EMPTY, ...clean(student) });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const set = (field) => (e) => {
    setForm({ ...form, [field]: e.target.value });
    setSaved(false);
  };

  const validate = () => {
    if (!form.name.trim()) return "Please enter your name.";
    if (!/^\S+@\S+\.\S+$/.test(form.email)) return "Please enter a valid email.";
    if (form.phone && !/^[0-9+\- ]{7,15}$/.test(form.phone)) return "Phone number looks invalid.";
    return null;
  };

  const submit = async (e) => {
    e.preventDefault();
    const problem = validate();
    if (problem) return setError(problem);

    setSaving(true);
    setError(null);
    try {
      const data = { ...form, name: form.name.trim(), email: form.email.trim() };
      if (isCreate) {
        onCreated(await createStudent(data)); // POST /api/students
      } else {
        onSaved(await updateStudent(student.id, data)); // PUT /api/students/{id}
        setSaved(true);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    setSaving(true);
    try {
      await deleteStudent(student.id); // DELETE /api/students/{id} (also deletes quiz history)
      onDeleted();
    } catch (err) {
      setError(err.message);
      setSaving(false);
    }
  };

  const formFields = (
    <form className="profile-form" onSubmit={submit}>
      <div className="field-row">
        <label>
          Full name *
          <input value={form.name} onChange={set("name")} placeholder="e.g. Rahul Sharma" autoFocus={isCreate} />
        </label>
        <label>
          Email *
          <input type="email" value={form.email} onChange={set("email")} placeholder="you@example.com" />
        </label>
      </div>
      <div className="field-row">
        <label>
          Phone
          <input value={form.phone} onChange={set("phone")} placeholder="Optional" />
        </label>
        <label>
          What are you learning?
          <select value={form.course} onChange={set("course")}>
            {COURSES.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
        </label>
      </div>

      <div className="field-label">Your level</div>
      <div className="level-picker">
        {LEVELS.map((l) => (
          <button
            type="button"
            key={l.id}
            className={`level-option ${form.level === l.id ? "selected" : ""}`}
            onClick={() => {
              setForm({ ...form, level: l.id });
              setSaved(false);
            }}
          >
            <strong>{l.id}</strong>
            <small>{l.hint}</small>
          </button>
        ))}
      </div>
      <p className="muted small">The AI mentor and quizzes adapt to your level.</p>

      {error && <div className="alert">⚠️ {error}</div>}
      {saved && <div className="alert success">✓ Profile updated</div>}

      <button className="btn-primary wide" disabled={saving}>
        {saving ? "Saving…" : isCreate ? "Create my profile →" : "Save changes"}
      </button>
    </form>
  );

  // ---------- Create (onboarding) ----------
  if (isCreate) {
    return (
      <div className="onboarding">
        <div className="onboarding-card">
          <Logo className="welcome-logo" />
          <h1>Welcome to JavaMentor AI</h1>
          <p className="muted">
            Your personal AI mentor for learning Java from scratch. Create your profile to start chatting,
            take AI quizzes and track your progress.
          </p>
          {formFields}
        </div>
      </div>
    );
  }

  // ---------- Read / Update / Delete ----------
  return (
    <section className="page">
      <div className="page-inner narrow">
        <div className="profile-hero card">
          <span className="initials big">{initials(student.name)}</span>
          <div>
            <h2>{student.name}</h2>
            <p className="muted">{student.email}</p>
            <div className="pills">
              <span className="pill accent">{student.level || "Beginner"}</span>
              <span className="pill">{student.course || "Java"}</span>
              {student.phone && <span className="pill">📞 {student.phone}</span>}
            </div>
          </div>
        </div>

        <div className="card">
          <h3>Edit profile</h3>
          {formFields}
        </div>

        <div className="card danger-zone">
          <div>
            <h3>Delete account</h3>
            <p className="muted">Removes your profile and your quiz history. This can't be undone.</p>
          </div>
          {!confirmDelete ? (
            <button className="btn-danger" onClick={() => setConfirmDelete(true)}>
              Delete account
            </button>
          ) : (
            <div className="confirm-row">
              <span>Are you sure?</span>
              <button className="btn-ghost" onClick={() => setConfirmDelete(false)}>
                Cancel
              </button>
              <button className="btn-danger" onClick={remove} disabled={saving}>
                Yes, delete
              </button>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

// Drop null fields so the defaults in EMPTY are used instead (keeps inputs controlled)
function clean(s) {
  return Object.fromEntries(Object.entries(s).filter(([, v]) => v !== null && v !== undefined));
}
