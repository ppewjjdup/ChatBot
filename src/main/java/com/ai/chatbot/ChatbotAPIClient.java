package com.ai.chatbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ChatbotAPIClient {
    private final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private String API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public ChatbotAPIClient() {
        loadProperties();
    }

    // Method to load the properties file
    private void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }

            // Load the properties file
            properties.load(input);

            // Retrieve the values from the properties file
            this.API_KEY = properties.getProperty("api.key");

            if (this.API_KEY == null) {
                throw new IllegalStateException("API key is not set in properties file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String message) throws IOException {
        // Create the JSON body based on the correct format
        JsonObject jsonBody = new JsonObject();

        // Set the model
        jsonBody.addProperty("model", "llama-3.2-90b-text-preview");

        // Create the "messages" array with "role" and "content"
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        messages.add(userMessage);

        // Add the messages array to the JSON body
        jsonBody.add("messages", messages);

        // Build the request body
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)  // Correct Authorization format
                .build();

        // Execute the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorResponse = response.body().string();
                throw new IOException("Unexpected code " + response + " - " + errorResponse);
            }

            // Parse the JSON response
            JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);

            // Extract the text from the "choices" array
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                return choice.getAsJsonObject("message").get("content").getAsString();
            } else {
                throw new IOException("No choices found in the response");
            }
        }
    }
}
