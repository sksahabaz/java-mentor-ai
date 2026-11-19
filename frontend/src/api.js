const JSON_HEADERS = { "Content-Type": "application/json" };
const AUTH_KEY = "javamentor-auth";
export const BACKEND_ORIGIN = "http://localhost:8080";
export const GOOGLE_LOGIN_URL = `${BACKEND_ORIGIN}/oauth2/authorization/google`;
export const GOOGLE_REGISTER_URL = `${GOOGLE_LOGIN_URL}?mode=register`;

let refreshPromise = null;

function loadAuth() {
  try {
    return JSON.parse(localStorage.getItem(AUTH_KEY));
  } catch {
    return null;
  }
}

export function saveAuth(accessToken, refreshToken) {
  localStorage.setItem(AUTH_KEY, JSON.stringify({ accessToken, refreshToken }));
}

export function clearAuth() {
  localStorage.removeItem(AUTH_KEY);
}

export function hasSession() {
  return !!loadAuth()?.refreshToken;
}

async function errorMessage(res) {
  try {
    const body = await res.json();
    const fieldErrors = body.errors ? Object.values(body.errors) : [];
    if (fieldErrors.length) return fieldErrors.join(" ");
    if (body.message) return body.message;
  } catch {
  }
  return `Request failed (${res.status}).`;
}

async function parse(res, as) {
  if (!res.ok) throw new Error(await errorMessage(res));
  if (res.status === 204) return null;
  return as === "text" ? res.text() : res.json();
}

async function refreshTokens() {
  const auth = loadAuth();
  if (!auth?.refreshToken) throw new Error("Not logged in");
  const res = await fetch("/api/auth/refresh", {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify({ refreshToken: auth.refreshToken }),
  });
  if (!res.ok) throw new Error("Session expired");
  const data = await res.json();
  saveAuth(data.accessToken, data.refreshToken);
}

function sessionExpired() {
  clearAuth();
  window.dispatchEvent(new Event("javamentor:logout"));
  throw new Error("Your session has expired. Please log in again.");
}

async function request(url, { method = "GET", body, as = "json" } = {}) {
  const send = () => {
    const headers = body !== undefined ? { ...JSON_HEADERS } : {};
    const token = loadAuth()?.accessToken;
    if (token) headers.Authorization = `Bearer ${token}`;
    return fetch(url, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined });
  };

  let res = await send();
  if (res.status === 401 && hasSession()) {
    try {
      refreshPromise ??= refreshTokens().finally(() => (refreshPromise = null));
      await refreshPromise;
    } catch {
      sessionExpired();
    }
    res = await send();
    if (res.status === 401) sessionExpired();
  }
  return parse(res, as);
}

export async function login(email, password) {
  const res = await fetch("/api/auth/login", {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify({ email, password }),
  });
  const data = await parse(res);
  saveAuth(data.accessToken, data.refreshToken);
}

export async function register(form) {
  const res = await fetch("/api/auth/register", {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify(form),
  });
  await parse(res);
  await login(form.email, form.password);
}

export async function logout() {
  const refreshToken = loadAuth()?.refreshToken;
  clearAuth();
  if (refreshToken) {
    await fetch("/api/auth/logout", {
      method: "POST",
      headers: JSON_HEADERS,
      body: JSON.stringify({ refreshToken }),
    }).catch(() => {});
  }
}

export function consumeOAuthCallback() {
  if (window.location.pathname !== "/oauth2/callback") return null;
  const params = new URLSearchParams(window.location.hash.slice(1));
  window.history.replaceState(null, "", "/#dashboard");
  const accessToken = params.get("token");
  const refreshToken = params.get("refreshToken");
  if (accessToken && refreshToken) {
    saveAuth(accessToken, refreshToken);
    return null;
  }
  const error = params.get("error");
  if (error === "not_registered") {
    const email = params.get("email") ?? "";
    return {
      mode: "register",
      email,
      message: `No account found for ${email}. Click "Sign up with Google" below, or register with a password.`,
    };
  }
  if (error === "already_registered") {
    const email = params.get("email") ?? "";
    return {
      mode: "login",
      email,
      message: `${email} is already registered. Please log in.`,
    };
  }
  if (error === "email_not_verified") return { message: "Your Google email is not verified." };
  if (error === "account_disabled") return { message: "Your account is disabled." };
  return { message: "Google sign-in failed. Please try again." };
}

export const sendMessage = (conversationId, question, level) =>
  request("/chat/memory", { method: "POST", body: { conversationId, question, level }, as: "text" });

export const getMe = () => request("/api/students/me");

export const updateMe = (profile) => request("/api/students/me", { method: "PUT", body: profile });

export const deleteMe = () => request("/api/students/me", { method: "DELETE" });

export const generateQuiz = (topic, level, count) =>
  request("/api/quiz/generate", { method: "POST", body: { topic, level, count } });

export const saveAttempt = (attempt) => request("/api/quiz/attempts", { method: "POST", body: attempt });

export const getAttempts = () => request("/api/quiz/attempts");

export const getStudyTips = () => request("/api/quiz/tips", { as: "text" });
