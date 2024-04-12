# Airport Check-In Counter System

## Objetive

This project involves the development of a thread-safe remote system for the allocation of airport check-in counters. The system allows notifying airlines and providing reports on check-ins performed up to the current moment. It addresses the fundamental aspects of the check-in counter assignment process from the perspectives of passengers, airlines, and airport administration.

### Prerequisites

The following prerequisites are needed to run the server executable as well as the client applications:
- Maven
- Java 19

### Compiling

To compile the project and get all executables, `cd` into the root of the project, and run the following command:

```Bash
mvn clean package
```

This will create two `.tar.gz` files, that contain all of the files necessary to run the clients and the server respectively.  Their location is:
* **Client**: `./client/target/....tar.gz`
* **Server**: `./server/target/....tar.gz`

### 
