import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;


public class ServerRepository {
	
	public WorkingDirectory directory;
	public File head;
	public CommitObject HEAD;
	public String ProjectDirectory;
	
		
	//De repository .vcs wordt gecreerd in de directory van het project (= init)
	public ServerRepository(WorkingDirectory directory) throws IOException {
			ProjectDirectory = directory.getWorkingDir();
			this.directory = directory;
			head = new File(ProjectDirectory + File.separator + "head");
			if (!head.exists()) {
				this.directory.createFile("head"); //de head file aanmaken
				putHead();
			}
			else {
				getHead();
			}
		}
		
		/*
		 * Commit changes the head of the repository, so other clients are not allowed to read while someonelse is committing.
		 */
		public synchronized String commit(ObjectOutputStream output, ObjectInputStream input, int clientNumber) throws IOException {
			String answer = "";
			try {
				CommitObject oldhead = (CommitObject) input.readObject();

				//kijken of de vorige head op de clientside gelijk is aan de huidige head op de server
				// --> deze client is de eerste die iets aanpast aan de server
				if (VcsServer.srepo.HEAD == null || VcsServer.srepo.HEAD.equals(oldhead)) {
					output.writeObject("ok");
					output.flush();
					CommitObject newhead = (CommitObject) input.readObject();
					String ID = newhead.ID;
					VcsServer.srepo.HEAD = newhead;
					VcsServer.srepo.putHead();
					directory.createDir(ID);
					directory.changeWorkingDir(ProjectDirectory + File.separator + ID);
					VcsServer.srepo.putMetaFile(newhead);
					int numberOfFiles = (Integer) input.readObject();
					String keyPath = ProjectDirectory + File.separator + ".client" + clientNumber + File.separator + ".my_private.key";
					ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(keyPath));
				    final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
				    inputStream.close();
					for(int i=0; i<numberOfFiles; i++) {
						Utilities.receiveFile(input, ProjectDirectory + File.separator + ID, privateKey);
					}
					String clientStatus = (String) input.readObject();
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
			return answer;
		}
		
		
		public String update(ObjectOutputStream output, ObjectInputStream input, int clientNumber, boolean checkout) {
			String answer = "";
			List<String> files = null;
			try {
				output.writeObject("ok");
				output.flush();	
				output.writeObject(VcsServer.srepo.HEAD);
				output.flush();

				int numberOfFiles = 0;
				String ID = null;
				if (VcsServer.srepo.HEAD != null) {
					ID = VcsServer.srepo.HEAD.ID;
					files = VcsServer.srepo.HEAD.files;
					numberOfFiles = files.size();
				}
				output.writeObject(numberOfFiles);
				output.flush();
				String keyPath = ProjectDirectory + File.separator + ".client" + clientNumber + File.separator + ".received_public.key";
				ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(keyPath));
			    PublicKey publicKey = (PublicKey) inputStream.readObject();
				inputStream.close();
				for (int i = 0; i < numberOfFiles; i++) {
					Utilities.sendFile(ProjectDirectory + File.separator + ID + File.separator + files.get(i), output, publicKey);	
				}
				if (checkout) {
					answer = "You now have a workingcopy of the repository";
				}
				else {
					answer = "Your workingcopy is succesfully updated";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return answer;
		}


		public void putHead() throws IOException {
			FileOutputStream fos = new FileOutputStream(head);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(HEAD);
			oos.close();
		}
		
		public void getHead() throws IOException {
			FileInputStream fis = new FileInputStream(head);
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				HEAD = (CommitObject)ois.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			ois.close();	
		}
		
		public void putMetaFile(CommitObject co) {
			try {
				File file = new File(ProjectDirectory + File.separator + co.ID + File.separator + ".meta");
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("Author: " + co.author + "\n");
				bw.write("Message: " + co.message + "\n");
				bw.write("ID: " + co.ID + "\n");
				//parent is null when there is no previous commit. 
				if (co.parent != null) {
					bw.write("Parent: " + co.parent.ID + "\n");
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
}
