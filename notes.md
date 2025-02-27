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
  - 