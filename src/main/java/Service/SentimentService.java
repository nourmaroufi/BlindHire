package Service;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SentimentService {

    private static final String API_URL = "https://api.api-ninjas.com/v1/sentiment";
    private static final String API_KEY1 = System.getenv("API_KEY1"); // paste your key

    // Returns result like "Positive (0.85)" or "Negative (0.60)"
    public String analyze(String text) {
        try {
            String encoded = URLEncoder.encode(text, "UTF-8");
            URL url = new URL(API_URL + "?text=" + encoded);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Api-Key", API_KEY1);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8")
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();
            System.out.println("RAW SENTIMENT RESPONSE: " + response.toString());
            return parseResult(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "Sentiment unavailable.";
        }
    }

    // Response looks like: {"sentiment":"positive","score":0.85}
    private String parseResult(String json) {
        try {
            // Handle both "sentiment":"value" and "sentiment": "value"
            String sentiment = json.contains("\"sentiment\": \"")
                    ? extractString(json, "\"sentiment\": \"")
                    : extractString(json, "\"sentiment\":\"");

            // Score can be "score": 0.85 or "score":0.85
            String scoreRaw = json.contains("\"score\": ")
                    ? extractString(json, "\"score\": ")
                    : extractString(json, "\"score\":");

            double score = Double.parseDouble(scoreRaw.replaceAll("[^0-9.]", ""));

            String emoji = switch (sentiment.toLowerCase().trim()) {
                case "positive" -> "🟢 Positive";
                case "negative" -> "🔴 Negative";
                default         -> "🟡 Neutral";
            };

            return emoji + " (" + String.format("%.0f", score * 100) + "% confidence)";

        } catch (Exception e) {
            e.printStackTrace();
            return "Could not parse sentiment. Raw: " + json; // temporarily show raw response
        }
    }

    private String extractString(String json, String key) {
        int start = json.indexOf(key) + key.length();
        int end   = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : json.substring(start);
    }

}