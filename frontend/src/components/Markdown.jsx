import { marked } from "marked";
import DOMPurify from "dompurify";

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
