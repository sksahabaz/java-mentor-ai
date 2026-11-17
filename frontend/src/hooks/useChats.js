import { useEffect, useState } from "react";

// Keeps the list of chats for ONE student in localStorage.
// The AI's real memory is in MySQL (SPRING_AI_CHAT_MEMORY), linked by the same chat id.
export default function useChats(studentId) {
  const key = `javamentor-chats-${studentId}`;

  const activeKey = `javamentor-active-chat-${studentId}`;

  const [chats, setChats] = useState(() => load(key));
  // The open chat is remembered too, so a page refresh reopens it
  const [activeId, setActiveId] = useState(() => localStorage.getItem(activeKey));

  // Different student -> load their chats
  useEffect(() => {
    setChats(load(key));
    setActiveId(localStorage.getItem(activeKey));
  }, [key, activeKey]);

  useEffect(() => {
    localStorage.setItem(key, JSON.stringify(chats));
  }, [key, chats]);

  useEffect(() => {
    if (activeId) localStorage.setItem(activeKey, activeId);
    else localStorage.removeItem(activeKey);
  }, [activeKey, activeId]);

  const createChat = (firstQuestion) => {
    const chat = {
      id: crypto.randomUUID(), // 36 chars -> fits conversation_id VARCHAR(36)
      title: firstQuestion.length > 40 ? firstQuestion.slice(0, 40) + "…" : firstQuestion,
      messages: [],
    };
    setChats((prev) => [chat, ...prev]);
    setActiveId(chat.id);
    return chat.id;
  };

  const addMessage = (chatId, msg) =>
    setChats((prev) => prev.map((c) => (c.id === chatId ? { ...c, messages: [...c.messages, msg] } : c)));

  const deleteChat = (id) => {
    setChats((prev) => prev.filter((c) => c.id !== id));
    if (id === activeId) setActiveId(null);
  };

  return { chats, activeId, setActiveId, createChat, addMessage, deleteChat };
}

function load(key) {
  try {
    return JSON.parse(localStorage.getItem(key)) ?? [];
  } catch {
    return [];
  }
}
