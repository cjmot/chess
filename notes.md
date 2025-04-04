# CS240 Notes
## Phase 1 Design Principles
- Encapsulation
- Abstraction
- High Cohesion
- Low coupling
- SOLID
- Decomposition

- Yagni - You aren't gonna need it
- DRY - Don't repeat yourself
- Single Responsibility Interface Segregation
- Dependency Inversion
- Open/Closed
- Liskov Substitution

## Phose 2 Software Design Principles
- Single responsibility Principle (SRP)
  - Every class represents one well defined concept, all functionality relates to that one concept, and has good name that describes what it represents(noun)
  - Every method/function does one well-defined task, and has good name that describes what it does (usually verb/verb phrase)
- Avoid Code duplication
- Encapsulation/Information hiding
  - classes and methods/functions should hide their internal implementation details.
  - class members should be private when possible (ie limit visibility)
  - names should not unnecessarily reveal implementation details
    - StudentLinkedList vs ClassRoll

## Phase 3 Server implementation
- If web api requires auth token, handler can validate the auth token
  - put in handler base class or in Service class that can be shared
- best to create separate class with fromJson and toJson methods instead of calling Gson directly from handlers
- how are handlers created and invoked
```
Spark.post("/user", (req, res) -> (new RegisterHandler()).handlerRequest(req, res));
```
- multiple ways to avoid creating a new handler instance for every request
  - create and keep them in instance variables of your Server class
  - Use a static method (will make it difficult to avoid code duplication across handlers)
  - use the singleton pattern: Create a static getInstance() method that always returns the same instance.
- Areas of potential code duplication
  - HTTP handler classes
  - Service classes
  - Request/Result classes
  - DAO classes
- Use inheritance to avoid this kind of code duplication
  - put common code in base classes that can be inherited
- # JSON tips
  - make sure field names in request and response classes match exactly the names used in the specification (including capitalization)
  - not doing this will cause bugs
  - GSON does not serialize null fields (will be missing in the JSON)
  - helpful when generating Success and Failure JSON responses
  - 
```
    
class Response {
  String message;
}

class LoginResponse extends Response {
  String authtoken;
  String username;
}
```
- Generating auth token
  - UUID.randomUUID().toString();

- ## Server implementation approach
  - Review class structure diagram
  - create java packages in project to contain your server classes
  - create your server class and get static file handling to work so you can see the test page
  - Pick one WebAPI and get it working end-to-end, and test it with the test web page or curl
  - as you go, write junit test cases for service classes you create
  - repeat for the other Web APIs until you have written all 7 and are passing all of the passoff tests

### Phase 4 Database
- need 3 things to connect to db
  - hostname of computer running db (usually localhost)
  - port number server is listening on
  - username/password used to login to mySQL
  - name of database inside mySQL server
- DatabaseManager class shows how to do a lot of things
- look at db.properties in resources
  - list of properties in external file

- ## SQL
  - Language for performing relational database operations
    - Data Definition Language (DDL)
      - Create tables
      - Delete tables
      - Alter tables
    - Data Manipulation Language (DML)
      - insert rows
      - update rows
      - delete rows
    - Data Query Language (DQL)
      - Search the database tables for data of interest
- ## SQL Data types
  - Strings
    - each column in an sql table declares the type that column may contain
    - Character strings
      - CHARACTER(n) or CHAR(n) -- fixed-width n-character string, padded with spaces as needed
      - CHARACTER VARYING(n) or VARCHAR(n) -- variable-width string with a max size of n chars
    - Bit strings
      - BIT(n) -- an array of n bits
      - BIT VARYING(n) -- an array of up to n bits 
  - Numbers and Large Objects
    - Numbers
      - INTEGER and SMALLINT
      - FLOAT, REAL, and DOUBLE PRECISION
      - NUMERIC(precision, scale) or DECIMAL(precision, scale)
    - Large objects
      - BLOB -- binary large object (images, sound, video, etc)
      - CLOB -- character large object (text docs)
  - Date and Time
    - DATE - for date values (2011-05-03)
    - TIME -- for time values (15:51:36) granularity of the time value is usually a tick(100 nanoseconds)
    - TIME WITH TIME ZONE or TIMETZ -- same as TIME, but including details about the time zone.
    - TIMESTAMP -- DATE and TIME put together in one value (2011-05-03 15:51:36)
    - TIMESTAMP WITH TIME ZONE or TIMESTAMPTZ -- same as TIMESTAMP, but including details about the time zone.
- ## Creating and Dropping Tables
  - Creating tables
    - CREATE TABLE
      - Primary Keys
      - Null/Not Null
      - Autoincrement
      - Foreign keys
```
create table book/create table if not exists
{
    id integer not null primary key auto_increment,
    title varchar(255) not null,
    author varchar(32) not null,
    category_id integer not null,
    foreign key(genre) references genre(genre),
    foreign key(category_id) references category(id)
};
```
  - Foreign key constraints
    - not required - can query without them
    - enforce that values used as foreign keys exist in their parent tables
    - Disallow deletes of the parent table row when referenced as a foreign key in another table
    - Disallow updates of the parent row primary key value if that would "orphan" the foreign keys
    - can specify that deletes and/or updates to the primary keys automatically affect the foreign key rows
      - ```foreign key(genre) references genre(genre) on update cascade on delete restrict```
    - Available actions:
      - No Action, Restrict, Set Null, Set Default, Cascade
  - Dropping Tables
    - Drop Table
      - drop table book;
      - drop table if exists book;
    - when using foreign key constraints, order of deletes matters
      - can't delete a table with columns being used as foreign keys in another table (delete the table with the foreign keys first)
- **Inserting, Updating and deleting Rows**
  - Inserting Data into Tables
    - INSERT
      - insert into book
      - (title, author, genre, category_id) values ('The Work and the Glory', 'Gerald Lund', 'HistoricalFiction', 3);
  - Updates
    - UPDATE Table
    - Set Column = Value, Column = Value, ...
    - WHERE Condition
    - Change a member's information
      UPDATE member
      SET name = 'Chris Jones',
        email_address = 'chris@gmail.com'
      WHERE id = 3
    - Set all member email addresses to empty
      UPDATE member
      SET email_address = ''
  - Deletes
    - DELETE FROM Table
    - WHERE Condition
    - Delete a member
        DELETE FROM member
        WHERE id = 3
    - Delete all readings for a member
        DELETE FROM books_read
        WHERE member_id = 3
    - Delete all books
        DELETE FROM book
- **Retrieving Data with SQL Queries**
  - Queries
      SELECT Column, Column, ...
      FROM Table, Table, ...
      WHERE Condition
  - Queries - Cartesian Product
    - SELECT member.name, book.title
    - FROM member, books_read, book
  - Queries - Join
    - SELECT member.name, book.title
    - FROM member, books_read, book
    - WHERE member.id = books_read.member_id AND book.id = books_read.book_id
  - Queries - Join 2
    - SELECT member.name, book.title
    - FROM member, books_read, book
    - WHERE member.id = books_read.member_id AND book.id = books_read.book_id AND genre = "NonFiction"
  - Queries - Join 2 (with Join Clauses)
    - SELECT member.name, book.title
    - FROM member
    - INNER JOIN books_read ON member.id = books_read.member_id
    - INNER JOIN book ON books_read.book_id = book.id
    - WHERE genre = "NonFiction"
- **Database Transactions**
  - Database Transactions
    - by default, each SQL statement is executed in a transaction by itself
    - Transactions are useful when they consist of multiple SQL statements, since you want to make sure that either all of them or none of them succeed
    - For a multi-statement transaction,
      - BEGIN TRANSACTION;
      - SQL statement 1;
      - SQL statement 2;
      - ...
      - COMMIT TRANSACTION; or ROLLBACK TRANSACTION;

| Command         | Purpose                                                                 | Example statement                                                                                                            |
| --------------- | ----------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| CREATE DATABASE | Creates a new database.                                                 | CREATE DATABASE pet_store;                                                                                                   |
| ALTER DATABASE  | Modifies the structure of a database.                                   | ALTER DATABASE pet_store CHARACTER SET utf8mb4;                                                                              |
| DROP DATABASE   | Deletes a database.                                                     | DROP DATABASE pet_store;                                                                                                     |
| USE DATABASE    | Selects a database for use with future commands.                        | USE pet_store;                                                                                                               |
| CREATE TABLE    | Creates a new table in a database.                                      | CREATE TABLE pet (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, PRIMARY KEY (id)); |
| DESCRIBE TABLE  | Describes the fields in a table.                                        | DESCRIBE pet;                                                                                                                |
| ALTER TABLE     | Modifies the structure of a table.                                      | ALTER TABLE pet ADD COLUMN nickname VARCHAR(255);                                                                            |
| DROP TABLE      | Deletes a table from a database.                                        | DROP TABLE pet;                                                                                                              |
| INSERT INTO     | Inserts new data into a table.                                          | INSERT INTO pet (name, type) VALUES ('Puddles', 'cat');                                                                      |
| SELECT          | Select data from a table.                                               | SELECT name, type FROM pet;                                                                                                  |
| UPDATE          | Updates existing data in a table.                                       | UPDATE pet SET name = 'fido' WHERE id = 1;                                                                                   |
| DELETE          | Deletes data from a table.                                              | DELETE FROM pet WHERE id = 1;                                                                                                |
| CREATE INDEX    | Creates an index on a column or columns in a table.                     | CREATE INDEX pet_name_index ON pet (name);                                                                                   |
| DROP INDEX      | Deletes an index from a table.                                          | DROP INDEX pet_name_index;                                                                                                   |
| TRUNCATE TABLE  | Deletes all rows from a table, but preserves the table structure.       | TRUNCATE TABLE pet;                                                                                                          |
| CREATE VIEW     | Creates a virtual table that is based on one or more underlying tables. | CREATE VIEW cats AS SELECT \* FROM pet WHERE type = 'cat';                                                                   |
| DROP VIEW       | Deletes a view.                                                         | DROP VIEW cats;                                                                                                              |


***PHASE 5***
- building everything in chess client up until gameplay
- Read, eval, print loop
  - 3 sets, one pre-login, one for logged in, and one for gameplay
- Server facade handles all api requests, same as api folder/class in javascript


***PHASE 6***
- **WebSocket**
  - websocket only between two parties, to get a group involved all have to be connected to the server, the server will receive messages and distribute them.
  - Spark.webSocket("/ws", WSServer.class)
    - onMessage needs to be included
- Creating websocket Client connection
  - needs library that implements javax.websocket.WebSocketContainer interface. 
    - glassfish.tyrus library used in this class
      - INSTALL: org.glassfish.tyrus.bundles:tyrus-standalone-client:1.15
  - needs onOpen method on javax.websocket.Endpoint abstract class to create class that will handle sending and receiving messages
  - 
```java
import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

public class WSClient extends Endpoint {
    public static void main(String[] args) throws Exception {
        var ws = new WSClient();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter message you want to echo");
        while (true) ws.send(scanner.nextLine());
    }
    
    public Session session;
    
    public WSClient() throws Exception {
        URI uri = new URI("ws://localhost:8080/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }
    
    public void send(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }
    
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}

```

- **Overview**
  - phase 6 implement all gameplay functionality
    - only piece missing after phase 5
  - when user in client 3 phases
    - logged out
    - logged in
    - gameplay
  - gameplay commands
    - Help
    - Redraw chess board
    - leave
    - make move
    - resign
    - highlight legal moves
  - Notification specs
    1. user connected to the game (black or white) - should include player name and what side they are playing
    2. user connected as observer. message should include observer name
    3. player made a move - should include player's name and description of move that was made (in addition to board being updated on each player's screen)
    4. player left the game (should include player name)
    5. player resigned the game (should include player name)
    6. player is in check (should include player's name) (generated by the server)
    7. player is in checkmate (message should include player's name) (generated by the server)
  
  - Websocket specs each time player begins playing or observing game
    1. call server join api to join to game (ONLY when playing not observing)
    2. Open Websocket connection with the server (using /ws endpoint) so can send and receive gameplay messages
    3. send connect websocket message to server
    4. transition to gameplay UI. gameplay UI draws the chessboard and allows user to perform the gameplay commands
  - WebSocket messages
    - UserGameCommand
    - ServerMessage
    - both included in starter code
    - needs subclasses to include more info in class
    - ServerMessage
      - needs subclasses
  - WebSocket interactions
    - (See docs)
    - instigating client = Root Client
      - server receives CONNECT command and sends appropriate ServerMessages to all clients connected to the game
    - when sending Notification that refers to one of the clients, message should use the username
    - if UserGameCommand is invalid (invalid auth or gameID doesn't exist) server should only send Error message to Root Client. No message sent to other Clients. Error message must contain "error" case independent
    - 