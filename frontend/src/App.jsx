import { useEffect, useState } from "react";
import Sidebar from "./components/Sidebar.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import ChatPage from "./pages/ChatPage.jsx";
import QuizPage from "./pages/QuizPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import useChats from "./hooks/useChats.js";
import { getStudent } from "./api.js";

const STUDENT_KEY = "javamentor-student-id";

const TITLES = {
  dashboard: "Dashboard",
  chat: "AI Mentor Chat",
  quiz: "AI Quiz",
  profile: "My Profile",
};

export default function App() {
  // Until login exists, "who am I" = the profile id saved in this browser.
  // (Register + Login will replace this later.)
  const [student, setStudent] = useState(null);
  const [checking, setChecking] = useState(true);
  // Current page lives in the URL hash (#dashboard, #chat, #quiz, #profile),
  // so refresh keeps you on the same page and each page has its own link.
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
    const id = localStorage.getItem(STUDENT_KEY);
    if (!id) {
      setChecking(false);
      return;
    }
    getStudent(id)
      .then(setStudent)
      .catch(() => localStorage.removeItem(STUDENT_KEY)) // profile was deleted
      .finally(() => setChecking(false));
  }, []);

  const onProfileCreated = (s) => {
    localStorage.setItem(STUDENT_KEY, s.id);
    setStudent(s);
    setView("dashboard");
  };

  const onProfileDeleted = () => {
    localStorage.removeItem(STUDENT_KEY);
    localStorage.removeItem(`javamentor-chats-${student.id}`);
    localStorage.removeItem(`javamentor-active-chat-${student.id}`);
    setStudent(null);
  };

  if (checking) {
    return (
      <div className="center-screen">
        <div className="spinner" />
      </div>
    );
  }

  // No profile yet -> onboarding (create profile)
  if (!student) {
    return <ProfilePage mode="create" onCreated={onProfileCreated} />;
  }

  return <Shell student={student} setStudent={setStudent} view={view} setView={setView}
    sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} onDeleted={onProfileDeleted} />;
}

// Separate component so useChats() only runs once a student exists
function Shell({ student, setStudent, view, setView, sidebarOpen, setSidebarOpen, onDeleted }) {
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
          <ProfilePage mode="edit" student={student} onSaved={setStudent} onDeleted={onDeleted} />
        )}
      </main>
    </div>
  );
}
