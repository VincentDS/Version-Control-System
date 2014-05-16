import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ServerRepository {
	
	public WorkingDirectory directory;
	File head;
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
		
		public String checkout() {
			return "";
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
