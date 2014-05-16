import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


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
}

