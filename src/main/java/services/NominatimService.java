package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NominatimService {

    private static final String API_URL = "https://nominatim.openstreetmap.org/search";

    // Returns [lat, lon] or null if not found
    public double[] searchLocation(String address) {
        try {
            String encoded = URLEncoder.encode(address, "UTF-8");
            String urlStr = API_URL + "?q=" + encoded + "&format=json&limit=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // Nominatim requires a User-Agent header
            conn.setRequestProperty("User-Agent", "HiringApp/1.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8")
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();

            String json = response.toString();

            // Parse lat and lon manually (no extra library needed)
            if (json.equals("[]") || json.isEmpty()) return null;

            double lat = extractDouble(json, "\"lat\":\"");
            double lon = extractDouble(json, "\"lon\":\"");

            return new double[]{lat, lon};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private double extractDouble(String json, String key) {
        int start = json.indexOf(key) + key.length();
        int end = json.indexOf("\"", start);
        return Double.parseDouble(json.substring(start, end));
    }
}