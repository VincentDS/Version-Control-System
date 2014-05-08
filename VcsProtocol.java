import java.io.IOException;


public interface VcsProtocol {
	
	public String add(String filename) throws IOException;
	public String checkout();
	public String commit(String comment) throws IOException;
	public String diff();
	public String update();
	public String status();
	
}
