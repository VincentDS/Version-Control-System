import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ClientRepository implements VcsProtocol {

	public WorkingDirectory directory;
	public List<String> index;
	public CommitObject HEAD;
		
	public String ProjectDirectory;
	public String VcsDirectory;
	
	public ClientRepository(WorkingDirectory directory, CommitObject HEAD) throws IOException {
		ProjectDirectory = directory.getWorkingDir();
		VcsDirectory = ProjectDirectory + File.separator + ".vcs";
		this.directory = directory;
		this.HEAD = HEAD;
		index = new ArrayList<String>();
		if (!directory.exists(".vcs")) {
			this.directory.createDir(".vcs"); //.vcs directory aanmaken
			this.directory.setWorkingDir(VcsDirectory);
			this.directory.createFile("index"); //de index file aanmaken
			this.directory.createFile("head");
			putMeta("index");
			putMeta("head");
			this.directory.setWorkingDir(ProjectDirectory);
		}
		else {
			this.directory.setWorkingDir(ProjectDirectory);
			getMeta("index");
			getMeta("head");
		}
	}
	
	
	public String add(String filename) throws IOException {
		String answer = "";
		this.directory.setWorkingDir(ProjectDirectory);
		if (directory.exists(filename)) {
			if (!index.contains(filename)) {
				index.add(filename);
				System.out.println(index.get(0));
				//sorting the list, so we get the same hash value, no matter in which order we add the different files.
				Collections.sort(index);
				System.out.println(index.get(0));

				putMeta("index");
				answer = "this file is added to the staging area";
			}
			else {
				answer = "this file is already in the staging area";
			}
		}
		else {
			answer = "this file doensn't exsists";
		}
		return answer;
	}

	
	public String checkout() {
		String answer = "";
		
		
		answer = "0 Can't checkout";
		
		answer = "1 vcs checkout";
		
		return answer;
	}

	
	public String commit(String message) throws IOException {
		CommitObject co = new CommitObject(this, HEAD, message);
		HEAD = co;
		//index file ledigen
		index.clear();
		putMeta("index");
		return "The files in the staging area were commited";
	}

	
	public String diff() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String update() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String status() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void putMeta(String object) throws IOException {
		this.directory.setWorkingDir(VcsDirectory);
		String path = directory.getWorkingDir() + File.separator + object;
		FileOutputStream fos = new FileOutputStream(path);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		if (object.equals("index")) {
			oos.writeObject(index);
		} else if (object.equals("head")) {
			oos.writeObject(HEAD);
		}
		oos.close();
		this.directory.setWorkingDir(ProjectDirectory);
	}
	
	@SuppressWarnings("unchecked")
	public void getMeta(String object) throws IOException {
		this.directory.setWorkingDir(VcsDirectory);
		String path = directory.getWorkingDir() + File.separator + object;
		FileInputStream fis = new FileInputStream(path);
		System.out.println(path);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {
			if (object.equals("index")) {
				index = (List<String>)ois.readObject();
			}
			else if (object.equals("head"))  {
				HEAD = (CommitObject)ois.readObject();
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		ois.close();	
		this.directory.setWorkingDir(ProjectDirectory);
	}


}
