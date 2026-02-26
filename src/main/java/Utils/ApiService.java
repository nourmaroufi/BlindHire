package Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Handles all external API calls:
 * 1. randomuser.me  → random fake/blind name (free, no key needed)
 * 2. Groq API       → extract CV data using llama-3.3-70b (free tier)
 *
 * ── GET A FREE GROQ API KEY ───────────────────────────────────────────────────
 *   1. Go to https://console.groq.com
 *   2. Sign up / sign in
 *   3. Click "API Keys" → "Create API Key"
 *   4. Copy the key (starts with gsk_...) and paste it below
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class ApiService {

    // ── Paste your Groq API key here ──────────────────────────────────────────
    private static final String GROQ_API_KEY = "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";

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    // =========================================================================
    //  RANDOM NAME  —  randomuser.me (free, no key needed)
    // =========================================================================

    /**
     * Fetches a random name from randomuser.me.
     * @return String[2] { firstName, lastName } or null on failure
     */
    public static String[] fetchRandomName() {
        try {
            URL url = new URL("https://randomuser.me/api/?inc=name&noinfo");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            if (conn.getResponseCode() != 200) return null;
            String json = readStream(conn.getInputStream());
            conn.disconnect();
            // {"results":[{"name":{"title":"Mr","first":"John","last":"Doe"}}]}
            String first = extractSimpleValue(json, "first");
            String last  = extractSimpleValue(json, "last");
            if (first != null && last != null)
                return new String[]{ capitalize(first), capitalize(last) };
        } catch (Exception e) {
            System.err.println("[RandomUser] " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    //  CV PARSING  —  Groq API (llama-3.3-70b-versatile, free tier)
    // =========================================================================

    /** Holds the 4 extracted fields from a CV. */
    public static class CvData {
        public String skills     = "";
        public String diplomas   = "";
        public String experience = "";
        public String bio        = "";
    }

    /**
     * Sends raw CV text to Groq and returns structured CvData.
     * @param cvText  raw text already extracted from the PDF by PdfReader
     */
    public static CvData extractCvData(String cvText) {
        if (GROQ_API_KEY.equals("YOUR_GROQ_API_KEY_HERE") || GROQ_API_KEY.isBlank()) {
            CvData err = new CvData();
            err.skills = "⚠ Add your Groq API key in Utils/ApiService.java";
            return err;
        }
        if (cvText == null || cvText.trim().isEmpty()) return null;

        // Truncate to stay within token limits
        if (cvText.length() > 12000) cvText = cvText.substring(0, 12000);

        String systemPrompt =
                "You are a CV parser. Extract information from CVs and return ONLY a raw JSON object. " +
                        "No markdown, no backticks, no explanation whatsoever. Just the JSON.";

        String userPrompt =
                "Extract the following from this CV and return ONLY a JSON object with exactly these 4 keys:\n" +
                        "- skills: comma-separated list of all technical and soft skills\n" +
                        "- diplomas: highest degree and institution (e.g. 'Bachelor in Computer Science, MIT')\n" +
                        "- experience: short summary of work experience (e.g. '3 years in web development and databases')\n" +
                        "- bio: one short sentence describing the candidate\n\n" +
                        "If a field is not found, use an empty string.\n\n" +
                        "CV TEXT:\n" + cvText;

        try {
            // Groq uses the OpenAI-compatible chat completions format
            String body = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": " + toJsonString(systemPrompt) + "},"
                    + "  {\"role\": \"user\",   \"content\": " + toJsonString(userPrompt)   + "}"
                    + "],"
                    + "\"temperature\": 0.1"
                    + "}";

            URL url = new URL(GROQ_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("[Groq] HTTP " + status + ": " + readStream(conn.getErrorStream()));
                return null;
            }

            String fullResponse = readStream(conn.getInputStream());
            conn.disconnect();

            System.out.println("[Groq] Raw response:\n" + fullResponse); // debug

            return parseGroqResponse(fullResponse);

        } catch (Exception e) {
            System.err.println("[Groq] " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    //  PARSING
    // =========================================================================

    /**
     * Groq (OpenAI-compatible) response structure:
     * {
     *   "choices": [{
     *     "message": {
     *       "content": "{ our JSON here }"
     *     }
     *   }]
     * }
     * We extract the value of "content", strip any markdown fences,
     * then parse our 4 fields from the clean JSON.
     */
    private static CvData parseGroqResponse(String response) {
        // Find "content" key — it appears once in the choices array
        String content = extractSimpleValue(response, "content");
        if (content == null || content.isEmpty()) {
            System.err.println("[Groq] No 'content' field found in response");
            return null;
        }

        // Strip markdown fences just in case the model added them
        content = content.replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*",     "")
                .trim();

        System.out.println("[Groq] Extracted content:\n" + content); // debug

        CvData data = new CvData();
        String skills     = extractSimpleValue(content, "skills");
        String diplomas   = extractSimpleValue(content, "diplomas");
        String experience = extractSimpleValue(content, "experience");
        String bio        = extractSimpleValue(content, "bio");

        data.skills     = skills     != null ? skills     : "";
        data.diplomas   = diplomas   != null ? diplomas   : "";
        data.experience = experience != null ? experience : "";
        data.bio        = bio        != null ? bio        : "";

        return data;
    }

    // =========================================================================
    //  SHARED HELPERS
    // =========================================================================

    /**
     * Extracts the string value of a JSON key: "key": "value"
     * Handles escape sequences inside the value.
     */
    private static String extractSimpleValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;

        int colon = json.indexOf(":", idx + search.length());
        if (colon == -1) return null;

        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length() || json.charAt(start) != '"') return null;

        StringBuilder sb = new StringBuilder();
        int j = start + 1;
        while (j < json.length()) {
            char c = json.charAt(j);
            if (c == '\\' && j + 1 < json.length()) {
                char next = json.charAt(j + 1);
                switch (next) {
                    case '"':  sb.append('"');  j += 2; continue;
                    case '\\': sb.append('\\'); j += 2; continue;
                    case 'n':  sb.append('\n'); j += 2; continue;
                    case 'r':  sb.append('\r'); j += 2; continue;
                    case 't':  sb.append('\t'); j += 2; continue;
                    default:   sb.append(next); j += 2; continue;
                }
            }
            if (c == '"') break;
            sb.append(c);
            j++;
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append('\n');
        br.close();
        return sb.toString();
    }

    private static String toJsonString(String s) {
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}