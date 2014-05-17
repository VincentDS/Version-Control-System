import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

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

	public void acceptClient(int clientNumber) throws IOException {
		Socket clientSocket = serverSocket.accept(); // blocks
		ServerSession session = new ServerSession(this, clientSocket, clientNumber);
		System.out.println("Server: client"+ clientNumber +" connected");
		session.start(); // starts a new thread
		// return immediately
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java EchoServer port");
			return;
		}
		int port = Integer.parseInt(args[0]);
		int numberOfClients = 0;

		System.out.println("Server: waiting for clients on port "+port);
		VcsServer server = new VcsServer(port);

		
		while (true) {
			numberOfClients++;
			server.acceptClient(numberOfClients);
		}
	}


}
