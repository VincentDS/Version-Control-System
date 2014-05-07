
public interface VcsProtocol {
	
	public void init();
	public void checkout();
	public void add();
	public void commit();
	public void update();
	public void status();
	public void diff();
	
}
