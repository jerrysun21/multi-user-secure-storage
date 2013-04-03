package jerrysun21.nujnah.multi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Context;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserFolderBrowser extends Activity {
	File UserDir;
	String strUserDir;
	MultiUserFileAdapter adapter;
	TextView status;
	ListView lv;
	String password;
	Context context;
	Button btnAddUser;
	Button btnAddDir;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.user_folder_browser);
		Bundle data = getIntent().getExtras();
		status = (TextView) findViewById(R.id.ufb_status_text);
		lv = (ListView) findViewById(R.id.ufb_file_list);
		btnAddUser = (Button) findViewById(R.id.ufb_add_file);
		btnAddDir = (Button) findViewById(R.id.ufb_add_dir);

		btnAddUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createFile();
			}
		});
		
		btnAddDir.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//createDirectory();
			}
		});

		if (data != null) {
			strUserDir = data.getString("userdir");
			password = data.getString("password");
		}
		UserDir = new File(strUserDir);

		// TODO: generate a checksum for all the files in the user's directory.
		// If it matches then show

		if (UserDir.listFiles().length > 0) {
			status.setVisibility(View.GONE);
		} else {
			status.setVisibility(View.VISIBLE);
			status.setText("Empty Folder");
		}

		showFileList(UserDir);

		Button logout = (Button) findViewById(R.id.ufb_logout);
		logout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("jerry", "should be logging out");
				byte[] checksum = generateChecksum(strUserDir);
				try {
					String s = new String(checksum, "UTF-8");
					Log.d("jerry", "Checksum of all the files: " + s);
					// TODO: save the checksum somewhere
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				finish();
			}
		});
		refreshAdapter();
	}
	
	private void refreshAdapter() {
		File[] fileList = adapter.getCurrentDir().listFiles();
		
		
		ArrayList<File> files = new ArrayList<File>();
		ArrayList<File> dirs = new ArrayList<File>();
		
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory())
				dirs.add(fileList[i]);
			else
				files.add(fileList[i]);
		}
		
		adapter.clear();
		adapter.add(adapter.getCurrentDir().getParentFile());
		adapter.addAll(dirs);
		adapter.addAll(files);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("jerry", "userdir: " + strUserDir);
		adapter.setPassword(password);
		encryptFiles(strUserDir);
	}

	// TODO: on destroy, generate checksum for use next time

	@Override
	public void onBackPressed() {
		// consume all back button presses
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
				fileList, 1, dir);

		lv.setAdapter(adapter);
	}

	public byte[] generateChecksum(String userdir) {
		File currentFile = new File(userdir);
		List<File> fileList = Arrays.asList(currentFile.listFiles());
		byte[][] checksum = new byte[fileList.size()][];
		byte[] calculatedChecksum = new byte[0];

		for (int i = 0; i < fileList.size(); i++) {
			File temp = fileList.get(i);
			if (temp.isDirectory()) {
				checksum[i] = generateChecksum(temp.getAbsolutePath());
			} else if (temp.isFile()) {
				try {
					String data = readFileData(temp);
					MessageDigest digest1 = MessageDigest.getInstance("SHA");
					digest1.update(data.getBytes("UTF-8"));
					checksum[i] = digest1.digest();
				} catch (Exception e) {
					Log.e("jerry", "error reading file");
					e.printStackTrace();
				}
			}
		}

		try {
			for (int i = 0; i < fileList.size(); i++) {
				byte[] tempChecksum = new byte[calculatedChecksum.length
						+ checksum[i].length];
				MessageDigest digest1;
				digest1 = MessageDigest.getInstance("SHA");
				digest1.update(tempChecksum);
				calculatedChecksum = digest1.digest();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return calculatedChecksum;
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
						String newFileName = temp.getAbsolutePath().substring(
								0, temp.getAbsolutePath().lastIndexOf('.'));
						Log.d("jerry", "Data: " + data + "\nName: "
								+ newFileName + "\nPassword: " + password);
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
	
	private void createFile() {
		final Dialog createFileDialog = new Dialog(this);
		Button btnOk;
		Button btnCancel;
		
		createFileDialog.setTitle("Create a new file");
		createFileDialog.setContentView(R.layout.dialog_create_file);
		
		btnOk = (Button)createFileDialog.findViewById(R.id.cf_OK);
		btnCancel = (Button)createFileDialog.findViewById(R.id.cf_Cancel);
		
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText etFileName = (EditText)createFileDialog.findViewById(R.id.cf_file_name);
				EditText etContent = (EditText)createFileDialog.findViewById(R.id.cf_data);
				File currDir = adapter.getCurrentDir();
				File newFile = new File(currDir, etFileName.getText().toString() + ".txt");
				
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							newFile, true));
					// Append and add '-' after each attribute
					writer.append(etContent.getText().toString());
					writer.newLine();
					writer.close();
					Log.d(currDir.getName(), "Created file: " + etFileName.getText().toString());
					Toast.makeText(UserFolderBrowser.this, "Created file", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(currDir.getName(), "Error: " + e.getMessage());
				}
				
				refreshAdapter();
				createFileDialog.dismiss();
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createFileDialog.dismiss();
			}
		});
		
		createFileDialog.show();
	}
	
	private void createDirectory() {
		final Dialog createDirDialog = new Dialog(this);
		Button btnOK;
		Button btnCancel;
		
		createDirDialog.setTitle("Create Directory");
		createDirDialog.setContentView(R.layout.dialog_create_dir);
		
		btnOK = (Button)createDirDialog.findViewById(R.id.cd_OK);
		btnCancel = (Button)createDirDialog.findViewById(R.id.cd_Cancel);
		
		btnOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText etDirName = (EditText)createDirDialog.findViewById(R.id.cd_name);
				File newFolder = new File(adapter.getCurrentDir(), etDirName.getText().toString());
				
				newFolder.mkdir();
				Log.d(adapter.getCurrentDir().getName(), "Create directory: " + newFolder.getName());
				
				refreshAdapter();
				createDirDialog.dismiss();
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createDirDialog.dismiss();
				
			}
		});
		
		createDirDialog.show();
	}
}
