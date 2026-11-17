import { marked } from "marked";
import DOMPurify from "dompurify";

// Renders AI text (Markdown) safely: marked -> HTML, DOMPurify removes any <script> etc.
// inline = for one-line text like quiz options (no <p> wrapper)
export default function Markdown({ text, inline = false, className = "" }) {
  const html = inline ? marked.parseInline(text ?? "") : marked.parse(text ?? "");
  const Tag = inline ? "span" : "div";
  return (
    <Tag
      className={`markdown ${className}`}
      dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(html) }}
    />
  );
}
