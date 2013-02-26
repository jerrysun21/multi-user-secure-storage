package jerrysun21.nujnah.multi;

public class MultiUserInfo {
	private String userId;
	private String password;
	private String NFCToken;
	
	public MultiUserInfo(String userId, String password) {
		this(userId, password, "");
	}
	
	public MultiUserInfo(String userId, String password, String NFCToken) {
		this.userId = userId;
		this.password = password;
		this.NFCToken = NFCToken;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNFCToken() {
		return NFCToken;
	}
	public void setNFCToken(String nFCToken) {
		NFCToken = nFCToken;
	}
}
