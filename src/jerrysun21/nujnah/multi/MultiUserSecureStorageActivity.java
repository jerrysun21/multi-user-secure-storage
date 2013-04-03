package jerrysun21.nujnah.multi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MultiUserSecureStorageActivity extends Activity {

	final String strAppDir = "jerrysun21.nujnah.multi";
	final String strUserFile = "users";
	Button btnTapNFC;
	Button btnCreateUser;
	Button btnEncrypt;
	// TextView for the line of text in the main activity
	TextView tvMainText;
	MultiUserFileAdapter adapter;
	ListView lv;
	File appDir;
	public static String nfcData;
	String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btnTapNFC = (Button) findViewById(R.id.main_tap_NFC);
		btnCreateUser = (Button) findViewById(R.id.main_create_user);
		tvMainText = (TextView) findViewById(R.id.main_text);
		lv = (ListView) findViewById(R.id.user_list);

		tvMainText.setVisibility(View.GONE);
		btnTapNFC.setVisibility(View.GONE);
		btnCreateUser.setVisibility(View.GONE);

		btnCreateUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createUserDialog(MultiUserSecureStorageActivity.this, appDir);
			}
		});

		lv.setClickable(true);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO: prompt the user to type in their password, generate
				// salted password to use for decryption etc
				// TODO: if password typed is wrong, don't launch intent, say
				// wrong password/NFC combination
				String item = ((TextView) arg1).getText().toString();
				Bundle data = new Bundle();
				Log.d("jerry",
						"==================================================\npassword being passed in "
								+ password);
				data.putString("password", password);
				File userdir = getFile(item, appDir.listFiles());
				if (userdir != null) {
					data.putString("userdir", userdir.getAbsolutePath());
					Intent intent = new Intent(
							MultiUserSecureStorageActivity.this,
							UserFolderBrowser.class);
					intent.putExtras(data);
					startActivity(intent);
				}
			}
		});

		String diskState = Environment.getExternalStorageState();

		if (diskState.equals(Environment.MEDIA_UNMOUNTED)
				|| diskState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			Toast.makeText(getApplicationContext(), "Storage not useable",
					Toast.LENGTH_SHORT).show();
		} else {
			File sd = Environment.getExternalStorageDirectory();

			// Get the data directory in SD card root (/something-something/0)
			sd = getFile("data", sd.listFiles());
			if (sd == null) {
				File newDir = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath(),
						"data");
				newDir.mkdir();
				sd = getFile("data", Environment.getExternalStorageDirectory()
						.listFiles());
			}

			// Check if application directory exists
			// If ever forget where this is,
			// /sdcard/data/jerrysun21.nujnah.multi/
			appDir = getFile(strAppDir, sd.listFiles());
			if (appDir == null) {
				// This folder not showing up on explorer (windows)
				appDir = new File(sd.getAbsolutePath(), strAppDir);
				appDir.mkdir();
			}

			// Check for a list of users, make my life easy and use a textfile

			File userFile = getFile(strUserFile, appDir.listFiles());
			if (userFile == null) {
				userFile = new File(appDir.getAbsoluteFile(), strUserFile);
				try {
					userFile.createNewFile();
					userFile.setWritable(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Build list of users
			ArrayList<MultiUserInfo> users = MultiUserList.getInstance(userFile).users;
			users = getUserList(userFile);
			if (users.size() == 0) {
				Toast.makeText(getApplicationContext(),
						"No users found please create a user",
						Toast.LENGTH_SHORT).show();
				createUserDialog(this, appDir);
				tvMainText.setVisibility(View.VISIBLE);
				tvMainText.setText("No users found, please create a new user");
			} else {
				btnTapNFC.setVisibility(View.VISIBLE);
				btnCreateUser.setVisibility(View.VISIBLE);
			}

			// Display list of users, based on file folders
			ArrayList<File> userNames = new ArrayList<File>();
			File files[] = appDir.listFiles();
			for (int i = 0; i < files.length; i++)
				if (!files[i].getAbsolutePath().equals(
						appDir.getAbsolutePath() + "/users"))
					userNames.add(files[i]);

			adapter = new MultiUserFileAdapter(this, R.layout.folder_list_item,
					userNames);
			lv.setAdapter(adapter);
			lv.requestFocus();
		}
		btnEncrypt = (Button) findViewById(R.id.main_encrypt);
		btnEncrypt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				password = SecurityHelper.setPassword("password", nfcData,
						getBaseContext());
				adapter.setPassword(password);

				String dataToEncrypt = "asldkfjaw;eghaoiwebnaowieh091y2509r8q2y389tghawoibuv;boiwye98rthq23ngv9pa8w3hbnp9wun3f9a8wheg[a9ubnaw9[8hytr-9182hrtpi1faqwfdcig";

				try {
					SecurityHelper.encryptFile(
							"/sdcard/data/jerrysun21.nujnah.multi/hello",
							dataToEncrypt, password);
					String s = SecurityHelper.decryptFile(
							"/sdcard/data/jerrysun21.nujnah.multi/hello",
							password);

					Log.d("jerry",
							dataToEncrypt + "\t" + dataToEncrypt.length()
									+ "\n" + s + "\t" + s.length());
					int trimLength = s.charAt(s.length() - 1);
					Log.d("jerry", "trim length is: " + trimLength);
					s = s.substring(0, s.length() - trimLength);
					Log.d("jerry", "trimmed file " + s);
					if (s.equals(dataToEncrypt)) {
						Log.d("jerry", "fuck padding");
					}
				} catch (Exception e) {
					Log.e("jerry", "BARGFFFFFFFFF");
					e.printStackTrace();
				}
			}
		});
	}

	private File getFile(String directoryName, File[] fileList) {
		for (int i = 0; i < fileList.length; i++)
			if (fileList[i].getName().equals(directoryName))
				return fileList[i];
		return null;
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

	// Launches dialog to create a new user, does not handle NFC tags yet
	private void createUserDialog(Activity activity, final File appDir) {
		final Dialog dialog = new Dialog(activity);
		dialog.setTitle("Create a User");
		dialog.setContentView(R.layout.dialog_create_user);

		final EditText usernameEdit = (EditText) dialog
				.findViewById(R.id.create_user_user_name);
		final EditText passwordEdit = (EditText) dialog
				.findViewById(R.id.create_user_password);

		Button okButton = (Button) dialog.findViewById(R.id.cuOK);
		Button cancelButton = (Button) dialog.findViewById(R.id.cuCancel);

		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (nfcData != null) {
					String username = usernameEdit.getText().toString();
					// Need to verify password length/strength
					String password = password = SecurityHelper.setPassword(
							username, passwordEdit.getText().toString(),
							MultiUserSecureStorageActivity.this);
					String nfc = "NFC"; // Temporary
					MultiUserInfo newUser = new MultiUserInfo(username,
							password, nfc);
					if (newUser.validateInfo())
						createUser(newUser, appDir);
					else
						Toast.makeText(MultiUserSecureStorageActivity.this,
								"Invalid username/password", Toast.LENGTH_SHORT)
								.show();
				}
				dialog.dismiss();
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();

	}

	// Appends username to file & creates a folder for the user
	private void createUser(MultiUserInfo newUser, final File appDir) {
		File userFolder = new File(appDir.getAbsolutePath(),
				newUser.getUserId());
		userFolder.mkdir();
		File userList = getFile(strUserFile, appDir.listFiles());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(userList,
					true));
			// Append and add '-' after each attribute
			writer.append(newUser.getUserId());
			writer.append("-");
			writer.append(newUser.getPassword());
			writer.append("-");
			writer.append(newUser.getNFCToken());
			writer.newLine();
			writer.close();

			// Display new list of users
			if (tvMainText.getVisibility() == View.VISIBLE)
				tvMainText.setVisibility(View.GONE);
			if (btnTapNFC.getVisibility() == View.GONE)
				btnTapNFC.setVisibility(View.VISIBLE);

			// Refresh the user list
			ArrayList<MultiUserInfo> users = MultiUserList.getInstance().users;
			users = getUserList(userList);

			if (users.size() > 0
					&& btnCreateUser.getVisibility() != View.VISIBLE)
				btnCreateUser.setVisibility(View.VISIBLE);

			adapter.clear();
			ArrayList<File> userNames = new ArrayList<File>();
			File files[] = appDir.listFiles();
			for (int i = 0; i < files.length; i++)
				if (!files[i].getAbsolutePath().equals(
						appDir.getAbsolutePath() + "/users"))
					userNames.add(files[i]);

			adapter.addAll(userNames);
			adapter.notifyDataSetChanged();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		Log.d("NFC",
				"nfc? "
						+ NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent()
								.getAction()));
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		// textView = (TextView) findViewById(R.id.textView);
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		String password = new String(msg.getRecords()[0].getPayload());
		password = password.substring(password.indexOf("jerry://") + 8,
				password.indexOf('Q', password.indexOf("jerry://")));
		tvMainText.setText(password);
		nfcData = password;
		Log.d("NFC", "NFC msg received: " + nfcData);
	}

}