import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;

public class VcsClient {
	
	public WorkingDirectory directory;
	public String MainDirectory;
	public ClientRepository crepo;
	public String author;
	private static final String PROMPT = "> ";
	boolean connectedToServer = false;
	
	private BufferedReader consoleInput;
	
	
	private InetSocketAddress serverAddress;		
	private Socket socket;
	private ObjectInputStream serverObjectInput;
	private ObjectOutputStream serverObjectOutput;

	
	public VcsClient() throws IOException {
		directory = new WorkingDirectory("/Users/vincentdeschutter/Documents/Test/Client1");
		MainDirectory = directory.getWorkingDir();
	}
	
	
	public void run() throws IOException {
		consoleInput = new BufferedReader(new InputStreamReader(System.in));

		String message;
		String answer;
		while (true) {
			System.out.print(PROMPT);
			message = consoleInput.readLine();
			answer = processClientInput(message);
			if (answer != "") {
				System.out.println(answer);
			}
		}	
	}
	
	private String processClientInput(String input) throws IOException  {
		String answer = "";	
		if (input.equals("ls")) {
			if (directory.listFiles().length != 0) {
				answer = directory.listFiles()[0];
				for(int i = 1; i < directory.listFiles().length; i++) {
					answer += "   " + directory.listFiles()[i];
				}
			}
		}
		else if (input.startsWith("cd")) {
			String todirectory;
			if (input.length() >= 3) {
				todirectory = input.substring(3);
				directory.changeWorkingDir(todirectory);
			}
			else {
				directory.setWorkingDir("/Users/");
			}
			crepo = null;
		}
		else if (input.equals("pwd")) {
			answer = directory.getWorkingDir();
		}
		else if (input.startsWith("ssh")) {
			if (!connectedToServer) {
				try {
					String connection = input.substring(4);
					String[] tokens = connection.split(" ");
					if(tokens.length!=2) {
						throw new IllegalArgumentException();
					}
					InetAddress ip = InetAddress.getByName(tokens[0]);
					int port = Integer.parseInt(tokens[1]);
					System.out.println("connecting to server at "+ip+":"+port+"...");
					this.connectToServer(ip, port);
				} catch (Exception e) {
					answer = "The connection with the server could not be established. Please check if the server is running and you gave the correct address/ip";
				}
			} else {
				answer = "You are already connected to the server";
			}
		}
		else if (input.equals("terminate")) {
			if (connectedToServer) {
				socket.close();
				connectedToServer = false;
				answer = "You are now disconnected from the server";
			} else {
				answer = "You aren't connected to the server";
			}
		}
		else if (input.startsWith("vcs")) {
			if (input.length() >= 4) {
				if ((crepo == null) && (directory.exists(".vcs"))) {
					CommitObject HEAD = null;
					String path = directory.getWorkingDir() + File.separator + ".vcs" + File.separator + "head";
					FileInputStream fis = new FileInputStream(path);
					ObjectInputStream ois = new ObjectInputStream(fis);
					try {
						HEAD = (CommitObject)ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ois.close();	
					crepo = new ClientRepository(directory, HEAD);
				}
				answer = processClientVcs(input.substring(4));
			} else {
			answer = "Please give a valid vcs command";
			}
		}
		return answer;
	}
	
	private String processClientVcs(String command) throws IOException {
		String answer = "";
		if (command.equals("checkout")) {
			if (connectedToServer) {
				if (crepo == null) {
					CommitObject HEAD = null;
					crepo = new ClientRepository(directory, HEAD);
					answer = crepo.update(serverObjectOutput, serverObjectInput, consoleInput, true);
				}
				else {
					answer = "There is already a workingcopy of the repository in this folder, you can just update your workingcopy";
				}
			} else {
				answer = "Please connect to the server";
			}
		}
		else if (command.equals("update")) {
			if (connectedToServer) {
				if (crepo != null) {
					answer = crepo.update(serverObjectOutput, serverObjectInput, consoleInput, false);
				}
				else {
					answer = "There is no workingcopy found in this folder, please checkout first";
				}
			} else {
				answer = "Please connect to the server";
			}
		}
		else if (command.equals("init")) {
			if (connectedToServer) {
				answer = sendToServer("init");
			} else {
				answer = "Please connect to the server";
			}
		}
		else if (command.startsWith("add")) {
			String filename = command.substring(4);
			if (crepo != null) {
				answer = crepo.add(filename);
			}
			else {
				answer = "No repository found, please checkout first";
			}
		}
		else if (command.startsWith("remove")) {
			String filename = command.substring(7);
			if (crepo != null) {
				answer = crepo.remove(filename);
			}
			else {
				answer = "No repository found, please checkout first";
			}
		} 
		else if (command.startsWith("commit")) {
			String message = "";
			if (connectedToServer) {
				if (crepo != null) {
					if (command.length() > 6) {
						if (command.substring(7,9).equals("-m")) {
							message = command.substring(10);
							answer = crepo.commit(serverObjectOutput, serverObjectInput, message, author);
						}
					}
				}
				else {
					answer = "No repository found, please checkout first";
				}
			}
			else {
				answer = "Please connect to the server";
			}
		}
		else if (command.equals("status")) {
			if (crepo != null) {
				answer = crepo.status();
			}
			else {
			answer = "No repository found, please checkout first";
			}
		}
		else if (command.equals("diff")) {
			if (crepo != null) {
				answer = crepo.diff();
			}
			else {
			answer = "No repository found, please checkout first";
			}
		}
		
		return answer;
	}
	
	
	public void connectToServer(InetAddress ip, int port) throws UnknownHostException, IOException {
		
		serverAddress = new InetSocketAddress(ip, port);		 
		socket = new Socket();
		socket.connect(serverAddress);
		
		try {
			// get raw input and output streams
			InputStream rawInput = socket.getInputStream();
			OutputStream rawOutput = socket.getOutputStream();
			
			// wrap streams in Readers and Writers to read and write
			// text Strings rather than individual bytes  
			serverObjectOutput = new ObjectOutputStream(rawOutput);
			serverObjectInput = new ObjectInputStream(rawInput);
			
			PublicKey publicKey = RSA.generateKeys(directory.getWorkingDir());
			author = serverObjectInput.readUTF();
			serverObjectOutput.writeObject(publicKey);
			serverObjectOutput.flush();
			RSA.receivePublicKey(directory.getWorkingDir(), serverObjectInput);

			
			 //Display the welcome-text
			System.out.println("You are now connected with the Version Control System server as " + author + "!");
			connectedToServer = true;

		} catch (Exception e) {
			e.printStackTrace();
			socket.close();
		}
	}
	
	public String sendToServer(String message) throws IOException {
		serverObjectOutput.writeUTF(message);
		serverObjectOutput.flush();
		String serverReply = null;
		try {
			serverReply = (String) serverObjectInput.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return serverReply;
	}

	public static void main(String[] args) throws IOException {
		VcsClient client = new VcsClient();
		client.run();
	}

}
