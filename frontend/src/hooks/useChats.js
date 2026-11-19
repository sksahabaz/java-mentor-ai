import { useEffect, useState } from "react";

export default function useChats(studentId) {
  const key = `javamentor-chats-${studentId}`;

  const activeKey = `javamentor-active-chat-${studentId}`;

  const [chats, setChats] = useState(() => load(key));
  const [activeId, setActiveId] = useState(() => localStorage.getItem(activeKey));

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
      id: crypto.randomUUID(),
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
