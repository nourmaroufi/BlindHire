package Service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * AI matching service powered by Groq's ultra-fast inference API.
 * Uses llama-3.1-8b-instant (free tier) to score candidate vs job skill match.
 *
 * Set your key: Environment variable GROQ_API_KEY
 * Get a free key at: https://console.groq.com
 */
public class AiMatchingService {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL        = "llama-3.1-8b-instant"; // fast & free on Groq

    /**
     * Returns a match score 0-100 between job requirements and candidate skills.
     *
     * @param jobSkills       Required skills from the job offer.
     * @param candidateSkills Skills listed on the candidate's profile.
     * @return Numeric string e.g. "82". Throws IOException on API failure.
     */
    public static String getMatchScore(String jobSkills, String candidateSkills) throws IOException {

        // 1. Try environment variable (works in terminal/production)
        String apiKey = System.getenv("GROQ_API_KEY");

        // 2. Try Java system property (set in IDE run config via -DGROQ_API_KEY=...)
        if (apiKey == null || apiKey.isEmpty())
            apiKey = System.getProperty("GROQ_API_KEY");

        // 3. Hardcoded fallback for development — replace with your key from https://console.groq.com
        if (apiKey == null || apiKey.isEmpty())
            apiKey = "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";

        // Handle missing/empty skill data gracefully
        if (jobSkills == null || jobSkills.isBlank()) {
            return "50"; // neutral score when job has no listed skills
        }
        if (candidateSkills == null || candidateSkills.isBlank()) {
            return "0";  // no candidate skills = no match
        }

        OkHttpClient client = new OkHttpClient();

        // Build the prompt — instruct model to reply with a plain number only
        String systemPrompt =
                "You are a recruitment assistant. Your only task is to output a single integer " +
                        "from 0 to 100 representing how well a candidate's skills match a job's required skills. " +
                        "Do not explain. Do not add any text. Output only the integer.";

        String userPrompt =
                "Job required skills: " + jobSkills + "\n" +
                        "Candidate skills: " + candidateSkills + "\n" +
                        "Match score (0-100):";

        // Build Groq-compatible request body (OpenAI chat completions format)
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", userPrompt);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("max_tokens", 10);       // we only need 1-3 digits
        requestBody.put("temperature", 0.0);     // deterministic output
        requestBody.put("messages", new JSONArray()
                .put(new JSONObject().put("role", "system").put("content", systemPrompt))
                .put(message)
        );

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(GROQ_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "no body";
                throw new IOException("Groq API error " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            // Parse: choices[0].message.content
            String raw = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

            // Extract digits only (defensive — model may add punctuation)
            String digits = raw.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                throw new IOException("Groq did not return a numeric score. Raw: " + raw);
            }

            int score = Math.min(100, Math.max(0, Integer.parseInt(digits)));
            return String.valueOf(score);
        }
    }
}