import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ServerRepository implements VcsProtocol {
	
	public WorkingDirectory directory;
	public CommitObject HEAD;
		
	public String ProjectDirectory;
	public String CommitObjectDirectory;
		
	//De repository .vcs wordt gecreerd in de directory van het project (= init)
	public ServerRepository(WorkingDirectory directory) throws IOException {
			ProjectDirectory = directory.getWorkingDir();
			CommitObjectDirectory = ProjectDirectory + File.separator + "commitobjects";
			this.directory = directory;
			if (!directory.exists("commitobjects")) {
				this.directory.createDir("commitobjects"); //commitobjects directory aanmaken
				this.directory.createFile("head"); //de head file aanmaken
				putHead();
			}
			else {
				getHead();
			}
		}
		
		public String add(String filename) throws IOException {
			return "1 vcs add " + filename;
		}
		
		public String checkout() {
			//HEAD commit doorsturen naar de client (als deze niet null is = lege repository)
			return "1 vcs checkout";			
		}
		
		
		public String commit(String message) throws IOException {
			return "";
		}
		

		public String diff() {
			// TODO Auto-generated method stub
			return "";
		}
		
		public String update() {
			// TODO Auto-generated method stub
			return "";
		}
		
		public String status() {
			// TODO Auto-generated method stub
			return "";
		}
		

		public void putHead() throws IOException {
			String path = directory.getWorkingDir() + File.separator + "head";
			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(HEAD);
			oos.close();
		}
		
		@SuppressWarnings("unchecked")
		public void getHead() throws IOException {
			String path = directory.getWorkingDir() + File.separator + "head";
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				HEAD = (CommitObject)ois.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			ois.close();	
		}

}
