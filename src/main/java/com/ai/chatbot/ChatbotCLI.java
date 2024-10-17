package com.ai.chatbot;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import javax.xml.stream.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;


enum AIModel {
    GEMMA2_9B("gemma2-9b-it"),
    GEMMA_7B("gemma-7b-it"),
    LLAMA3_GROQ_70B_TOOL_USE("llama3-groq-70b-8192-tool-use-preview"),
    LLAMA3_GROQ_8B_TOOL_USE("llama3-groq-8b-8192-tool-use-preview"),
    LLAMA3_1_405B("llama-3.1-405b"),
    LLAMA3_1_70B("llama-3.1-70b-versatile"),
    LLAMA3_1_8B("llama-3.1-8b-instant"),
    LLAMA3_2_1B("llama-3.2-1b-preview"),
    LLAMA3_2_3B("llama-3.2-3b-preview"),
    LLAMA_GUARD_3_8B("llama-guard-3-8b"),
    LLAVA_1_5_7B("llava-v1.5-7b-4096-preview"),
    META_LLAMA3_70B("llama3-70b-8192"),
    META_LLAMA3_8B("llama3-8b-8192"),
    MIXTRAL_8X7B("mixtral-8x7b-32768");

    private final String modelId;

    AIModel(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }
}

class Message {
    String sender;
    String content;
    String timestamp;

    Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = getDateAndTime();
    }

    private static String getDateAndTime() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }
}

public class ChatbotCLI {
    private static final String SYSTEM_PROMPT = "You are a knowledgeable, efficient, and direct Al assistant. Provide concise answers, focusing on the key information needed. Offer suggestions tactfully when appropriate to improve outcomes. Engage in productive collaboration with the user utilising multi-step reasoning to answer the question, if there are multiple questions in the initial question split them up and answer them in the order that will provide the most accurate response.";
    private static final String XML_FILE_NAME = "conversation.xml";
    private static final Logger logger = Logger.getLogger(ChatbotCLI.class.getName());
    private static boolean isModelSelected = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ChatbotAPIClient apiClient = new ChatbotAPIClient(SYSTEM_PROMPT);
        List<Message> conversation = new ArrayList<>();

        String conversationStartTime = getDateAndTime();
        System.out.println("Welcome to Chatbot CLI!");
        printHelp();

        while (true) {
            try {
                if(!isModelSelected){
                    System.out.println("\nChoose which model you want to use ");
                    System.out.println("\nAvailable Models : ");

                    for (AIModel model: AIModel.values()){
                        System.out.println(model.ordinal() + 1 +" "+ model.getModelId());
                    }
                    System.out.println("0. exit");
                    int choice = scanner.nextInt();
                    scanner.nextLine();  // Properly consume the newline character

                    if(choice == 0){
                        break;
                    }

                    if(choice < 1 || choice > AIModel.values().length){
                        System.out.println("Incorrect Input. exiting ..");
                    }

                    apiClient.MODEL_ID = AIModel.values()[choice - 1].getModelId();

                    System.out.println("Chosen Model : " + AIModel.values()[choice - 1].getModelId());
                    isModelSelected = true;
                }

                System.out.print("You: ");
                String input = scanner.nextLine().trim();

                if (handleCommand(input, apiClient, conversation, conversationStartTime)) {
                    continue;
                }

                conversation.add(new Message("You", input));

                String response = apiClient.sendMessage(input);
                conversation.add(new Message("Groq", response));
                System.out.println("Groq: " + response);
            } catch (Exception e) {
                handleError(e, conversation);
            }
        }
    }

    private static boolean handleCommand(String input, ChatbotAPIClient apiClient, List<Message> conversation, String conversationStartTime) {
        switch (input.toLowerCase()) {
            case "/quit":
                System.out.println("Saving conversation and quitting...");
                saveConversationToXML(conversation, conversationStartTime);
                System.out.println("Goodbye!");
                System.exit(0);
                return true;
            case "/reset":
                System.out.println("Saving and resetting conversation...");
                apiClient.resetConversation();
                saveConversationToXML(conversation, conversationStartTime);
                conversation.clear();
                System.out.println("Conversation reset. You can start a new conversation.");
                return true;
            case "/help":
                printHelp();
                return true;
            default:
                return false;
        }
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("/help  - Show this help message");
        System.out.println("/reset - Reset the conversation");
        System.out.println("/quit  - Save the conversation and exit");
    }

    private static void handleError(Exception e, List<Message> conversation) {
        String errorMessage = "Error: " + e.getMessage();
        System.err.println(errorMessage);
        conversation.add(new Message("System", errorMessage));
        logger.log(Level.SEVERE, "An error occurred", e);
    }

    private static String getDateAndTime() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }

    private static void saveConversationToXML(List<Message> conversation, String conversationStartTime) {
        File xmlFile = new File(XML_FILE_NAME);
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

            writeConversationToXML(xmlStreamWriter, conversation, conversationStartTime);

            if (!fileExists) {
                xmlStreamWriter.writeEndElement(); // conversations
                xmlStreamWriter.writeCharacters("\n");
                xmlStreamWriter.writeEndDocument();
            }

            xmlStreamWriter.flush();
            xmlStreamWriter.close();

            String xmlContent = stringWriter.toString();
            appendOrWriteToFile(xmlFile, xmlContent, fileExists);

            System.out.println("Conversation saved to " + XML_FILE_NAME);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving conversation to XML", e);
        }
    }

    private static void writeConversationToXML(XMLStreamWriter writer, List<Message> conversation, String startTime) throws XMLStreamException {
        writer.writeCharacters("  ");
        writer.writeStartElement("conversation");
        writer.writeAttribute("startTime", startTime);
        writer.writeCharacters("\n");

        for (Message message : conversation) {
            writer.writeCharacters("    ");
            writer.writeStartElement("message");
            writer.writeAttribute("sender", message.sender);
            writer.writeAttribute("timestamp", message.timestamp);
            writer.writeCharacters("\n      ");
            writer.writeStartElement("content");
            writer.writeCData(message.content);
            writer.writeEndElement(); // content
            writer.writeCharacters("\n    ");
            writer.writeEndElement(); // message
            writer.writeCharacters("\n");
        }

        writer.writeCharacters("  ");
        writer.writeEndElement(); // conversation
        writer.writeCharacters("\n");
    }

    private static void appendOrWriteToFile(File file, String content, boolean append) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            if (append) {
                long length = raf.length();
                long position = length - 20; // Assuming </conversations> is within the last 20 bytes
                String endTag = "</conversations>";
                byte[] buffer = new byte[20];

                while (position >= 0) {
                    raf.seek(position);
                    raf.readFully(buffer);
                    String bufferStr = new String(buffer);
                    int tagIndex = bufferStr.lastIndexOf(endTag);
                    if (tagIndex != -1) {
                        raf.setLength(position + tagIndex);
                        break;
                    }
                    position--;
                }

                raf.seek(raf.length());
                raf.writeBytes(content);
                raf.writeBytes("</conversations>\n");
            } else {
                raf.setLength(0);
                raf.writeBytes(content);
            }
        }
    }
}