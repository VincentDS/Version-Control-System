import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class ClientRepository implements VcsProtocol, Serializable {
	
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

	
	public String checkout() {
		String answer = "";
		return answer;
	}

	
	public String commit(String message) throws IOException {
		CommitObject co = new CommitObject(this, HEAD, message);
		HEAD = co;
		putMeta("head");
		return "The files in the staging area were commited on clientside";
	}

	
	public String diff() {
		String answer = "";
		
		answer += "    Difference between the current Project-Directory and the latest version (ID: " + HEAD.ID +") \n \n";
		
		File directory = new File(ProjectDirectory);
		File[] fileList = directory.listFiles();
		if (fileList.length-index.size() != 2) {
			for (File projectfile : fileList) {
				if (projectfile.isFile() && (!projectfile.getName().equals(".DS_Store"))) {
					answer += "\t" + projectfile.getName() + ": \n";
					File versionfile = new File(HeadFilesDirectory + File.separator + projectfile.getName());
					if (versionfile.isFile()) {
						
						/*//if a file in the project directory was already in the latest version: show the difference
						List<String> original = Utilities.fileToLines(versionfile.getAbsolutePath());
		                List<String> revised  = Utilities.fileToLines(projectfile.getAbsolutePath());
		                
		                // Compute diff. Get the Patch object. Patch is the container for computed deltas.
		                difflib.Patch patch = DiffUtils.diff(original, revised);

		                for (Delta delta: patch.getDeltas()) {
		                	if (delta == null) {
		                		answer += "   " + delta;
		                	} else {
		                		answer += "\t   #There are no changes in this file";
		                	}
		                	answer += "\n \n";
		                }*/
						
		/*				diff_match_patch dmp = new diff_match_patch();
						dmp.Diff_Timeout = 16;
						LinesToCharsResult diffs = dmp.diff_linesToChars("lol \n hey", "lal");
						diff_cleanupSemantic(diffs);
						String diff = dmp.diff_toDelta(diffs);
						System.out.println(diff);
						
						  var a = dmp.diff_linesToChars_(text1, text2);
						  var lineText1 = a[0];
						  var lineText2 = a[1];
						  var lineArray = a[2];

						  var diffs = dmp.diff_main(lineText1, lineText2, false);

						  dmp.diff_charsToLines_(diffs, lineArray);
						  return diffs;
						  */

						
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

	
	public String update() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String status() {
		String answer = "";
		List<String> files = index;
		if (HEAD != null) {
			answer += "  Latest version : \n     ID: " + HEAD.ID + "\n     Message: " + HEAD.message + "\n";
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
				if (file.isFile() && (!file.getName().equals(".DS_Store")) && (!index.contains(file.getName()))) {
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
					boolean validAnswer = false;
					System.out.println("#Conflict: \"" + projectfile.getName() + "\" exists already in your project. \n Do you want to override the file with the file from the server or keep the file unmodified? \n Write \"o\" to override or \"u\" to keep the file unmodified");
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
