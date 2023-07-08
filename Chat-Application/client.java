import java.io.*; 
import java.net.*; 

class ClientSend implements Runnable{
	Socket sendSocket;
	BufferedReader sendUser;
	DataOutputStream sendServerOut;
 	BufferedReader sendServerIn;
	ClientSend (Socket sendSocket, BufferedReader sendUser, DataOutputStream sendServerOut, BufferedReader sendServerIn){
		this.sendSocket = sendSocket;
    	this.sendUser = sendUser;
    	this.sendServerOut = sendServerOut;
		this.sendServerIn = sendServerIn;
    } 
	public void run(){
		while(true){
			String msg, recipient, send_line;
			int count, msgLength;
			try{
				recipient="";
				String read_line = sendUser.readLine(); //@[recipient username] [message] SEND 
				if(read_line.charAt(0)!='@'){
					System.out.println("Input in @[recipient username] Format");
					continue;				
				}
			
				count = 1;
				while(count<read_line.length() && read_line.charAt(count)!=' '){
					recipient+=read_line.charAt(count);	
					count++;
				}
				if(count==read_line.length()){
					System.out.println("Input in @[recipient username] [message] Format");
					continue;
				}
				msg=read_line.substring(count+1);
				msgLength=read_line.length()-count-1;
				try{
					sendServerOut.writeBytes("SEND "+recipient+"\n"+ "Content-length: "+ msgLength + "\n"+'\n'+msg+'\n');
				}catch(Exception e1){
					System.out.println("Please Connect To A Server");
					sendSocket.close();
					break;
				}
				
				send_line=sendServerIn.readLine();
				if(send_line.equals("SENT "+recipient)){
					if((sendServerIn.readLine()).equals("") )
						System.out.println("Message Delivered");
				}
				else{
					if((sendServerIn.readLine()).equals("") )
						System.out.println(send_line);
						System.out.println("Retry!");
				}
			}catch(Exception e1){
				try{
					System.out.println("Error: Connection Disrupted");
					sendSocket.close();
					break;
				}catch(Exception e2) { break; }
			}
		}
	}
}

class ClientRecv implements Runnable{
	Socket socketRecv;
	BufferedReader fromUser, fromServer;
	DataOutputStream toServer;
	ClientRecv(DataOutputStream toServer, BufferedReader fromServer, Socket socketRecv, BufferedReader fromUser){
		this.toServer = toServer;
		this.fromServer = fromServer;
		this.socketRecv = socketRecv;
        this.fromUser = fromUser;
    } 
	 
	public void run(){
		while(true){
			String recvMsg, send_format, message, recv_line;
			int msgLength;
			try{
				try{
					recvMsg=fromServer.readLine();
					if(recvMsg.equals("")) recvMsg=fromServer.readLine();
				}catch(Exception e1) { 
					System.out.println("Server Port Not Found");
					socketRecv.close();
					break;
				}

				if((recvMsg.substring(0,8)).equals("FORWARD ")){
					send_format=recvMsg.substring(8);
				}
				else{
					toServer.writeBytes("ERROR 103 Header Incomplete\n"+'\n');
					continue;
				}
				recvMsg=fromServer.readLine();
				
				if((recvMsg.substring(0,16)).equals("Content-length: ")){
					msgLength=Integer.parseInt(recvMsg.substring(16));
				}
				else{
					toServer.writeBytes("ERROR 103 Header Incomplete\n"+'\n');
					continue;
				}

				if(fromServer.readLine().equals("")){
					message=(fromServer.readLine()).substring(0,msgLength);
				}
				else{
					toServer.writeBytes("ERROR 103 Header Incomplete\n"+'\n');
					continue;
				}
				toServer.writeBytes("RECEIVED "+ send_format +'\n'+'\n');
				System.out.println(send_format+":"+message);

			}catch(Exception e1){
				try {
					System.out.println("Error: Connection Disrupted");
					socketRecv.close();
				} catch(Exception e2) { System.out.println("Error: Connection Disrupted");}
				break;
			}
		}
	}
}

class MainClient { 
    public static void main(String argv[]) throws Exception 
    {
		Socket sendSocket;
        String username, host;
        BufferedReader sendUser, sendServerIn, inFromUser;
		DataOutputStream sendServerOut;
		boolean first=true;
		host="";
		
		while(true){
			try{
				System.out.print("Username: ");
				inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
				username=inFromUser.readLine();
				if(first==true){
					System.out.print("IP Address of Host: ");
					inFromUser = new BufferedReader(new InputStreamReader(System.in));
					host=inFromUser.readLine();
				}

				try{
					sendSocket = new Socket(host, 3000);
				}catch(Exception e1){
					System.out.println("Unable to Find Host"+ host); 
					return;
				}

				sendUser = new BufferedReader(new InputStreamReader(System.in));
				sendServerIn = new BufferedReader(new InputStreamReader(sendSocket.getInputStream()));
				sendServerOut = new DataOutputStream(sendSocket.getOutputStream());
				
				sendServerOut.writeBytes("REGISTER TOSEND "+username+"\n" + '\n');

				String read_line_3 = sendServerIn.readLine();
				if((read_line_3).equals("REGISTERED TOSEND "+username)){
					String read_line_1 = sendServerIn.readLine();
					break;
				}else{
					System.out.println("Retry with Another Username!");
				}
				first=false;

			}catch(Exception e2){
				System.out.println("Unable to Register Username at Server");
				return;
			}
		}

		Socket socketRecv = new Socket(host, 3000);
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(socketRecv.getInputStream()));	
		BufferedReader fromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream toServer = new DataOutputStream(socketRecv.getOutputStream());	
		
		while(true){
			try{
				toServer.writeBytes("REGISTER TORECV "+username+"\n" + '\n');

				String read_line_4 = fromServer.readLine();
				if((read_line_4).equals("REGISTERED TORECV "+username)){
					String read_line_2 = fromServer.readLine();
					break;
				}
			}catch(Exception e3){
				System.out.println("Unable to Register Username at Server");
				return;
			}
		}

		ClientSend socket_s=new ClientSend(sendSocket, sendUser, sendServerOut, sendServerIn);
		Thread threadSend = new Thread(socket_s);
		threadSend.start();

		ClientRecv socket_r = new ClientRecv(toServer, fromServer, socketRecv, fromUser);
		Thread threadRecv = new Thread(socket_r);
		threadRecv.start();
			
	} 
} 

