package jerrysun21.nujnah.multi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MultiUserList {
	
	public ArrayList<MultiUserInfo> users = new ArrayList<MultiUserInfo>();
	public String nfcData;
	
	private static MultiUserList instance;
	
	public static synchronized MultiUserList getInstance() {
		if (instance == null) {
			return null;
		}
		return instance;
	}
	
	// this getInstance always needs to be called at least once before the other getInstance
	public static synchronized MultiUserList getInstance(File userFile) {
		if (instance == null) {
			instance = new MultiUserList(userFile);
		}
		return instance;
	}
	
	private MultiUserList(File userFile) {
		users = getUserList(userFile);
	}
	
	// Probably move this into another thread, would get slow once there are a
	// lot of users
	private ArrayList<MultiUserInfo> getUserList(File userFile) {
		ArrayList<MultiUserInfo> list = new ArrayList<MultiUserInfo>();
		String line;

		try {
			FileInputStream fin = new FileInputStream(userFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fin));
			while ((line = reader.readLine()) != null) {
				String[] temp;
				// Split the line by '-' and create a temporary user
				temp = line.split("-");
				MultiUserInfo tempUser = new MultiUserInfo(temp[0], temp[1],
						temp[2]);
				list.add(tempUser);
			}
			reader.close();
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
	
}
