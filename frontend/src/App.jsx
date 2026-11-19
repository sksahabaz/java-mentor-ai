import { useEffect, useState } from "react";
import Sidebar from "./components/Sidebar.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import ChatPage from "./pages/ChatPage.jsx";
import QuizPage from "./pages/QuizPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import AuthPage from "./pages/AuthPage.jsx";
import useChats from "./hooks/useChats.js";
import { clearAuth, consumeOAuthCallback, getMe, hasSession, logout } from "./api.js";

const TITLES = {
  dashboard: "Dashboard",
  chat: "AI Mentor Chat",
  quiz: "AI Quiz",
  profile: "My Profile",
};

const oauthResult = consumeOAuthCallback();

export default function App() {
  const [student, setStudent] = useState(null);
  const [checking, setChecking] = useState(true);
  const [view, setViewState] = useState(() => {
    const fromUrl = window.location.hash.slice(1);
    return TITLES[fromUrl] ? fromUrl : "dashboard";
  });
  const setView = (v) => {
    window.location.hash = v;
    setViewState(v);
  };
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (!hasSession()) {
      setChecking(false);
      return;
    }
    getMe()
      .then(setStudent)
      .catch(() => clearAuth())
      .finally(() => setChecking(false));
  }, []);

  useEffect(() => {
    const onExpired = () => setStudent(null);
    window.addEventListener("javamentor:logout", onExpired);
    return () => window.removeEventListener("javamentor:logout", onExpired);
  }, []);

  const onLoggedIn = async () => {
    setStudent(await getMe());
    setView("dashboard");
  };

  const onLogout = async () => {
    await logout();
    setStudent(null);
  };

  const onProfileDeleted = () => {
    localStorage.removeItem(`javamentor-chats-${student.id}`);
    localStorage.removeItem(`javamentor-active-chat-${student.id}`);
    clearAuth();
    setStudent(null);
  };

  if (checking) {
    return (
      <div className="center-screen">
        <div className="spinner" />
      </div>
    );
  }

  if (!student) {
    return <AuthPage onLoggedIn={onLoggedIn} oauth={oauthResult} />;
  }

  return <Shell student={student} setStudent={setStudent} view={view} setView={setView}
    sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} onDeleted={onProfileDeleted} onLogout={onLogout} />;
}

function Shell({ student, setStudent, view, setView, sidebarOpen, setSidebarOpen, onDeleted, onLogout }) {
  const chat = useChats(student.id);

  return (
    <div className="app">
      <Sidebar
        student={student}
        view={view}
        onViewChange={setView}
        chat={chat}
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        onLogout={onLogout}
      />
      {sidebarOpen && <div className="backdrop" onClick={() => setSidebarOpen(false)} />}

      <main className="main">
        <header className="topbar">
          <button className="menu-btn" onClick={() => setSidebarOpen(true)}>
            ☰
          </button>
          <span className="topbar-title">
            {view === "chat" && chat.activeId
              ? chat.chats.find((c) => c.id === chat.activeId)?.title
              : TITLES[view]}
          </span>
        </header>

        {view === "dashboard" && <Dashboard student={student} onNavigate={setView} />}
        {view === "chat" && <ChatPage student={student} chat={chat} />}
        {view === "quiz" && <QuizPage student={student} onNavigate={setView} />}
        {view === "profile" && (
          <ProfilePage student={student} onSaved={setStudent} onDeleted={onDeleted} />
        )}
      </main>
    </div>
  );
}
