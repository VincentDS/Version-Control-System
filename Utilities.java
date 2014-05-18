import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;


public class Utilities {

	public static void sendFile(String fileName, ObjectOutputStream os, PublicKey publicKey) {
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
			byte[] encryptedFileBytes = RSA.encrypt(fileBytes, publicKey);
			fileObject.setFileData(encryptedFileBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			os.writeObject(fileObject);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void receiveFile(ObjectInputStream is, String directoryPath, PrivateKey privateKey) {
		try {
			FileObject FileObject = (FileObject) is.readObject();
			String outputFile = directoryPath + File.separator +  FileObject.getFilename();
			File file = new File(outputFile);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			byte[] encryptedFileBytes = FileObject.getFileData();
			byte[] decryptedFileBytes = RSA.decrypt(encryptedFileBytes, privateKey);
			fileOutputStream.write(decryptedFileBytes);
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
                in.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
        return lines;
	}
	
	public static byte[] FileToByteArray(File file) throws IOException {
		byte[] buffer = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(buffer);
		fis.close();
		return buffer;
	}
}

