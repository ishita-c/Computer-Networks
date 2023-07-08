JCC = javac
JFLAGS = -g

default: TCPClient.class TCPServer.class 


TCPClient.class: client.java
	$(JCC) $(JFLAGS) client.java

TCPServer.class: server.java
	$(JCC) $(JFLAGS) server.java


clean: 
	$(RM) *.class

