# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## Phase 2 Server Sequence Diagram
[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3SblVZPQN++oQADW6ErU32jsohfgyHM5QATE4nN0y0MxWMYFXHlNa6l6020C3Vgd0BxTF5fP4AtB2OSYAAZCDRJIBNIZLLdvJF4ol6p1JqtAzqBJoIei0azF5vDgHYsgwr5ks9K+KDvvorxLAC5wFrKaooOUCAHjysL7oeqLorE2IJoYLphm6ZIUgatLlqOIELEsJpEuGFocjA3K8gagrCjAQEwOcMAfksoYUW6naYeURraI6zoEjhLLlJ63pBgGQYhq6LKRtR0YwLGwYCZhnb-mmyE8tmuaYBpPFXmmgFEW+LFBrO6DfoZnbZD2MD9oOvRAbM04WWgS6cKu3h+IEXgoOge4Hr4zDHukmSYLZF5FNQ17SAAorucX1HFzQtA+qhPt0rmNug7a-mcQIltlc56ZBHbQU68owPB9jBUhQW+qhGIYXKWHCVxolGCg3CZMpUl1jlaDkUy3HyeU0jdRShjKfGrXqWVaY1cFOkIHmC0GTFRkwFZm02eeYB9gOQ7bWYnmeN5G6Qrau7QjAADio6sqFp4RftbI-uUFR3UlqX2KOWXmYNeVshp5TFbl+kVeC1XQg9oyqP1M6DS1MEaiJpIwOSYB9eDQ2cSN0pjTRPK2tODFhLj+NmhGUNVWTqlzdhHUY8gsRw2ohEjqMw3U1R5S0aTyqY49QoiiZYiyeaSaXjBpQVH9oyMo0gmJsmC3lDdbOPSta2FSCFUAQraifT0RsAJLSLMJ6ZAadyzN0MBCggoANrbxHC6MAByo57I0O3HNLXY5Ad9lHb0RvupUpujhbVthfq4v20kTsu27plG97oy+x5K7neugTYD4UDYNw8C6pk92jik8evcH72GZ9tQNL9-3BIDc5DhnPvA2retg+36Cd6OmcoBBev17LMDiZk7OwnA5coOzTXoSrbUyOjkdYzjA945LEaFJaAtmXG2ii8fSMlXvG2VbB5+zajTMExj0+L6OsLszJG980pNqV4rDMP17qmco88vQz1HDrUq49A4fVLObaQ-tyqnEiiHByDto6jAtjnLy+cAiWG6vBZIMAABSEAeR-0MAEHQzsQANlruYCeJRG6UjvC0I2AMBod16CXYA+CoBwAgPBKAsx4GIPyqDc+bkhw8L4QIoRIiY4IKgamRh0MABWZC0Cz1ITyJeKA0TNVXmjZmm8KTb04egHmlEiZH3pvIM+lMr4wNav3E+8gjGP2pnhMAs94FWNGgfaiR8jZn3gVTaxtNb5G3vjfeafcSGaL0TmVayioIGy2mIvawdDqOR6CdZcOCfIBC8LwrsXpYDAGwCXQg8REjVxeigxhsUEpJRSq0YwPc-zq1LCdSGMsb5iW4HgOeQysz6LQliDx7Un6RxAKM2E-jCaBPGpNCuwAhb8XkAo-+WyWK8jscADxQDgQgNGZAyG6SrgnR-FkuyaDej5M8kAA)