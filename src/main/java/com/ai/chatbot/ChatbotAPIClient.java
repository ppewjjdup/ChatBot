package com.ai.chatbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ChatbotAPIClient {
    private final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private String API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private final String systemPrompt;
    private final List<JsonObject> conversationHistory = new ArrayList<>();

    public ChatbotAPIClient(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        loadProperties();
        initializeConversation();
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            properties.load(input);
            this.API_KEY = properties.getProperty("api.key");
            if (this.API_KEY == null) {
                throw new IllegalStateException("API key is not set in properties file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeConversation() {
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemPrompt);
            conversationHistory.add(systemMessage);
        }
    }

    public String sendMessage(String message) throws IOException {
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        conversationHistory.add(userMessage);

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "llama-3.2-11b-text-preview");
        jsonBody.add("messages", gson.toJsonTree(conversationHistory));

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                String assistantResponse = choice.getAsJsonObject("message").get("content").getAsString();

                JsonObject assistantMessage = new JsonObject();
                assistantMessage.addProperty("role", "assistant");
                assistantMessage.addProperty("content", assistantResponse);
                conversationHistory.add(assistantMessage);

                return assistantResponse;
            } else {
                throw new IOException("No choices found in the response");
            }
        }
    }

    public void resetConversation() {
        conversationHistory.clear();
        initializeConversation();
    }
}