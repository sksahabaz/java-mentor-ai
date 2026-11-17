// All backend calls live here. Pages never call fetch() directly.
// Vite proxies /chat and /api to Spring Boot on port 8080 (see vite.config.js).

const JSON_HEADERS = { "Content-Type": "application/json" };

// Turns an HTTP error status into a message a student understands
async function handle(res) {
  if (res.ok) {
    return res.status === 204 ? null : res.json(); // 204 = deleted, no body
  }
  if (res.status === 404) throw new Error("Profile not found.");
  if (res.status === 400) throw new Error("Some details are invalid. Please check the form.");
  // Duplicate email hits the UNIQUE constraint in MySQL -> backend returns 500 for now
  if (res.status === 500) throw new Error("Something went wrong. This email may already be registered.");
  throw new Error(`Request failed (${res.status}).`);
}

// ---------- AI Chat ----------
// POST /chat/memory -> ChatService.chatWithMemory() (memory stored in MySQL)
export async function sendMessage(conversationId, question, level) {
  const res = await fetch("/chat/memory", {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify({ conversationId, question, level }),
  });
  if (!res.ok) throw new Error(`Server error ${res.status}`);
  return res.text(); // Markdown
}

// ---------- My Profile (StudentController /api/students) ----------
export const getStudent = (id) => fetch(`/api/students/${id}`).then(handle);

export const createStudent = (student) =>
  fetch("/api/students", { method: "POST", headers: JSON_HEADERS, body: JSON.stringify(student) }).then(handle);

export const updateStudent = (id, student) =>
  fetch(`/api/students/${id}`, { method: "PUT", headers: JSON_HEADERS, body: JSON.stringify(student) }).then(handle);

export const deleteStudent = (id) => fetch(`/api/students/${id}`, { method: "DELETE" }).then(handle);

// ---------- AI Quiz + performance (QuizController /api/quiz) ----------
export async function generateQuiz(topic, level, count) {
  const res = await fetch("/api/quiz/generate", {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify({ topic, level, count }),
  });
  if (!res.ok) throw new Error("JavaMentor couldn't create this quiz. Please try again.");
  return res.json(); // { topic, questions: [{ question, options[4], correctIndex, explanation }] }
}

export const saveAttempt = (attempt) =>
  fetch("/api/quiz/attempts", { method: "POST", headers: JSON_HEADERS, body: JSON.stringify(attempt) }).then(handle);

export const getAttempts = (studentId) => fetch(`/api/quiz/attempts/${studentId}`).then(handle);

export async function getStudyTips(studentId) {
  const res = await fetch(`/api/quiz/tips/${studentId}`);
  if (!res.ok) throw new Error("Couldn't get your study plan right now. Please try again.");
  return res.text(); // Markdown
}
