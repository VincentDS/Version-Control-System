import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class VcsServer {

	public static WorkingDirectory directory;
	public static ServerRepository srepo;
	private final ServerSocket serverSocket;
	private static int numberOfClients = 0;


	//constructor of the server
	public VcsServer(int port) throws IOException {
		directory = new WorkingDirectory("/Users/vincentdeschutter/Documents/Test/Server");

		InetSocketAddress serverAddress = new InetSocketAddress(port);
		this.serverSocket = new ServerSocket();
		serverSocket.bind(serverAddress);

	}
	
	/*
	 * Constructor of the server repository. Only 1 client can make the repository at the same time
	 */
	public synchronized static void init() throws IOException {
		srepo = new ServerRepository(directory);
	}

	public void acceptClient(int clientNumber) throws IOException {
		Socket clientSocket = serverSocket.accept(); // blocks
		ServerSession session = new ServerSession(this, clientSocket, clientNumber);
		System.out.println("Server: client"+ clientNumber +" connected");
		session.start(); // starts a new thread
		// return immediately
	}
	
	public synchronized static void incrementClients() {
		numberOfClients++;
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java EchoServer port");
			return;
		}
		int port = Integer.parseInt(args[0]);

		VcsServer server = new VcsServer(port);
		System.out.println("Server: waiting for clients on port "+port);
		
		while (true) {
			incrementClients();
			server.acceptClient(numberOfClients);
		}
	}


}
