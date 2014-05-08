import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class VcsServer {
	
	WorkingDirectory directory;
	String MainDirectory;
	private final ServerSocket serverSocket;

	//constructor of the server
	public VcsServer(int port) throws IOException {
		directory = new WorkingDirectory("/Users/vincentdeschutter/Documents/Test/Server");
		MainDirectory = directory.getWorkingDir();
		
		InetSocketAddress serverAddress = new InetSocketAddress(port);
		this.serverSocket = new ServerSocket();
		serverSocket.bind(serverAddress);
		
	}
	
	public void acceptClient() throws IOException {
		Socket clientSocket = serverSocket.accept(); // blocks
		Session session = new Session(this, clientSocket);
		System.out.println("Server: client connected");
		session.start(); // starts a new thread
		// return immediately
	}

	
private static class Session extends Thread {
		
		public VcsServer server;
		public WorkingDirectory directory;
		public String MainDirectory;
		public ServerRepository srepo;

		private final Socket clientSocket;
		
		public Session(VcsServer server, Socket clientSocket) throws IOException {
			this.server = server;
			directory = server.directory;
			MainDirectory = server.directory.getWorkingDir();
			this.clientSocket = clientSocket;
		}
		
		public void run() {
			try {
				// get raw input and output streams
				InputStream rawInput = this.clientSocket.getInputStream();
				OutputStream rawOutput = this.clientSocket.getOutputStream();
				
				// wrap streams in Readers and Writers to read and write
				// text Strings rather than individual bytes 
				BufferedReader input = new BufferedReader(
						new InputStreamReader(rawInput));
				PrintWriter output = new PrintWriter(rawOutput);
				
				while (true) {
					// read string from client
					String clientInput = input.readLine();
					
					if (clientInput != null) {
						// log the string on the local console
						System.out.println("Server: client sent '" +
								clientInput + "'");
					
						// send back the string to the client
						String serveranswer = processClientInput(clientInput);
						output.println(serveranswer);
						
						// make sure the output is sent to the client before closing
						// the stream
						output.flush();		
					}
				}
			
			} catch (IOException e) {
				
			} finally {
				// tear down communication
				System.err.println("Server: closing client connection");
				try {
					this.clientSocket.close();
				} catch (IOException e) { /* ignore */ }
			}
		}
		
		public String processClientInput(String input) throws IOException {
			String answer = "0";
			//sending back the message because we can only navigate on the client
			if (input.startsWith("ls") || input.startsWith("cd") || input.startsWith("pwd")) {
				answer = "1 " + input;
			}
			else if (input.startsWith("vcs")) {
				if (input.length() >= 4) {
					if ((srepo == null) && (directory.exists("commitobjects"))) {
						srepo = new ServerRepository(directory);
					}
					answer = processVcs(input.substring(4));
				} else {
				answer = "0 Please give a valid vcs command";
				}
			}
			return answer;
		}
		
		public String processVcs(String command) throws IOException {
			String answer = "0";
			if (command.equals("init")) {
				if (srepo == null) {
					srepo = new ServerRepository(directory);
					answer = "0 You initialized a new repository";
				}
				else {
					answer = "0 There exist already a repository in this project";
				}
			}
			else if (command.startsWith("add")) {
				String filename = command.substring(4);
				if (srepo != null) {
					answer = srepo.add(filename);
				}
				else {
					answer = "0 Please create a repository first";
				}
			}
			else if (command.startsWith("commit")) {
				String message = "";
				if (command.length() > 6) {
					if (command.substring(7,9).equals("-m")) {
						message = command.substring(10);
					}
				}
				if (srepo != null) {
					answer = srepo.commit(message);
				}
				else {
				answer = "0 Please create a repository first";
				}
			}
			else if (command.equals("checkout")) {
				if (srepo != null) {
					answer = srepo.checkout();
				}
				else {
				answer = "0 Please create a repository first";
				}
			}
			return answer;
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java EchoServer port");
			return;
		}
		int port = Integer.parseInt(args[0]);
		
		System.out.println("Server: waiting for clients on port "+port);
		VcsServer server = new VcsServer(port);

		while (true) {
			server.acceptClient();
		}
	}


}
