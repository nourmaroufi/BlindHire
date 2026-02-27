package services;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class AiQuizGeneratorService {

    private static final String OLLAMA_URL = "http://127.0.0.1:11434/api/generate";
    private static final String MODEL = "qwen2.5:7b"; // change if you pulled another

    private final HttpClient http = HttpClient.newHttpClient();

    /**
     * @param prompt user/admin prompt (skills, difficulty, number of questions, etc.)
     * @return raw JSON string (ONLY JSON)
     */
    public String generateQuizJson(String prompt) throws IOException, InterruptedException {
        String system =
                "Return ONLY valid JSON (no markdown, no comments, no extra text). " +
                        "The first character MUST be '{' and the last MUST be '}'. " +
                        "No trailing commas. " +
                        "Schema: {\"skill\":string,\"questions\":[{\"statement\":string,\"points\":number," +
                        "\"choices\":[{\"text\":string,\"is_correct\":boolean}]}]}. " +
                        "Rules: each question has >=2 choices; exactly ONE choice has is_correct=true.";

        String fullPrompt =
                system + "\n\n" +
                        "ADMIN REQUEST:\n" + prompt + "\n\n" +
                        "Return ONLY the JSON now.";

        String body = "{"
                + "\"model\":\"" + escapeJson(MODEL) + "\","
                + "\"prompt\":\"" + escapeJson(fullPrompt) + "\","
                + "\"stream\":false,"
                + "\"options\":{"
                +   "\"temperature\":0.1,"
                +   "\"num_predict\":2048"
                + "}"
                + "}";


        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() != 200) {
            throw new IOException("Ollama error " + res.statusCode() + ": " + res.body());
        }

        // Ollama returns JSON with a "response" field that contains the text we want.
        String responseText = extractJsonField(res.body(), "response");
        if (responseText == null) {
            throw new IOException("Ollama response missing 'response' field: " + res.body());
        }

        // Sometimes model may add whitespace/newlines. Trim, then take JSON object bounds.
        String json = extractFirstJsonValue(responseText);

        if (json == null) {
            // retry once with a stricter prompt
            String retryPrompt =
                    fullPrompt +
                            "\n\nYou returned invalid/incomplete JSON. Try again. " +
                            "Return ONLY ONE complete JSON object. First char '{' last char '}'.";
            String retryBody = "{"
                    + "\"model\":\"" + escapeJson(MODEL) + "\","
                    + "\"prompt\":\"" + escapeJson(retryPrompt) + "\","
                    + "\"stream\":false,"
                    + "\"options\":{"
                    +   "\"temperature\":0.0,"
                    +   "\"num_predict\":2048"
                    + "}"
                    + "}";

            HttpRequest req2 = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(retryBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res2 = http.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res2.statusCode() != 200) throw new IOException("Ollama retry error " + res2.statusCode() + ": " + res2.body());

            String response2 = extractJsonField(res2.body(), "response");
            json = extractFirstJsonValue(response2);
        }

        if (json == null) {
            throw new IOException("Model did not return COMPLETE JSON. Last chars: " +
                    (responseText == null ? "null" : responseText.substring(Math.max(0, responseText.length()-200))));
        }

        return json;
    }

    // ---- helpers (no external JSON libs) ----

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    /**
     * Very small JSON field extractor for top-level string fields like "response":"..."
     */
    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":";
        int i = json.indexOf(key);
        if (i < 0) return null;

        int startQuote = json.indexOf('"', i + key.length());
        if (startQuote < 0) return null;

        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (int p = startQuote + 1; p < json.length(); p++) {
            char c = json.charAt(p);
            if (esc) {
                // basic unescape for common sequences
                if (c == 'n') out.append('\n');
                else if (c == 'r') out.append('\r');
                else if (c == 't') out.append('\t');
                else out.append(c);
                esc = false;
            } else {
                if (c == '\\') esc = true;
                else if (c == '"') return out.toString();
                else out.append(c);
            }
        }
        return null;
    }

    /**
     * Extract first {...} JSON object from a string.
     */
    private String extractFirstJsonValue(String s) {
        if (s == null) return null;

        // remove leading whitespace/newlines
        s = s.trim();
        if (s.isEmpty()) return null;

        // find first { or [
        int obj = s.indexOf('{');
        int arr = s.indexOf('[');

        int start;
        char open, close;

        if (obj == -1 && arr == -1) return null;
        if (obj == -1 || (arr != -1 && arr < obj)) {
            start = arr; open = '['; close = ']';
        } else {
            start = obj; open = '{'; close = '}';
        }

        int depth = 0;
        boolean inString = false;
        boolean esc = false;

        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inString) {
                if (esc) esc = false;
                else if (c == '\\') esc = true;
                else if (c == '"') inString = false;
                continue;
            } else {
                if (c == '"') { inString = true; continue; }

                if (c == open) depth++;
                else if (c == close) depth--;

                if (depth == 0) {
                    return s.substring(start, i + 1).trim();
                }
            }
        }
        return null;
    }
}