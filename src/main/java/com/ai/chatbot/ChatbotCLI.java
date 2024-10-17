package com.ai.chatbot;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.File;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;



class Message {
    String sender;
    String content;

    Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }
}

public class ChatbotCLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ChatbotAPIClient apiClient = new ChatbotAPIClient();
        List<Message> conversation = new ArrayList<>();

        // Get the current date and time when the conversation starts
        String conversationStartTime = getDateAndTime();

        System.out.println("Welcome to Chatbot CLI!");

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("/quit")) {
                System.out.println("Saving conversation and quitting...");
                saveConversationToXML(conversation, conversationStartTime);
                System.out.println("Goodbye!");
                break;
            }
            conversation.add(new Message("You", input));

            try {
                String response = apiClient.sendMessage(input);
                conversation.add(new Message("Groq", response));
                System.out.println("Groq: " + response);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                conversation.add(new Message("System", "Error: " + e.getMessage()));
            }
        }

        scanner.close();
    }

    private static String getDateAndTime() {
        // Get the current date and time with time zone
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        // Define the desired string format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        // Return the formatted date and time string
        return currentDateTime.format(formatter);
    }

    private static void saveConversationToXML(List<Message> conversation, String conversationStartTime) {
        File xmlFile = new File("conversation.xml");
        boolean fileExists = xmlFile.exists() && xmlFile.length() > 0;

        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            StringWriter stringWriter = new StringWriter();
            XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(stringWriter);

            if (!fileExists) {
                xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
                xmlStreamWriter.writeCharacters("\n");
                xmlStreamWriter.writeStartElement("conversations");
                xmlStreamWriter.writeCharacters("\n");
            }

            xmlStreamWriter.writeCharacters("  ");
            xmlStreamWriter.writeStartElement("conversation");
            xmlStreamWriter.writeAttribute("startTime", conversationStartTime);
            xmlStreamWriter.writeCharacters("\n");

            for (Message message : conversation) {
                xmlStreamWriter.writeCharacters("    ");
                xmlStreamWriter.writeStartElement("message");
                xmlStreamWriter.writeAttribute("sender", message.sender);
                xmlStreamWriter.writeCharacters("\n      ");
                xmlStreamWriter.writeStartElement("content");
                xmlStreamWriter.writeCData(message.content);
                xmlStreamWriter.writeEndElement(); // content
                xmlStreamWriter.writeCharacters("\n    ");
                xmlStreamWriter.writeEndElement(); // message
                xmlStreamWriter.writeCharacters("\n");
            }

            xmlStreamWriter.writeCharacters("  ");
            xmlStreamWriter.writeEndElement(); // conversation
            xmlStreamWriter.writeCharacters("\n");

            if (!fileExists) {
                xmlStreamWriter.writeEndElement(); // conversations
                xmlStreamWriter.writeCharacters("\n");
                xmlStreamWriter.writeEndDocument();
            }

            xmlStreamWriter.flush();
            xmlStreamWriter.close();

            String xmlContent = stringWriter.toString();

            if (fileExists) {
                // Read existing content
                BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals("</conversations>")) {
                        sb.append(xmlContent);
                    }
                    sb.append(line).append("\n");
                }
                reader.close();

                // Write updated content
                FileWriter writer = new FileWriter(xmlFile);
                writer.write(sb.toString());
                writer.close();
            } else {
                FileWriter writer = new FileWriter(xmlFile);
                writer.write(xmlContent);
                writer.close();
            }

            System.out.println("Conversation saved to conversation.xml");

        } catch (Exception e) {
            System.out.println("Error saving conversation to XML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
