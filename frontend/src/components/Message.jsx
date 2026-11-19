import { useState } from "react";
import Markdown from "./Markdown.jsx";
import Logo from "./Logo.jsx";

export default function Message({ role, content, error }) {
  const [copied, setCopied] = useState(false);
  const isUser = role === "user";

  const copy = async () => {
    await navigator.clipboard.writeText(content);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <div className={`msg-row ${isUser ? "user" : "ai"}`}>
      {!isUser && <Logo className="avatar" />}
      <div className={`bubble ${error ? "error" : ""}`}>
        {isUser ? (
          <p className="user-text">{content}</p>
        ) : (
          <>
            <Markdown text={content} />
            {!error && (
              <button className="copy-btn" onClick={copy}>
                {copied ? "✓ Copied" : "Copy"}
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
}
