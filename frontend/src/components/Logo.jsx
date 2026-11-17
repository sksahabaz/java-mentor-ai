import { useState } from "react";

// JavaMentor AI logo: public/logo.png (served by Vite at /logo.png).
// If the file is missing, falls back to the ☕ emoji so the UI never breaks.
export default function Logo({ className = "logo" }) {
  const [failed, setFailed] = useState(false);
  return (
    <span className={`${className} ${failed ? "" : "has-img"}`}>
      {failed ? "☕" : <img src="/logo.png" alt="JavaMentor AI" onError={() => setFailed(true)} />}
    </span>
  );
}
