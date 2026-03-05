package Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationService {

    // your local LibreTranslate
    private static final String BASE_URL = "http://127.0.0.1:5000";

    private final HttpClient http = HttpClient.newHttpClient();

    // simple cache to avoid translating same strings repeatedly
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String translate(String text, String sourceLang, String targetLang) throws IOException, InterruptedException {
        if (text == null) return "";
        String t = text.trim();
        if (t.isEmpty()) return "";
        if (targetLang == null || targetLang.isBlank() || targetLang.equalsIgnoreCase(sourceLang)) return text;

        String key = sourceLang + "->" + targetLang + "::" + t;
        String cached = cache.get(key);
        if (cached != null) return cached;

        String body =
                "q=" + enc(t) +
                        "&source=" + enc(sourceLang) +
                        "&target=" + enc(targetLang) +
                        "&format=text";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/translate"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("LibreTranslate " + res.statusCode() + ": " + res.body());
        }

        String translated = extractTranslatedText(res.body());
        cache.put(key, translated);
        return translated;
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private String extractTranslatedText(String json) {
        // expects {"translatedText":"..."}
        String key = "\"translatedText\":";
        int i = json.indexOf(key);
        if (i < 0) return json;
        int start = json.indexOf('"', i + key.length());
        int end = json.indexOf('"', start + 1);
        if (start < 0 || end < 0) return json;
        return json.substring(start + 1, end).replace("\\n", "\n").replace("\\\"", "\"");
    }
}