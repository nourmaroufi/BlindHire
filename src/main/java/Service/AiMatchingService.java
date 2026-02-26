package Service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AiMatchingService {

    private static final String API_URL = "https://api.openai.com/v1/responses";

    /**
     * Returns the AI match percentage (0-100) between a job and candidate skills.
     *
     * @param jobDescription  The job's required skills/description.
     * @param candidateSkills The candidate's skills.
     * @return Numeric match as string (e.g., "82"). Throws IOException on failure.
     */
    public static String getMatchScore(String jobDescription, String candidateSkills) throws IOException {

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("OPENAI_API_KEY not set");
        }

        OkHttpClient client = new OkHttpClient();

        // Build prompt for AI
        String prompt = "Rate the match between this job and candidate skills from 0 to 100.\n\n"
                + "Job Description: " + jobDescription + "\n"
                + "Candidate Skills: " + candidateSkills + "\n"
                + "Return only the number (0-100).";

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", "gpt-5.2");
        jsonBody.put("input", prompt);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("OpenAI API call failed: " + response);
        }

        String responseBody = response.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Safely parse output array
        JSONArray outputArray = jsonResponse.optJSONArray("output");
        if (outputArray == null || outputArray.length() == 0) {
            throw new IOException("No output from OpenAI API");
        }

        StringBuilder textBuilder = new StringBuilder();

        for (int i = 0; i < outputArray.length(); i++) {
            JSONObject item = outputArray.getJSONObject(i);
            if ("output_text".equals(item.optString("type"))) {
                textBuilder.append(item.optString("text", ""));
            }
        }

        String rawText = textBuilder.toString().trim();

        // Extract digits only
        String digits = rawText.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            throw new IOException("AI did not return a numeric match");
        }

        // Limit to 0-100 just in case
        int percentage = Math.min(100, Math.max(0, Integer.parseInt(digits)));

        return String.valueOf(percentage);
    }
}