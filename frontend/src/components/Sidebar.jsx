import Logo from "./Logo.jsx";

const NAV = [
  { id: "dashboard", icon: "📊", label: "Dashboard" },
  { id: "chat", icon: "💬", label: "AI Mentor Chat" },
  { id: "quiz", icon: "📝", label: "AI Quiz" },
  { id: "profile", icon: "👤", label: "My Profile" },
];

// Left panel: navigation, the chat list (only on the chat page), and the student card.
export default function Sidebar({ student, view, onViewChange, chat, open, onClose }) {
  const go = (id) => {
    onViewChange(id);
    onClose();
  };

  return (
    <aside className={`sidebar ${open ? "open" : ""}`}>
      <div className="brand">
        <Logo className="logo" />
        <div>
          <h1>JavaMentor AI</h1>
          <small>Your Java learning assistant</small>
        </div>
      </div>

      <nav className="nav">
        {NAV.map((item) => (
          <button
            key={item.id}
            className={`nav-item ${view === item.id ? "active" : ""}`}
            onClick={() => go(item.id)}
          >
            <span className="nav-icon">{item.icon}</span>
            {item.label}
          </button>
        ))}
      </nav>

      {view === "chat" && (
        <div className="chat-section">
          <div className="section-label">Conversations</div>
          <button
            className="new-chat"
            onClick={() => {
              chat.setActiveId(null);
              onClose();
            }}
          >
            + New chat
          </button>
          <div className="chat-list">
            {chat.chats.length === 0 && <p className="empty-list">No chats yet</p>}
            {chat.chats.map((c) => (
              <div
                key={c.id}
                className={`chat-item ${c.id === chat.activeId ? "active" : ""}`}
                onClick={() => {
                  chat.setActiveId(c.id);
                  onClose();
                }}
              >
                <span className="chat-title">{c.title}</span>
                <button
                  className="delete-btn"
                  title="Delete chat"
                  onClick={(e) => {
                    e.stopPropagation(); // don't also open the chat
                    chat.deleteChat(c.id);
                  }}
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="spacer" />

      <button className={`me-card ${view === "profile" ? "active" : ""}`} onClick={() => go("profile")}>
        <span className="initials">{initials(student.name)}</span>
        <span className="me-text">
          <strong>{student.name}</strong>
          <small>{student.level || "Beginner"} · {student.course || "Java"}</small>
        </span>
      </button>
    </aside>
  );
}

export function initials(name = "") {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join("");
}
