To compile the code, run the folowing command on terminal (Ubuntu machine used, version 20.04). The server.java contains server application and client.java contains client aplication. To run the code, follow the instructions below:

$ make
(This will compile both the files, client.java and server.java using javac -g client.java and javac -g server.java respectively)

Run the server with the command:
$java MainServer

Now we can connect multiple clients to our server, each of the clients must be connected from a new terminal window. Run the command:
$java MainClient

Enter a valid (and non-registered) Username and IP Address for each client when asked. The IP Address 127.0.0.1 is used to connect to the localhost. The mesage should be entered in @[recipient name] [message] format, where recipient name represents the addressed name where message is supposed to be deliver.

Use $make clean to remove the generated class files after compilation.

All the instructions of assignment statement are followed, handling exceptions, single registration of each user, etc.
