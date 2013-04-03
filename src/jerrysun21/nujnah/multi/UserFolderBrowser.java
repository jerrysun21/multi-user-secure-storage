package jerrysun21.nujnah.multi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UserFolderBrowser extends Activity {
	File UserDir;
	String strUserDir;
	MultiUserFileAdapter adapter;
	TextView status;
	ListView lv;
	String password;
	Context context;
	Button btnAddUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.user_folder_browser);
		Bundle data = getIntent().getExtras();
		status = (TextView) findViewById(R.id.ufb_status_text);
		lv = (ListView) findViewById(R.id.ufb_file_list);
		btnAddUser = (Button) findViewById(R.id.ufb_add_file);

		btnAddUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File current = adapter.getCurrentDir();
				File userList = new File(adapter.getCurrentDir(), Integer
						.toString(adapter.getCurrentDir().listFiles().length)
						+ ".txt");
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							userList, true));
					// Append and add '-' after each attribute
					writer.append("junk data junk data junk data junk data junk data junk data junk data junk data");
					writer.newLine();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				adapter.clear();
				File[] files = current.listFiles();
				for (int i = 0; i < files.length; i++)
					adapter.add(files[i]);
				adapter.notifyDataSetChanged();
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
				byte[] checksum = SecurityHelper.generateChecksum(strUserDir);
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("jerry", "userdir: " + strUserDir);
		adapter.setPassword(password);
		SecurityHelper.encryptFiles(strUserDir, password);
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
}
