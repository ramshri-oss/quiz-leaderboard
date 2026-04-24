import java.net.URI;
import java.net.http.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;

public class QuizLeaderboard {

    static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    static final String REG_NO = "RA2311003050182";

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Set<String> seen = new HashSet<>();
        Map<String, Integer> scores = new HashMap<>();

        // Poll API 10 times
        for (int i = 0; i < 10; i++) {

            String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + i;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());
            JsonNode events = root.get("events");

            for (JsonNode event : events) {

                String roundId = event.get("roundId").asText();
                String participant = event.get("participant").asText();
                int score = event.get("score").asInt();

                String key = roundId + "-" + participant;

                if (seen.contains(key)) continue;

                seen.add(key);

                scores.put(participant,
                        scores.getOrDefault(participant, 0) + score);
            }

            Thread.sleep(5000);
        }

        // Build leaderboard
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        scores.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(entry -> {
                    Map<String, Object> user = new HashMap<>();
                    user.put("participant", entry.getKey());
                    user.put("totalScore", entry.getValue());
                    leaderboard.add(user);
                });

        int total = scores.values().stream().mapToInt(Integer::intValue).sum();

        System.out.println("Leaderboard: " + leaderboard);
        System.out.println("Total Score: " + total);

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("regNo", REG_NO);
        requestBody.put("leaderboard", leaderboard);

        String json = mapper.writeValueAsString(requestBody);

        // Submit leaderboard
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> postResponse =
                client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = postResponse.body();

        System.out.println("Full Response:");
        System.out.println(responseBody);

        // Parse response JSON
        JsonNode responseJson = mapper.readTree(responseBody);

        // Print isCorrect if available
        if (responseJson.has("isCorrect")) {
            System.out.println("isCorrect: " + responseJson.get("isCorrect").asBoolean());
        } else {
            System.out.println("isCorrect not present (likely multiple attempts).");
        }

        // Always print totals for verification
        if (responseJson.has("submittedTotal")) {
            System.out.println("Submitted Total: " + responseJson.get("submittedTotal").asInt());
        }
        if (responseJson.has("expectedTotal")) {
            System.out.println("Expected Total: " + responseJson.get("expectedTotal").asInt());
        }
    }
}