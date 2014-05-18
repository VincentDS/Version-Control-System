import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PublicKey;

public class ServerSession extends Thread {

		public WorkingDirectory directory;
		public String MainDirectory;
		public int clientNumber;

		private final Socket clientSocket;
		ObjectInputStream clientObjectInput;
		ObjectOutputStream clientObjectOutput;

		public ServerSession(VcsServer server, Socket clientSocket, int clientNumber) throws IOException {
			this.clientNumber = clientNumber;
			directory = VcsServer.directory;
			MainDirectory = VcsServer.directory.getWorkingDir();
			this.clientSocket = clientSocket;
		}

		public void run() {
			try {

				// get raw input and output streams
				InputStream rawInput = this.clientSocket.getInputStream();
				OutputStream rawOutput = this.clientSocket.getOutputStream();


				// wrap streams in Readers and Writers to read and write
				// text Strings rather than individual bytes 
				clientObjectOutput = new ObjectOutputStream(rawOutput);
				//clientObjectOutput.flush();
				clientObjectInput = new ObjectInputStream(rawInput);

				File keyDir = new File(MainDirectory + File.separator + ".client" + clientNumber);
				keyDir.mkdir();
				PublicKey publicKey = RSA.generateKeys(keyDir.getAbsolutePath());
				clientObjectOutput.writeUTF("client" + clientNumber);
				clientObjectOutput.flush();	
				RSA.receivePublicKey(keyDir.getAbsolutePath(), clientObjectInput);
				clientObjectOutput.writeObject(publicKey);
				clientObjectOutput.flush();
				
				
				while (true) {

					// read string from client
					String clientInput = null;
					clientInput = clientObjectInput.readUTF();

					if (clientInput != null) {

						// log the string on the local console
						System.out.println("Server: client" + clientNumber +" sent '" + clientInput + "'");

						// send back the string to the client
						String serveranswer = processClientInput(clientInput);

						clientObjectOutput.writeObject(serveranswer);

						// make sure the output is sent to the client before closing
						// the stream
						clientObjectOutput.flush();		
					}
				}

			} catch (IOException e) {

			} finally {
				// tear down communication
				System.err.println("Server: closing client" + clientNumber +" connection");
				try {
					this.clientSocket.close();
				} catch (IOException e) { /* ignore */ }
			}
		}


		public String processClientInput(String command) throws IOException {
			String answer = "";
			if ((VcsServer.srepo == null) && (new File(MainDirectory + File.separator + "head").exists())) {
				VcsServer.init();
			}				
			if (command.equals("init")) {
				if (VcsServer.srepo == null) {
					VcsServer.init();
					answer = "You initialized a new repository";
				}
				else {
					answer = "There exist already a repository in this project";
				}
			}
			else if (command.equals("checkout")) {
				if (VcsServer.srepo != null) {
					answer = VcsServer.srepo.update(clientObjectOutput, clientObjectInput, clientNumber, false);
				}
				else {
					answer = "Please create a repository first";
				}
			}
			else if (command.equals("update")) {
				if (VcsServer.srepo != null) {
					answer = VcsServer.srepo.update(clientObjectOutput, clientObjectInput, clientNumber, true);
				}
				else {
					answer = "Please create a repository first";
				}
			}
			else if (command.startsWith("commit")) {
				if (VcsServer.srepo != null) {				
					answer = VcsServer.srepo.commit(clientObjectOutput, clientObjectInput, clientNumber);
				}
				else {
					answer = "Please create a repository first";
				}
			}
			return answer;
		}
		
	}
