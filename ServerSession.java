import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ServerSession extends Thread {

		public VcsServer server;
		public WorkingDirectory directory;
		public String MainDirectory;
		public ServerRepository srepo;

		private final Socket clientSocket;
		ObjectInputStream clientInputStream;
		ObjectOutputStream clientOutputStream;

		public ServerSession(VcsServer server, Socket clientSocket) throws IOException {
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
				clientOutputStream = new ObjectOutputStream(rawOutput);
				//clientOutputStream.flush();
				clientInputStream = new ObjectInputStream(rawInput);

				
				while (true) {
					
					// read string from client
					String clientInput = null;
					clientInput = clientInputStream.readUTF();
					
					if (clientInput != null) {

						// log the string on the local console
						System.out.println("Server: client sent '" +
								clientInput + "'");

						// send back the string to the client
						String serveranswer = processClientInput(clientInput);

						clientOutputStream.writeObject(serveranswer);

						// make sure the output is sent to the client before closing
						// the stream
						clientOutputStream.flush();		
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


		public String processClientInput(String command) throws IOException {
			String answer = "";
			if ((srepo == null) && (new File(MainDirectory + File.separator + "head").exists())) {
				srepo = new ServerRepository(directory);
			}				
			if (command.equals("init")) {
				if (srepo == null) {
					srepo = new ServerRepository(directory);
					answer = "You initialized a new repository";
				}
				else {
					answer = "There exist already a repository in this project";
				}
			}
			else if (command.equals("checkout")) {
				if (srepo != null) {
					List<String> files = null;
					try {
						clientOutputStream.writeObject("ok");
						clientOutputStream.flush();	
						clientOutputStream.writeObject(srepo.HEAD);
						clientOutputStream.flush();
						
						int numberOfFiles = 0;
						String ID = null;
						if (srepo.HEAD != null) {
							System.out.println("testje1");
							ID = srepo.HEAD.ID;
							files = srepo.HEAD.files;
							numberOfFiles = files.size();
						}
						clientOutputStream.writeObject(numberOfFiles);
						clientOutputStream.flush();
				        for (int i = 0; i < numberOfFiles; i++) {
							System.out.println("testje2");
							Utilities.sendFile(MainDirectory + File.separator + ID + File.separator + files.get(i), clientOutputStream);	
				        }
				        answer = "The repository is succesfully sent";
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					answer = "Please create a repository first";
				}
			}
			else if (command.startsWith("commit")) {
				if (srepo != null) {				
					try {
						CommitObject oldhead = (CommitObject) clientInputStream.readObject();

						//kijken of de vorige head op de clientside gelijk is aan de huidige head op de server
						// --> deze client is de eerste die iets aanpast aan de server
						if (srepo.HEAD == null || srepo.HEAD.equals(oldhead)) {
							clientOutputStream.writeObject("ok");
							clientOutputStream.flush();
							CommitObject newhead = (CommitObject) clientInputStream.readObject();
							String ID = newhead.ID;
							srepo.HEAD = newhead;
							srepo.putHead();
							directory.createDir(ID);
							directory.changeWorkingDir(MainDirectory + File.separator + ID);
							srepo.putMetaFile(newhead);
							int numberOfFiles = (Integer) clientInputStream.readObject();
							for(int i=0; i<numberOfFiles; i++) {
								Utilities.receiveFile(clientInputStream, MainDirectory + File.separator + ID);
							}
							String clientStatus = (String) clientInputStream.readObject();
							if (clientStatus.equals("succes")) {
								answer = "The files are succesfully commited";
							} else {
								answer = "Something went wrong while transferring the files";
							}
							
						} else {
							answer = "You don't have the latest version, please update your working directory";
						}		
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					answer = "Please create a repository first";
				}
			}
			return answer;

		}
		
	}
