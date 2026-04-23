package com.smartcampus.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

/**
 * Simple console client for the Smart Campus REST API.
 *
 * <p>Uses Java 11's built-in {@link java.net.http.HttpClient} so there are no
 * extra dependencies. The menu exercises every endpoint the server exposes,
 * which is handy for a quick demo during the CW viva.</p>
 *
 * <p>Run with: {@code java -cp target/smart-campus-api.jar com.smartcampus.client.SmartCampusClient}</p>
 */
public class SmartCampusClient {

    private static final String DEFAULT_BASE = "http://localhost:8080/api/v1";

    private final String baseUrl;
    private final HttpClient http;

    public SmartCampusClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String base = (args.length > 0) ? args[0] : DEFAULT_BASE;
        SmartCampusClient client = new SmartCampusClient(base);

        System.out.println("Smart Campus CLI client");
        System.out.println("Talking to: " + base);
        System.out.println();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            printMenu();
            System.out.print("> ");
            String choice = in.readLine();
            if (choice == null) {
                break;
            }
            choice = choice.trim();
            if (choice.equals("0") || choice.equalsIgnoreCase("q")) {
                System.out.println("Bye.");
                return;
            }
            try {
                client.handle(choice, in);
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("1) Discovery        GET  /");
        System.out.println("2) List rooms       GET  /rooms");
        System.out.println("3) Get room         GET  /rooms/{id}");
        System.out.println("4) Create room      POST /rooms");
        System.out.println("5) Delete room      DEL  /rooms/{id}");
        System.out.println("6) List sensors     GET  /sensors");
        System.out.println("7) Get sensor       GET  /sensors/{id}");
        System.out.println("8) Create sensor    POST /sensors");
        System.out.println("9) Delete sensor    DEL  /sensors/{id}");
        System.out.println("10) List readings   GET  /sensors/{id}/readings");
        System.out.println("11) Post reading    POST /sensors/{id}/readings");
        System.out.println("0) Quit");
    }

    private void handle(String choice, BufferedReader in) throws IOException, InterruptedException {
        switch (choice) {
            case "1":
                get("");
                break;
            case "2":
                get("/rooms");
                break;
            case "3": {
                String id = ask(in, "room id: ");
                get("/rooms/" + id);
                break;
            }
            case "4": {
                String id = ask(in, "room id: ");
                String name = ask(in, "name: ");
                String cap = ask(in, "capacity: ");
                String body = String.format(
                        "{\"id\":\"%s\",\"name\":\"%s\",\"capacity\":%s}", id, name, cap);
                post("/rooms", body);
                break;
            }
            case "5": {
                String id = ask(in, "room id: ");
                delete("/rooms/" + id);
                break;
            }
            case "6": {
                String type = ask(in, "filter by type (blank for all): ");
                String path = type.isEmpty() ? "/sensors" : "/sensors?type=" + type;
                get(path);
                break;
            }
            case "7": {
                String id = ask(in, "sensor id: ");
                get("/sensors/" + id);
                break;
            }
            case "8": {
                String id = ask(in, "sensor id: ");
                String type = ask(in, "type: ");
                String roomId = ask(in, "roomId: ");
                String body = String.format(
                        "{\"id\":\"%s\",\"type\":\"%s\",\"roomId\":\"%s\",\"status\":\"ACTIVE\"}",
                        id, type, roomId);
                post("/sensors", body);
                break;
            }
            case "9": {
                String id = ask(in, "sensor id: ");
                delete("/sensors/" + id);
                break;
            }
            case "10": {
                String id = ask(in, "sensor id: ");
                get("/sensors/" + id + "/readings");
                break;
            }
            case "11": {
                String id = ask(in, "sensor id: ");
                String value = ask(in, "value: ");
                String body = String.format("{\"value\":%s}", value);
                post("/sensors/" + id + "/readings", body);
                break;
            }
            default:
                System.out.println("Unknown choice.");
        }
    }

    private static String ask(BufferedReader in, String prompt) throws IOException {
        System.out.print(prompt);
        String line = in.readLine();
        return (line == null) ? "" : line.trim();
    }

    private void get(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .GET()
                .build();
        send(req);
    }

    private void post(String path, String jsonBody) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        send(req);
    }

    private void delete(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .DELETE()
                .build();
        send(req);
    }

    private void send(HttpRequest req) throws IOException, InterruptedException {
        HttpResponse<String> resp = http.send(req, BodyHandlers.ofString());
        System.out.println("HTTP " + resp.statusCode());
        String body = resp.body();
        if (body != null && !body.isEmpty()) {
            System.out.println(body);
        }
    }
}
