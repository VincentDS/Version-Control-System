import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class VcsClient {
	
	WorkingDirectory directory;
	String MainDirectory;
	public ClientRepository crepo;

	
	public VcsClient() throws IOException {
		directory = new WorkingDirectory("/Users/vincentdeschutter/Documents/Test/Client");
		MainDirectory = directory.getWorkingDir();
	}

private static final String PROMPT = "> ";

	public void connectToServer(InetAddress ip, int port)
		        throws UnknownHostException, IOException {
		
		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);		
		Socket socket = new Socket();
		socket.connect(serverAddress);
		
		try {
			// get raw input and output streams
			InputStream rawInput = socket.getInputStream();
			OutputStream rawOutput = socket.getOutputStream();
			
			// wrap streams in Readers and Writers to read and write
			// text Strings rather than individual bytes 
			BufferedReader input = new BufferedReader(
					new InputStreamReader(rawInput));
			PrintWriter output = new PrintWriter(rawOutput);
			
			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));
			
			 //Display the welcome-text
			System.out.println("Welcome to the Version Control System server!");
			String message;
			do {
				
				System.out.print(PROMPT);
				message = consoleInput.readLine();

				// send message to the server
				output.println(message);
				
				// make sure the output is sent to the server before waiting
				// for a reply
				output.flush();
								
				// wait for and read the reply of the server
				String serverReply = input.readLine();
				
				String clientanswer = processHeader(serverReply);
				
				// log the string on the local console
				System.out.println(clientanswer);
				
			} while (!message.equals(""));

		} finally {
			// tear down communication
			socket.close();
		}
	}
	
	/*
	 * When the input, that comes from the server begins with:
	 *  0 - The client doensn't need the process the message,
	 *  1 - The client needs to process the message
	 *
	 */
	private String processHeader(String input) throws IOException {
		String answer = "";
		if (input.startsWith("0")) {
			answer = input.substring(2);
		} 
		else if (input.startsWith("1")) {
			answer = processServerInput(input.substring(2));
		}
		return answer;
	}
	
	private String processServerInput(String input) throws IOException {
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
		}
		else if (input.equals("pwd")) {
			answer = directory.getWorkingDir();
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
				answer = processVcs(input.substring(4));
			} else {
			answer = "Please give a valid vcs command";
			}
		}
		return answer;
	}
	
	public String processVcs(String command) throws IOException {
		String answer = "";
		if (command.equals("checkout")) {
			if (crepo == null) {
				//HEAD ontvangen van de server
				CommitObject HEAD = null;
				crepo = new ClientRepository(directory, HEAD);
				answer = "You now have a workingcopy of the repository";
			}
			else {
				answer = "There is already a workingcopy of the repository in this folder, you can just update your workingcopy";
			}
		}
		else if (command.startsWith("add")) {
			String filename = command.substring(4);
			if (crepo != null) {
				answer = crepo.add(filename);
			}
			else {
				answer = "Please checkout first";
			}
		}
		else if (command.startsWith("commit")) {
			String message = "";
			if (command.length() > 6) {
				if (command.substring(7,9).equals("-m")) {
					message = command.substring(10);
				}
			}
			if (crepo != null) {
				answer = crepo.commit(message);
			}
			else {
			answer = "Please create a repository first.";
			}
		}
		else if (command.equals("diff")) {
			if (crepo != null) {
				answer = crepo.checkout();
			}
			else {
			answer = "Please create a repository first.";
			}
		}
		
		return answer;
	}


	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: java EchoClient ip port");
			return;
		}
		
		InetAddress ip = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		
		System.out.println("Client: connecting to server at "+ip+":"+port);
		
		VcsClient client = new VcsClient();
		client.connectToServer(ip, port);
		
		System.out.println("Client: terminating");
	}

}
