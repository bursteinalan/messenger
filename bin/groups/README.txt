Files
----------------------
Server.java
User.java

#Installation
javac Server.java
javac User.java
java Server <port #>
java User <host> <port #>

Architecture
-------------------
Closely resembles skeleton code

Friendship Tracking
Uses a HashMap to store friendship data: <User>,<type?
type=0 => friend request sent
type=1 => friends

The Client is designed to just send or receive some message based on input. No logic is done on the client side.
The Server tracks thread state and determines whether a certain action is allowed, and changes the server state when an allowed action is triggered.

Many use cases are handled and documented in the code. When an invalid command is used the server responds with the string of the error

Protocol
--------------------
To Server
----
*** Client automatically prepends #join to first message before sending
#status message			-Send status to all friends
@connect <user>			-If not currently friend send a friend request
@friend <user>			-If friend request available add friend
@deny <user>			-If friend request available reject request
@disconnect <user>		-If friends delete friendship
#Bye or Exit			-Close connection


Possible tradeoffs & extensions
---------------------------------------
-The Client must tradeoff in the protocol usage between ease of use and precision (writing #status <status> vs implying it and just letting <status>)
-The Server must choose who gets to see what updates. I decided to hide all private things similarly to real applications

There are many extensions for the messenger app.
Some that come to mind are:
-Add encryption (Diffie-Hellman into symmetric) to create secure communications
-Add a web UI to make it usable
-Port to AWS or some external host and give it a URL to make it exposed
-Allow for private messaging, to friends, and to all
-Add mini games for fun. The server handles all logic
