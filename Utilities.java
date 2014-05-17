import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;



/*
 * Compile in terminal: javac -cp external/diffutils-1.2.1.jar *.java
 * Run in terminal: 
 * 		Server: java -cp .:external/diffutils-1.2.1.jar VcsServer 8080
 * 		Client: java -cp .:external/diffutils-1.2.1.jar VcsClient
 */

public class Utilities {

	public static void sendFile(String fileName, ObjectOutputStream os) {
		FileObject fileObject = new FileObject();
		File file = new File(fileName);
		fileObject.setFilename(file.getName());
		DataInputStream diStream = null;
		try {
			diStream = new DataInputStream(new FileInputStream(file));
			long len = (int) file.length();
			byte[] fileBytes = new byte[(int) len];

			int read = 0;
			int numRead = 0;
			while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read,
					fileBytes.length - read)) >= 0) {
				read = read + numRead;
			}
			fileObject.setFileData(fileBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			os.writeObject(fileObject);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void receiveFile(ObjectInputStream is, String directoryPath) {
		try {
			FileObject FileObject = (FileObject) is.readObject();
			String outputFile = directoryPath + File.separator +  FileObject.getFilename();
			File file = new File(outputFile);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(FileObject.getFileData());
			fileOutputStream.flush();
			fileOutputStream.close();
			//System.out.println("Output file : " + outputFile + " is successfully saved ");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line = "";
        try {
                BufferedReader in = new BufferedReader(new FileReader(filename));
                while ((line = in.readLine()) != null) {
                        lines.add(line);
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return lines;
}
}

