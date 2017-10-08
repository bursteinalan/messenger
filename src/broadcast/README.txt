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

The Client is designed to just send or receive some message based on input. No logic is done on the client side.
The Server tracks thread state and determines whether a certain action is allowed, and changes the server state when an allowed action is triggered.

Protocol
--------------------
To Server
----
*** Client automatically prepends #join to first message before sending
#status message

