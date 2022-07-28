# PennPals
This project involves building an internet chat server in Java. The code models the internal state of the server and handle communication with chat users. The design principles for the project:   
- **Client** - user who has connected to the server to chat with other users. Each client is assigned a unique **integer ID** when they sign in. This integer cannot be changed, and persists as long as the client is connected. If a client later reconnects, they will get a new integer ID.
Each client has a **nickname** - a string that other users can see. A user can change their own nickname at any time.   
- **Server** - A computer system designed to provide a service to many connected clients at once.   
- **Channel** - A group of users who are connected to the server. Every channel has an owner - the user who created the channel.
  - A user in a channel receives all messages sent to that channel after they join.   
  - A user in a channel can leave the channel at any time. After they leave, they will no longer get messages from that channel.
  - A user connected to the server can be in any number of channels (including 0).   
- **Channel Privacy** - A channel is either public or private. If it is public, any user who knows the name of the channel can join. If it is private, new users can only be added if the owner invites them - in which case they automatically join.   
- **Protocol** - A standardized set of instructions for communicating over the Internet by requesting and transmitting data.   
In this project, clients communicate with the server by sending commands that instruct the server to create new channels, send messages on channels, etc. We represent this communication protocol in the code base by using subclasses of an abstract class ``Command``.
