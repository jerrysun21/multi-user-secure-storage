package jerrysun21.nujnah.multi;

import android.util.Log;

public class MultiUserInfo {
	private String userId;
	private String password;
	private String NFCToken;
	private String passwordRaw;
	
	public void setPasswordRaw(String passwordRaw) {
		this.passwordRaw = passwordRaw;
	}

	public MultiUserInfo(String userId, String password) {
		this(userId, password, "");
	}
	
	public MultiUserInfo(String userId, String password, String NFCToken) {
		this.userId = userId;
		this.password = password;
		this.NFCToken = NFCToken;
	}
	
	private Boolean checkCharacters(String input) {
		char[] invalidChar = {'/', '#', '\\', ' '};
		
		for (int i = 0; i < invalidChar.length; i++) {
			if (input.indexOf(invalidChar[i]) != -1)
				return false;
		}
				
		return true;
	}
	
	// Looks for invalid characters
	private Boolean checkUsername() {
		if (!checkCharacters(userId))
			return false;
		return true;
	}
	
	// Length and invalid characters
	private Boolean checkPassword() {
		if (!checkCharacters(passwordRaw))
			return false;
		
		if (passwordRaw.length() < 6)
			return false;
		
		return true;
	}
	
	// Santizes username/password
	public Boolean validateInfo() {
		if (!checkUsername() || !checkPassword())
			return false;
		else
			return true;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	// Returns hashed password
	public String getPassword() {
		return MultiHelper.toSHA1(password);
	}
	public String getRawPassword() {
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
