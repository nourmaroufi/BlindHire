package services;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AiCandidateAnalysisService {
    private static final String OLLAMA_URL = "http://127.0.0.1:11434/api/generate";
    private static final String MODEL = "qwen2.5:7b";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(600))
            .build();

    public String analyze(String prompt) throws Exception {
        String body = "{"
                + "\"model\":\"" + escapeJson(MODEL) + "\","
                + "\"prompt\":\"" + escapeJson(prompt) + "\","
                + "\"stream\":false,"
                + "\"options\":{"
                +   "\"temperature\":0.2,"
                +   "\"num_predict\":1200"
                + "}"
                + "}";

        HttpRequest req = HttpRequest.newBuilder(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json; charset=utf-8")
                .timeout(Duration.ofSeconds(180))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Ollama HTTP " + resp.statusCode() + ":\n" + resp.body());
        }

        // Ollama returns JSON like: {"response":"...","done":true,...}
        String responseText = extractOllamaResponse(resp.body());

        // IMPORTANT: force JSON extraction
        String json = extractFirstJsonObject(responseText);

        if (json == null) {
            throw new RuntimeException("AI did not return JSON object.\nRAW RESPONSE:\n" + responseText);
        }
        return json;
    }
    private String extractOllamaResponse(String ollamaJson) {
        String key = "\"response\":";
        int i = ollamaJson.indexOf(key);
        if (i < 0) return ollamaJson;

        int start = ollamaJson.indexOf('"', i + key.length());
        if (start < 0) return ollamaJson;
        start++;

        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int p = start; p < ollamaJson.length(); p++) {
            char c = ollamaJson.charAt(p);
            if (esc) { sb.append(c); esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
    }

    private String extractFirstJsonObject(String s) {
        if (s == null) return null;
        int start = s.indexOf('{');
        if (start < 0) return null;

        int depth = 0;
        boolean inString = false, esc = false;

        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inString) {
                if (esc) esc = false;
                else if (c == '\\') esc = true;
                else if (c == '"') inString = false;
            } else {
                if (c == '"') inString = true;
                else if (c == '{') depth++;
                else if (c == '}') depth--;
                if (depth == 0) return s.substring(start, i + 1);
            }
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    // -------- helpers --------

    private String extractJsonField(String json, String field) {
        // minimal extraction for {"response":"..."} (works with Ollama response)
        String key = "\"" + field + "\":";
        int i = json.indexOf(key);
        if (i < 0) return json;
        int start = json.indexOf('"', i + key.length());
        if (start < 0) return json;
        start++;
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int p = start; p < json.length(); p++) {
            char c = json.charAt(p);
            if (esc) { sb.append(c); esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
    }




}