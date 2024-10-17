# Java Chatbot CLI

Welcome to the **Java Chatbot CLI**! This is a simple yet powerful command-line application that allows you to interact with a chatbot powered by the Groq API. Whether you’re looking to get answers to your questions or just want to have a chat, this chatbot is here to help!

## Prerequisites

Before you get started, ensure you have the following installed on your machine:

- **Java JDK** (version 8 or higher)
- **Maven**

## Installation and Setup

Follow these steps to set up the project on your local machine:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/ppewjjdup/ChatBot.git
   cd ChatBot/
   ```

2. **Set Up Your Groq API Key**:
   - Create a `config.properties` file in the `src/main/resources` directory.
   - Add your Groq API key to the file like this:
     ```
     api.key=your_api_key_here
     ```

## Building the Application

To compile the code and create a JAR file, run the following command from the project root directory:

```bash
mvn clean package
```

This command will build the project and place the resulting JAR file in the `target` directory.

## Running the Application

To start the Chatbot CLI, use the command below:

```bash
java -jar target/Ai-Chatbot-CLI-1.0-SNAPSHOT.jar
```

## Usage

Once the application is running, you can interact with the chatbot in the console:

1. **Type your messages or questions** and press **Enter** to send them.
2. The chatbot will respond based on the output from the Groq API.
3. To exit the application, simply type **'/quit'**.
4. Your chat history will be saved in a file named `conversation.xml`.

## Available Commands

While interacting with the chatbot, you can use the following commands:

- **/help**: Displays a list of available commands and how to use them.
- **/reset**: Resets the conversation, clearing the chat history while saving the current conversation to an XML file.
- **/quit**: Saves the conversation to an XML file and exits the application.

## Troubleshooting

If you run into any issues, here are a few things to check:

- Make sure your Groq API key is correctly set in the `config.properties` file.
- Ensure you have the required version of Java installed.
- Verify that all dependencies are listed correctly in the `pom.xml` file.

## Contributing

We welcome contributions to this project! If you have any improvements or new features in mind, please feel free to submit a Pull Request.
