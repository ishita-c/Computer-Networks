import java.io.*; 
import java.net.*; 
import java.lang.String;
import java.util.Hashtable;
import java.util.Set;

class ServerRecv implements Runnable {
	
	String outputSentence, username;
	Socket connect;
	DataOutputStream clientOut;
	BufferedReader clientIn;
	
	ServerRecv (String outputSentence, String username, Socket connect, DataOutputStream clientOut, BufferedReader clientIn){
		this.outputSentence = outputSentence;
		this.username=username;
		this.connect = connect;
		this.clientIn = clientIn;
		this.clientOut = clientOut;
	} 

	public void run(){
		while(true) { 
			try {
				if(outputSentence.length()>=17){
					if(outputSentence.equals("ERROR 100 Malformed Username\n") || outputSentence.substring(0,17).equals("REGISTERED TORECV")){
						clientOut.writeBytes(outputSentence+'\n');
						break;
					}
				}
			}catch(Exception e1) {
				try {
					System.out.println("Error: Connection Disrupted");
					connect.close();
					break;
				}catch(Exception e2){break;}
			}
		} 
	}
}


class ServerSend implements Runnable {
	
	String outputSentence, sentUsername;
	Socket connect;
	BufferedReader clientIn;
	DataOutputStream clientOut;
	int first_time;

	ServerSend (String outputSentence,String sentUsername, Socket connect, BufferedReader clientIn, DataOutputStream clientOut){
		this.outputSentence=outputSentence;
		this.sentUsername=sentUsername;
		this.first_time = 0;
		this.connect = connect;
		this.clientIn = clientIn;
		this.clientOut = clientOut;
	} 

	String username, message;
	public void run() {
		while(true) { 
			int length =0;
			try {
				if(first_time==0){
					if(outputSentence.length()>=17){
						if ( outputSentence.substring(0,17).equals("REGISTERED TOSEND")||outputSentence.equals("ERROR 100 Malformed Username\n")){
							clientOut.writeBytes(outputSentence+'\n');
							if(outputSentence.equals("ERROR 100 Malformed Username\n")){
								break;
							}
							if(outputSentence.substring(0,17).equals("REGISTERED TOSEND")){
								first_time = 1;
								continue;
							}
						}
					}
				}
				message = clientIn.readLine();
				if (message.equals("")) message=clientIn.readLine();
				if((message.substring(0, 4)).equals("SEND")){
					username = message.substring(5);
					message = clientIn.readLine(); 

					if(message.substring(0, 16).equals("Content-length: ")){
						length = Integer.parseInt(message.substring(16));
						message = clientIn.readLine();

						if(message.equals("")){
							message = clientIn.readLine(); 

							if(message.length()==length){
								if(!(MainServer.name_table.containsKey(username))){
									clientOut.writeBytes("Username "+username+" Not Registered"+'\n'+'\n');
									continue;
								}

								Socket socket_username = MainServer.name_table.get(username);
								BufferedReader recvClientIn = new BufferedReader(new InputStreamReader(socket_username.getInputStream()));
								DataOutputStream recvClientOut = new DataOutputStream(socket_username.getOutputStream());
								recvClientOut.writeBytes("FORWARD "+ sentUsername+'\n'+"Content-length: "+ length+'\n'+"\n"+message+'\n');
								message=recvClientIn.readLine();
								
								while(true){
									if(message.equals("RECEIVED "+ sentUsername)){
										break;
									}
								}
								clientOut.writeBytes("SENT "+username+'\n'+'\n');
							}
						}
					}
				}
			}catch(Exception e1){
				System.out.println("Error: Connection Disrupted");
				try{
					connect.close();
					break;
				}catch(Exception e2){break;}
			}
		} 
	}
}

class MainServer{ 

	public static String register_username(BufferedReader clientIn, String input){
		String string="";
		try{
			string = clientIn.readLine(); 
		}catch(Exception e){
			System.out.println("Error: Connection Disrupted");
		}
		if((string.substring(0, 15).equals("REGISTER TOSEND") && input.equals("send")) || (string.substring(0, 15).equals("REGISTER TORECV") && input.equals("recv")) ){
			return string.substring(16);
		}
		else return "";
	}
	public static Boolean username_ok(String username){
		return (username.matches("^[a-zA-Z0-9]+$"));
	}
	public static Hashtable<String, Socket> name_table = new Hashtable<String, Socket>(); 
	public static void printAllUsernames(){
		Set<String> keys = name_table.keySet();
		for(String key: keys){
			System.out.println(key);
		}
		System.out.println("---------------------------------------");
	}

	public static void main(String argv[]) throws Exception { 
    	ServerSocket openSocket = new ServerSocket(3000);


      	while(true) { 
			String messagesend = "";

      		Socket sendSocket = openSocket.accept(); 
			DataOutputStream sendClientOut = new DataOutputStream(sendSocket.getOutputStream()); 
			BufferedReader sendClientIn = new BufferedReader(new InputStreamReader(sendSocket.getInputStream())); 
			String sentUsername = register_username(sendClientIn,"send");

			if(!(MainServer.name_table.containsKey(sentUsername))){
				if(username_ok(sentUsername)){
					messagesend = "REGISTERED TOSEND " + sentUsername + '\n';
				}
			}else{
				messagesend = "ERROR 100 Malformed Username" + '\n';
			}

			ServerSend  sendThread = new ServerSend(messagesend, sentUsername, sendSocket, sendClientIn, sendClientOut);
			Thread thread_send = new Thread(sendThread);
			thread_send.start();

			if(messagesend.equals("ERROR 100 Malformed Username\n")) continue;

			Socket recvSocket = openSocket.accept(); 
			DataOutputStream recvClientOut = new DataOutputStream(recvSocket.getOutputStream());
			BufferedReader recvClientIn = new BufferedReader(new InputStreamReader(recvSocket.getInputStream()));  
			String recvUsername = register_username(recvClientIn, "recv");
			String msgrecv = "";

			if(!(MainServer.name_table.containsKey(recvUsername))){
				if(username_ok(recvUsername)){
					msgrecv = "REGISTERED TORECV " + recvUsername + '\n';
				}
			}else{
				msgrecv = "ERROR 100 Malformed Username\n";
			}
			ServerRecv recvThread = new ServerRecv(msgrecv, recvUsername, recvSocket, recvClientOut, recvClientIn);
			Thread thread_recv = new Thread(recvThread);
			thread_recv.start();

			name_table.put(recvUsername, recvSocket);
			printAllUsernames();
		}
    } 
} 
