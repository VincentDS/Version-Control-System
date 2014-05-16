import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class WorkingDirectory implements Serializable {

	private File current;
		
	public WorkingDirectory(String path) {
		this.current = new File(path);
	}
	
	public String getWorkingDir() throws IOException {
		return current.getCanonicalPath(); //canonical is als absolute maar zonder /../
	}
	
	public boolean setWorkingDir(String path) throws IOException {
		File newdirectory = new File(path);
		if (newdirectory.exists()) {
			current = newdirectory;
			return true;
		}
		return false;
	}
	
	public boolean changeWorkingDir(String path) throws IOException {
		File newdirectory = new File(getWorkingDir() + File.separator + path);
		if (newdirectory.exists()) {
			current = newdirectory;
			return true;
		}
		return false;
	}
	
	public String[] listFiles() {
		return current.list();
	}	
	
	public boolean copyFile(String from, String to) throws FileNotFoundException { 
		FileInputStream in = new FileInputStream(from);
		FileOutputStream out = new FileOutputStream(to); 
		int c;
		try {
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			in.close();
			out.close();
		}
		catch (IOException e) {
			return false;	
		}
		return true;
	}
	
	//kopieert bestand van huidige directory met naam filename naar het pad 'to'.
	public boolean getFile(String filename, String to) throws IOException {
		String path = getWorkingDir() + File.separator + filename;
		File file = new File(path);
		return copyFile(file.getCanonicalPath(), to);
	}
	
	//kopieert bestand op filepath naar bestand in huidige directory met dezefde naam.
	public boolean putFile(String filepath) throws IOException { //we hebben enkel de naam van de file nodig, niet heel het pad
		String newfilepath = getWorkingDir() + File.separator + new File(filepath).getName();
		return copyFile(filepath, newfilepath);
	}
	
	public boolean createDir(String name) throws IOException {
		File filename = new File(getWorkingDir() + File.separator + name);
		return filename.mkdir();
	}
	
	public boolean createFile(String name) throws IOException {
		return new File(getWorkingDir() + File.separator + name).createNewFile();
	}
	
	public boolean exists(String name) throws IOException {
		return new File(getWorkingDir() + File.separator + name).exists();
	}

}
