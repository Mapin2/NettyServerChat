# NettyServerChat

Simple TCP Netty/Java Server Chat based on a test for the Server Programmer position.

I leave here the specification:

# 1. SPECIFICATION

We are building a simple TCP chat server using Netty (https://netty.io/). Only the server
implementation is required. Utilities like telnet or nc ( netcat ) shall be used for connecting to
the server, perform commands and send messages.

There’s no need for authentication: once a user connects, a random nickname is assigned.

Nicknames are unique, and are released after disconnection.

Users can also join rooms, which are created ad hoc, and once in a room they are able to send and
receive messages.

## 1.1 First connection

Upon first connection the server replies with a welcome message :)

`$ nc localhost 5222`

>Hello Spiderman

## 1.2 Nickname change

Command `/nick <new name>` shall be used to change connected user nickname. If a name is
already being used, an appropriate error message should be displayed. For instance:

`$ nc localhost 5222`

>Hello Spiderman

`$ /nick Batman`

>Hello Batman


## 1.3 List rooms

Command `/list` would output all current room names in the server. For example:

`$ nc localhost 5222`

>Hello Spiderman

`$ /list`

>Superheroes Room

>Wannabe Superheroes Room

>Marvel

>DC

## 1.4 Join room

Command `/join <room name>` is used to join an already existing room, or create one as needed.

- The room should reply with a welcome message, telling how many users are connected in that moment.
- When joining, a user receives the historic of last 5 previous messages, in order.
- Rooms are automatically destroyed when the last connected user leaves.

`$ nc localhost 5222`

>Hello Spiderman

`/join superheroes room`

>Welcome to Superheroes Room, there are 3 users connected.

>Batman: I think I’m stronger than Spiderman

>Superman: yes, I think so...

## 1.5 Sending & receiving messages

Once a user has joined a room, she can freely type to send messages to the users in the chat room,
and will receive what others send.

To avoid spam, there is a message rate limit of max 30 messages per minute. Messages that exceed
the ratio should be automatically dropped.

## 1.6 Exit

Command `/exit` will disconnect the player from the server. Upon disconnection, the server shall
broadcast an information message to other members of the same chat room.

# 2. REMARKS

- High concurrency is expected.
- All data is stored in memory, no persistence to database needed.
- Only Java 8, Netty 4 and unit testing libraries are allowed.

# Assumptions / changes regarding the points of the specification:

With the first connection every one enter into an initial room, a kind of "General Room" channel to
bring it closer to what would be the actual operation of an application of this type. This channel,
unlike others, will not be deleted when it has 0 users.

All user names created by the / nick command will be limited to 20 characters and with lowercase
characters.

All room names created by the / join command will be limited to 20 characters and with lowercase
characters.

The / list command shows the list of channels and a (*) next to the name of the channel in which
you are at that moment.

# COMPILATION AND EXECUTION

- The project code is an exported eclipse project.
- Import the project to eclipse and execute a maven update on the project once imported into the package/project explorer.
- To execute it, the ChatServer.java class must be started (Run/Debug). Once this is done, we can connect using telnet, for example: "$ telnet localhost 8080".
 
