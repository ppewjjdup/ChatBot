package com.ai.chatbot;

import java.util.Scanner;

public class ChatbotCLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ChatbotAPIClient apiClient = new ChatbotAPIClient();

        System.out.println("Welcome to Groq CLI!");

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("/quit")) {
                System.out.println("Goodbye!");
                break;
            }

            try {
                String response = apiClient.sendMessage(input);
                System.out.println("Groq : " + response);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
