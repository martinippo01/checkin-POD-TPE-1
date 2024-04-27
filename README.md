# Airport Check-In Counter System

## Objetive

This project involves the development of a thread-safe remote system for the allocation of airport check-in counters. The system allows notifying airlines and providing reports on check-ins performed up to the current moment. It addresses the fundamental aspects of the check-in counter assignment process from the perspectives of passengers, airlines, and airport administration.

### Prerequisites

The following prerequisites are needed to run the server executable as well as the client applications:
- Maven >= 3.9.6
- Java == 17

### Compiling

To compile the project and get all executables, `cd` into the root of the project, and run the following command:

```Bash
bash ./compile.sh
```

This will create all necessary files, to run the clients and the server respectively.

### Running the Server

Running the server is as simple as running the following command in the root folder of this project:

```bash
bash ./run_server.sh
```

### Running the Clients

All client scripts are in `client/target/grpc-com-tpe1-client-2024.1Q/`.
So, for running any of them, one must `cd` into this directory first:
```bash
cd "client/target/grpc-com-tpe1-client-2024.1Q/"
```

And then the desired `*Client.sh`

```bash
bash adminClient.sh -DserverAddress="localhost:50058" -Daction=addSector -Dsector=C
```


## Team members

- Marco Scilipoti - 62512
- Martín Ippolito - 62510
- Martín E. Zahnd - 60401
- Santiago Andrés Larroudé Alvarez - 60460
