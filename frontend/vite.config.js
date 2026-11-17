import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// React runs on port 5173, Spring Boot on 8080.
// The proxy forwards every "/chat..." request to Spring Boot,
// so the browser thinks it is the same server -> no CORS setup needed.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/chat": "http://localhost:8080",
      "/api": "http://localhost:8080",
    },
  },
});
