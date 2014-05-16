import java.io.Serializable;
 
public class FileObject implements Serializable {
	
    public String sourceDirectory;
    private String filename;
    private long fileSize;
    private byte[] fileData;
    private static final long serialVersionUID = 1L;

	public FileObject() {
    }
 
    public String getFilename() {
        return filename;
    }
 
    public void setFilename(String filename) {
        this.filename = filename;
    }
 
    public long getFileSize() {
        return fileSize;
    }
 
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
 
    public byte[] getFileData() {
        return fileData;
    }
 
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}

