import { useEffect, useRef, useState } from "react";
import Message from "../components/Message.jsx";
import Logo from "../components/Logo.jsx";
import { sendMessage } from "../api.js";

const SUGGESTIONS = [
  "What is a variable in Java?",
  "Explain OOP with a real-life example",
  "Difference between == and equals()",
  "Why do I get a NullPointerException?",
];

// AI Mentor Chat. Chat list state comes from useChats() (shared with the sidebar).
export default function ChatPage({ student, chat }) {
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);
  const textareaRef = useRef(null);

  const activeChat = chat.chats.find((c) => c.id === chat.activeId);
  const messages = activeChat?.messages ?? [];

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages.length, loading]);

  // Auto-grow the textarea (max 200px)
  useEffect(() => {
    const ta = textareaRef.current;
    if (!ta) return;
    ta.style.height = "auto";
    if (input) ta.style.height = Math.min(ta.scrollHeight, 200) + "px";
  }, [input]);

  const send = async (text) => {
    const question = (text ?? input).trim();
    if (!question || loading) return;

    const chatId = activeChat ? activeChat.id : chat.createChat(question);
    chat.addMessage(chatId, { role: "user", content: question });
    setInput("");
    setLoading(true);

    try {
      // level from the profile -> the mentor adapts its explanation
      const answer = await sendMessage(chatId, question, student.level);
      chat.addMessage(chatId, { role: "ai", content: answer });
    } catch (err) {
      chat.addMessage(chatId, {
        role: "ai",
        content: `⚠️ **Couldn't get an answer.** ${err.message}. Is the backend running on port 8080?`,
        error: true,
      });
    } finally {
      setLoading(false);
      textareaRef.current?.focus();
    }
  };

  const onKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  return (
    <>
      <section className="messages">
        {messages.length === 0 ? (
          <div className="welcome">
            <Logo className="welcome-logo" />
            <h2>Hi {student.name.split(" ")[0]}, what shall we learn today?</h2>
            <p>
              I'm your Java mentor. Ask me any doubt, paste code with an error, or pick a topic below.
              I'll explain it at <strong>{student.level || "Beginner"}</strong> level.
            </p>
            <div className="suggestions">
              {SUGGESTIONS.map((s) => (
                <button key={s} onClick={() => send(s)}>
                  {s}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="messages-inner">
            {messages.map((m, i) => (
              <Message key={i} {...m} />
            ))}
            {loading && (
              <div className="msg-row ai">
                <Logo className="avatar" />
                <div className="bubble typing">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>
        )}
      </section>

      <footer className="composer">
        <div className="composer-box">
          <textarea
            ref={textareaRef}
            rows={1}
            value={input}
            placeholder="Ask your Java mentor anything…"
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={onKeyDown}
            disabled={loading}
            autoFocus
          />
          <button className="send-btn" onClick={() => send()} disabled={loading || !input.trim()}>
            ➤
          </button>
        </div>
        <small className="hint">Enter to send · Shift + Enter for a new line</small>
      </footer>
    </>
  );
}
