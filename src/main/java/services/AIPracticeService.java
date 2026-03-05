package services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIPracticeService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = System.getenv("API_KEY"); // paste your key
    private static final String MODEL = "llama-3.3-70b-versatile"; // fast + smart

    public String askAI(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            String escapedPrompt = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            String jsonInput = "{"
                    + "\"model\": \"" + MODEL + "\","
                    + "\"messages\": ["
                    +   "{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}"
                    + "],"
                    + "\"max_tokens\": 1024"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("UTF-8"));
                os.flush();
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8")
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            return extractResponse(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error communicating with AI.";
        }
    }

    private String extractResponse(String json) {
        try {
            // Groq returns: "content":"<answer>"
            int start = json.indexOf("\"content\":\"");
            if (start == -1) return json;

            int valueStart = start + 11;
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
            return result.toString();

        } catch (Exception e) {
            return json;
        }
    }
}