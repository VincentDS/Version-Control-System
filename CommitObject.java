import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


/* 
 * Een commit object is een object dat behoort tot een bepaalde commit.
 * Deze bevat: 
 * 		- Een set van files die tot de commit behoren
 * 		- Referentie naar een parent commit object 
 * 		- SHA1 ID, een 40-char string dat het object identificeert
 */
public class CommitObject {
	public ClientRepository repo;
	public List<String> files;
	public String message;
	public String ID;
	public CommitObject parent;
	public String CurrentObjectDirectory;
	
	public CommitObject(ClientRepository repo, CommitObject parent, String message) throws IOException {
		this.repo = repo;
		this.parent = parent;
		this.message = message;
		files = repo.index;
	
		//unique ID creeren
		SHA1 hash = new SHA1(FilesToByteArray(files));
		ID = hash.getSHA1();
		
		//directory maken met deze ID
	/*    CurrentObjectDirectory = repo.CommitObjectDirectory + File.separator + ID;
		repo.directory.setWorkingDir(repo.CommitObjectDirectory);
	    repo.directory.createDir(ID);
	*/	
		//bestanden in de index kopieren naar de directory
		repo.directory.setWorkingDir(CurrentObjectDirectory);
	    for(int i=0; i<=files.size()-1; i++) {
	    	String path = repo.ProjectDirectory + File.separator + files.get(i);
	    	repo.directory.putFile(path);  	
	    }
	    
		//info file met message, parent, id aanmaken en in de directory zetten
	    MetaFile();
	}
	
	//Makes a bytearray from all the files that are in the staging area
	public byte[] FilesToByteArray(List<String> files) throws IOException {
		byte[] buffer;
		int length = 0;
		for(int i=0; i<=files.size()-1; i++) {
			String path = repo.ProjectDirectory + File.separator + files.get(i);
			File file = new File(path);
			length += file.length();
		}
		buffer = new byte[length];
		int bufptr = 0;
		for(int i=0; i<=files.size()-1; i++) {
			String path = repo.ProjectDirectory + File.separator + files.get(i);
			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			fis.read(buffer, bufptr, (int)file.length());
			bufptr += (int)file.length();
			fis.close();
		}
		return buffer;
	}
	
	//creates a metafile in a CommitObject-folder with the message, ID(SHA1) and the ID of the parent
	public void MetaFile() {
		try {
			File file = new File(CurrentObjectDirectory + File.separator + ".meta");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Message: " + message + "\n");
			bw.write("ID: " + ID + "\n");
			//parent is null when there is no previous commit. 
			if (parent != null) {
				bw.write("Parent: " + parent.ID + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
