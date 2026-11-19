import { useState } from "react";

export default function Logo({ className = "logo" }) {
  const [failed, setFailed] = useState(false);
  return (
    <span className={`${className} ${failed ? "" : "has-img"}`}>
      {failed ? "☕" : <img src="/logo.png" alt="JavaMentor AI" onError={() => setFailed(true)} />}
    </span>
  );
}
