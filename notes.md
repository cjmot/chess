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