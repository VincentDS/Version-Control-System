import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


/*
 * Een repository is een hidden map in de directory van een project. 
 * Deze map bevat:
 * 		- set van commit objects
 * 		- referentie naar een commit object (HEAD)
 * 		- index file met bestanden die in de staging area zitten
 * 		- adres van de repository op de server
 */
public class Repository {
	public WorkingDirectory directory;
	public List<String> index;
	public CommitObject HEAD;
	
	public String ProjectDirectory;
	public String VcsDirectory;
	public String CommitObjectDirectory;
	
	//De repository .vcs wordt gecreerd in de directory van het project (= init)
	public Repository(WorkingDirectory directory) throws IOException {
		ProjectDirectory = directory.getWorkingDir();
		VcsDirectory = ProjectDirectory + File.separator + ".vcs";
		CommitObjectDirectory = VcsDirectory + File.separator + "commitobjects";
		this.directory = directory;
		index = new ArrayList<String>();
		if (!directory.exists(".vcs")) {
			this.directory.createDir(".vcs"); //.vcs directory aanmaken
			this.directory.setWorkingDir(VcsDirectory);
			this.directory.createFile("index"); //de index file aanmaken
			this.directory.createDir("commitobjects");
			this.directory.setWorkingDir(ProjectDirectory);
		}
		else {
			this.directory.setWorkingDir(ProjectDirectory);
			getIndex();
		}
	}
	
	//files worden in de staging area gezet, klaar om te commiten
	public void add(String filename) throws IOException {
		this.directory.setWorkingDir(ProjectDirectory);
		if (directory.exists(filename)) {
			if (!index.contains(filename)) {
				index.add(filename);
				putIndex();
			}
			else {
				System.out.println("this file is already in the staging area");
			}
		}
		else {
			System.out.println("this file doensn't exsists");
		}
	}
	
	public void status() {
		
	}
	
	public void commit(String message) throws IOException {
		CommitObjectDirectory = VcsDirectory + File.separator + "commitobjects";
		directory.setWorkingDir(ProjectDirectory);
		CommitObject co = new CommitObject(this, HEAD, message);
		HEAD = co;
		//index file ledigen
		index.clear();
		putIndex();
	}
	
	public void diff () {
		
	}
	

	public void putIndex() throws IOException {
		this.directory.setWorkingDir(VcsDirectory);
		String path = directory.getWorkingDir() + File.separator + "index";
		FileOutputStream fos = new FileOutputStream(path);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(index);
		oos.close();
		this.directory.setWorkingDir(ProjectDirectory);
	}
	
	public void getIndex() throws IOException {
		this.directory.setWorkingDir(VcsDirectory);
		String path = directory.getWorkingDir() + File.separator + "index";
		FileInputStream fis = new FileInputStream(path);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {
			index = (List<String>)ois.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ois.close();	
		this.directory.setWorkingDir(ProjectDirectory);
	}
	
}
