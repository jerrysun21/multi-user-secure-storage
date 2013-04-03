package jerrysun21.nujnah.multi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class UserFolderBrowser extends Activity {
	File UserDir;
	String strUserDir;
	MultiUserFileAdapter adapter;
	TextView status;
	ListView lv;
	String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_folder_browser);
		Bundle data = getIntent().getExtras();
		status = (TextView) findViewById(R.id.ufb_status_text);
		lv = (ListView) findViewById(R.id.ufb_file_list);

		if (data != null) {
			strUserDir = data.getString("userdir");
		}
		UserDir = new File(strUserDir);

		if (UserDir.listFiles().length > 0) {
			status.setVisibility(View.GONE);
		} else {
			status.setVisibility(View.VISIBLE);
			status.setText("Empty Folder");
		}

		password = "blahblahblah";

		showFileList(UserDir);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("jerry", "userdir: " + strUserDir);
		adapter.setPassword(password);
		encryptFiles(strUserDir);
	}

	private ArrayList<File> populateFileList(File dir) {
		if (!dir.isDirectory()) {
			return null;
		} else {
			ArrayList<File> list = new ArrayList<File>();
			list.add(dir.getParentFile());
			for (int i = 0; i < dir.listFiles().length; i++) {
				File temp = dir.listFiles()[i];
				list.add(temp);
			}
			return list;
		}
	}

	private void showFileList(File dir) {
		if (!dir.isDirectory())
			return;

		ArrayList<File> fileList = populateFileList(dir);
		adapter = new MultiUserFileAdapter(this, R.layout.folder_list_item,
				fileList, 1);
		lv.setAdapter(adapter);
	}

	public void encryptFiles(String userdir) {
		File currentFile = new File(userdir);
		List<File> fileList = Arrays.asList(currentFile.listFiles());

		for (int i = 0; i < fileList.size(); i++) {
			File temp = fileList.get(i);
			if (temp.isDirectory()) {
				encryptFiles(temp.getAbsolutePath());
			} else if (temp.isFile()) {
				String extension = temp.getName();
				Log.d("jerry", "file name: " + extension);
				if (extension.substring(extension.lastIndexOf('.')).equals(
						".security")) {
					try {
						String data = readFileData(temp);
						String newFileName = temp.getAbsolutePath().substring(0,
								temp.getAbsolutePath().lastIndexOf('.'));
						Log.d("jerry", "Data: " + data + "\nName: " + newFileName);
						encryptFile(newFileName, data, password);
						temp.delete();
					} catch (Exception e) {
						Log.e("jerry", "error reading file");
						e.printStackTrace();
					}
				}
			}
		}
	}

	private String readFileData(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);

		int size = (int) fis.getChannel().size();
		byte[] data = new byte[size];
		fis.read(data, 0, size);
		Log.d("jerry", "reading all data " + size);
		String s = new String(data, "UTF-8");
		fis.close();

		return s;
	}

	private void encryptFile(String fileName, String dataToEncrypt,
			String password) throws Exception {
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		if (dataToEncrypt.length() % 16 != 0) {
			char pad = (char) (16 - dataToEncrypt.length() % 16);
			for (int i = 0; i < pad; i++) {
				dataToEncrypt += pad;
			}
		} else {
			char pad = (char) 16;
			for (int i = 0; i < pad; i++) {
				dataToEncrypt += pad;
			}
		}

		MessageDigest digest1 = MessageDigest.getInstance("SHA");
		digest1.update(password.getBytes());
		// AES requires a key with 128 bits (16 bytes)
		SecretKeySpec key1 = new SecretKeySpec(digest1.digest(), 0, 16, "AES");
		aes.init(Cipher.ENCRYPT_MODE, key1);

		FileOutputStream fos = new FileOutputStream(new File(fileName));
		Log.d("jerry", "writing all data "
				+ dataToEncrypt.getBytes("UTF-8").length);
		CipherOutputStream cos = new CipherOutputStream(fos, aes);
		cos.write(dataToEncrypt.getBytes("UTF-8"));
		cos.flush();
		fos.flush();
		cos.close();
		fos.close();
	}
}
