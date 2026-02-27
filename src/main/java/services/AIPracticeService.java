package services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIPracticeService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    String API_KEY = System.getenv("HF_TOKEN");
    public String askAI(String prompt) {

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HTTP-Referer", "http://localhost");
            conn.setRequestProperty("X-Title", "AI Interview App");
            conn.setDoOutput(true);

            String jsonInput = "{"
                    + "\"model\": \"mistralai/mistral-7b-instruct\","
                    + "\"messages\": ["
                    + "{ \"role\": \"system\", \"content\": \"You are a professional HR recruiter conducting a Java Developer interview. Ask one question at a time and evaluate answers briefly.\" },"
                    + "{ \"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\" }"
                    + "]"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
                os.flush();
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            return extractMessage(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error communicating with AI.";
        }
    }

    // Extract assistant message from OpenRouter JSON
    private String extractMessage(String json) {
        try {
            int index = json.indexOf("\"content\":\"");
            if (index == -1) return json;

            String result = json.substring(index + 11);
            result = result.substring(0, result.indexOf("\""));

            return result.replace("\\n", "\n");

        } catch (Exception e) {
            return json;
        }
    }
}