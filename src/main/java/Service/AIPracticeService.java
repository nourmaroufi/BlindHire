package Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIPracticeService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final String MODEL = "llama-3.3-70b-versatile"; // fast + smart

    public String askAI(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            String escaped = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            String body = "{"
                    + "\"model\": \"" + MODEL + "\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escaped + "\"}],"
                    + "\"max_tokens\": 1024"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            int status = conn.getResponseCode();

            // Read either success stream or error stream
            InputStream stream = (status == 200)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            String json = sb.toString();

            if (status != 200) {
                System.err.println("[AIPracticeService] HTTP " + status + ": " + json);
                return "⚠️ AI error (HTTP " + status + "). Check your API key.";
            }

            return extractContent(json);

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Could not reach Groq. Check your internet connection.";
        }
    }

    /**
     * Parses choices[0].message.content — targets the assistant reply specifically,
     * not any other "content" field that might appear earlier in the JSON.
     */
    private String extractContent(String json) {
        try {
            // Groq response: {"choices":[{"message":{"role":"assistant","content":"<HERE>"}}]}
            int choicesIdx = json.indexOf("\"choices\"");
            if (choicesIdx == -1) return json;

            int messageIdx = json.indexOf("\"message\"", choicesIdx);
            if (messageIdx == -1) return json;

            int contentIdx = json.indexOf("\"content\":\"", messageIdx);
            if (contentIdx == -1) return json;

            int valueStart = contentIdx + 11;
            StringBuilder result = new StringBuilder();

            int i = valueStart;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    switch (next) {
                        case 'n'  -> { result.append('\n'); i += 2; }
                        case 't'  -> { result.append('\t'); i += 2; }
                        case '"'  -> { result.append('"');  i += 2; }
                        case '\\' -> { result.append('\\'); i += 2; }
                        default   -> { result.append(next); i += 2; }
                    }
                } else if (c == '"') {
                    break;
                } else {
                    result.append(c);
                    i++;
                }
            }
            return result.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return json;
        }
    }
}