# Java Chatbot CLI

This is a simple Java Chatbot Application that interacts with the Groq API.

## Prerequisites

- Java JDK (version 8 or higher)
- Maven

## Installation and Setup

1. Clone the repository:
   ```shell
   git clone https://github.com/yourusername/ChatBot.git
   cd ChatBot/
   ```

2. Set up your Groq API key:
    - Create a `config.properties` file in the `src/main/resources` directory
    - Add your Groq API key to the file:
      ```
      api.key=your_api_key_here
      ```

## Building the Application

To build the JAR file, run the following command in the project root directory:

```shell
mvn clean package
```

This will compile the code and create a JAR file in the `target` directory.

## Running the Application

To run the Chatbot CLI, use the following command:

```shell
java -jar target/Ai-Chatbot-CLI-1.0-SNAPSHOT.jar
```

## Usage

Once the application is running:

1. Type your messages or questions in the console.
2. Press Enter to send your message to the chatbot.
3. The chatbot will respond based on the Groq API's output.
4. To exit the application, type '/quit' and press Enter.

## Troubleshooting

If you encounter any issues:
- Ensure your Groq API key is correctly set in the `config.properties` file.
- Check that you have the required Java version installed.
- Verify that all dependencies are correctly specified in the `pom.xml` file.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
