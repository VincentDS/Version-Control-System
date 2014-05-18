import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import difflib.DiffRow;
import difflib.DiffRowGenerator;


public class ClientRepository implements Serializable {
	
	private static final long serialVersionUID = -477189107700903771L;
	public WorkingDirectory directory;
	public List<String> index;
	public CommitObject HEAD;
		
	public String ProjectDirectory;
	public String VcsDirectory;
	public String HeadFilesDirectory;
	
	public ClientRepository(WorkingDirectory directory, CommitObject HEAD) throws IOException {
		ProjectDirectory = directory.getWorkingDir();
		VcsDirectory = ProjectDirectory + File.separator + ".vcs";
		HeadFilesDirectory = VcsDirectory + File.separator + "headfiles";
		this.directory = directory;
		this.HEAD = HEAD;
		index = new ArrayList<String>();
		if (!directory.exists(".vcs")) {
			this.directory.createDir(".vcs"); //.vcs directory aanmaken
			this.directory.setWorkingDir(VcsDirectory);
			this.directory.createFile("index"); //de index file aanmaken
			this.directory.createFile("head");
			this.directory.createDir("headfiles");
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
				//sorting the list, so we get the same hash value, no matter in which order we add the different files.
				Collections.sort(index);
				putMeta("index");
				answer = "this file is added to the staging area";
			}
			else {
				answer = "this file is already in the staging area";
			}
		}
		else {
			answer = "this file doensn't exists";
		}
		return answer;
	}
	
	public String remove(String filename) throws IOException {
		String answer = "";
		this.directory.setWorkingDir(ProjectDirectory);
		if (directory.exists(filename)) {
			if (index.contains(filename)) {
				index.remove(filename);
				//sorting the list, so we get the same hash value, no matter in which order we add the different files.
				Collections.sort(index);
				putMeta("index");
				answer = "this file is removed from the staging area";
			}
			else {
				answer = "this file is not in the staging area";
			}
		}
		else {
			answer = "this file doensn't exists";
		}
		return answer;
	} 

	
	public String update(ObjectOutputStream output, ObjectInputStream input, BufferedReader consoleInput, boolean checkout) throws IOException {
		String answer = "";
		if (checkout) {
			output.writeUTF("checkout");
		} else {
			output.writeUTF("update");
		}
		output.flush();	

		String serverReply = null;
		try {
			serverReply = (String) input.readObject();

			if (serverReply.equals("ok")) {
				CommitObject newhead = (CommitObject) input.readObject();
				HEAD = newhead;
				int numberOfFiles = (Integer) input.readObject();
				directory.setWorkingDir(HeadFilesDirectory);
				directory.cleanDir();
				String keyPath = ProjectDirectory + File.separator + ".my_private.key";
				ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(keyPath));
			    PrivateKey privateKey = (PrivateKey) inputStream.readObject();
			    inputStream.close();
				for(int i=0; i<numberOfFiles; i++) {
					Utilities.receiveFile(input, HeadFilesDirectory, privateKey);
				}
				transferHeadFiles(consoleInput);
				serverReply = (String) input.readObject();
				answer = serverReply;	
			}
			else {
				answer = serverReply;
			}
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer; 
	}
	
	public String commitLocal(String message, String author) throws IOException {
		CommitObject co = new CommitObject(this, HEAD, message, author);
		HEAD = co;
		putMeta("head");
		return "The files in the staging area were commited on clientside";
	}
	
	public String commit(ObjectOutputStream output, ObjectInputStream input, String message, String author) throws IOException {
		String answer = "";
		CommitObject oldhead = HEAD;
		List<String> files = index;

		if (!files.isEmpty()) {
			//commiten op client-side
			commitLocal(message, author);
			CommitObject newhead = HEAD;

			//commit doorsturen naar server
			output.writeUTF("commit");
			output.flush();	
			output.writeObject(oldhead);
			output.flush();	


			String serverReply = null;
			try {
				serverReply = (String) input.readObject();

				if (serverReply.equals("ok")) {
					output.writeObject(newhead);
					output.flush();
					output.writeObject(files.size());
					output.flush();
					String keyPath = ProjectDirectory + File.separator + ".received_public.key";
					ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(keyPath));
				    PublicKey publicKey = (PublicKey) inputStream.readObject();
				    inputStream.close();
					for(int i=0; i<files.size(); i++) {
						Utilities.sendFile(ProjectDirectory + File.separator + files.get(i), output, publicKey);
					}
					output.writeObject("succes");
					output.flush();	
					putHeadFiles();
					emptyIndex();
					serverReply = (String) input.readObject();
					answer = serverReply;
				} else {
					answer = serverReply;
				} 
			}
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			answer = "There are no files in the staging area, please add one or more files before you commit";
		}

		return answer;
	}

	
	public String diff() throws IOException {
		String answer = "";

		answer += "    Difference between the current Project-Directory and the latest version (ID: " + HEAD.ID +") \n \n";

		File directory = new File(ProjectDirectory);
		File[] fileList = directory.listFiles();
		if (fileList.length-index.size() != 2) {
			for (File projectfile : fileList) {
				String projectfileName = projectfile.getName();
				String extension = projectfileName.substring(projectfileName.lastIndexOf(".") + 1, projectfileName.length());
				if (projectfile.isFile() && (!projectfileName.equals(".DS_Store")) && (!extension.equals("key"))) {
					answer += "\t" + projectfile.getName() + ": \n";
					File versionfile = new File(HeadFilesDirectory + File.separator + projectfile.getName());

					if (versionfile.isFile()) {

						SHA1 projectfileHash = new SHA1(Utilities.FileToByteArray(projectfile));
						SHA1 versionfileHash = new SHA1(Utilities.FileToByteArray(versionfile));

						if (!versionfileHash.getSHA1().equals(projectfileHash.getSHA1())) {

							//if a file in the project directory was already in the latest version: show the difference
							List<String> original = Utilities.fileToLines(versionfile.getAbsolutePath());
							List<String> revised  = Utilities.fileToLines(projectfile.getAbsolutePath());

							DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();             
							boolean sideBySide = true;  //default -> inline
							builder.showInlineDiffs(!sideBySide); 
							builder.columnWidth(120);
							DiffRowGenerator dfg = builder.build();                
							List<DiffRow> rows = dfg.generateDiffRows(original, revised);    

							int rowNumber = 0;
							for (DiffRow row: rows) {
								rowNumber++;
								if (!row.toString().equals("")) {
									answer += "\t   Line " + rowNumber +": " + row + "\n";
								}
							}
							answer += "\n";
						} else {
							answer += "\t   #There are no changes in this file \n \n";
						}
					} else {
						//if a file in the project directory was not in the latest version: file is recently added
						answer += "\t   #This file is recently added to the project \n \n";
					}
				}
			}
		} else {
			answer += "\t there are no files in the repository to diff with the latest version \n";
		}
		return answer;
	}
	
	public String status() {
		String answer = "";
		List<String> files = index;
		if (HEAD != null) {
			answer += "  Latest version : \n     ID: " + HEAD.ID + "\n     Author: " + HEAD.author + "\n     Message: " + HEAD.message + "\n";
			if (HEAD.parent != null) {
				answer += "     Parent: " + HEAD.parent.ID + "\n \n";
			} else {
				answer += "\n";
			}
		} else {
			answer += "  There is no previous version \n \n";
		}

		answer += "  Untracked files: \n    (Use \"vcs add <file>\" to include in what will be committed) \n";
		File directory = new File(ProjectDirectory);
		File[] fileList = directory.listFiles();
		if (fileList.length-index.size() != 2) {
			for (File file : fileList) {
				String fileName = file.getName();
				String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
				if (file.isFile() && (!fileName.equals(".DS_Store")) && (!index.contains(fileName)) && (!extension.equals("key"))) {
					answer += "\t #" + file.getName() + "\n";
				}
			}
		} else {
			answer += "\t there are no untracked files \n";
		}

		answer += "\n  Changes to be commited: \n    (Use \"vcs remove <file>\" to delete files out of the staging area) \n";
		if (files != null && !files.isEmpty()) {
			for(int i=0; i<files.size(); i++) {
				answer += "\t #" + files.get(i) + "\n";
			}
		} else {
			answer += "\t no files will be commited \n";
		}
		return answer;
	}
	
	

	public void emptyIndex() throws IOException {
		index.clear();
		putMeta("index");
	}
	
	//When there is a commit on the client-side, the commited files will be stores in the headfiles folder.
	public void putHeadFiles() throws IOException {
		directory.setWorkingDir(HeadFilesDirectory);
		directory.cleanDir();
		if (HEAD != null) {
			for(int i=0; i<HEAD.files.size(); i++) {
				String path = ProjectDirectory + File.separator + HEAD.files.get(i);
				directory.putFile(path);  	
			}
		}
	}
	
	//When there is a checkout or update, the files from the server will be put in the headfiles folder.
	//Here after, the files will be transferred to the projectdirectory if there are no conflicts with existing files.

	public void transferHeadFiles(BufferedReader consoleInput) throws IOException {
		directory.setWorkingDir(ProjectDirectory);
		if (HEAD != null) {
			for(int i=0; i<HEAD.files.size(); i++) {
				String headpath = HeadFilesDirectory + File.separator + HEAD.files.get(i);
				String projectpath = ProjectDirectory + File.separator + HEAD.files.get(i);
				File headfile = new File(headpath);
				File projectfile = new File(projectpath);

				if (projectfile.isFile()) {
					SHA1 headfileHash = new SHA1(Utilities.FileToByteArray(headfile));
					SHA1 projectfileHash = new SHA1(Utilities.FileToByteArray(projectfile));
					if (!headfileHash.getSHA1().equals(projectfileHash.getSHA1())) {
						boolean validAnswer = false;
						System.out.println("#Conflict: \"" + projectfile.getName() + "\" exists already in your project and isn't equal as the file on the server. \n Do you want to override the file with the file from the server or keep the file unmodified? \n Write \"o\" to override or \"u\" to keep the file unmodified");
						while (!validAnswer) {
							System.out.print("> ");
							String answer = consoleInput.readLine();
							if (answer.equals("o")) {
								validAnswer = true;
								directory.putFile(headpath);  	
							} else if (answer.equals("u")) {
								validAnswer = true;
							} else {
								System.out.println(" Please give a valid answer");
							}
						}
					} 
				} else {
					directory.putFile(headpath);  
				}
			}
		}
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
