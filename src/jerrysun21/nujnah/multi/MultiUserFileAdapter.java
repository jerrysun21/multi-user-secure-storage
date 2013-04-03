package jerrysun21.nujnah.multi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MultiUserFileAdapter extends ArrayAdapter<File> {

	private Context context;
	private List<File> list;
	private int lvResource;
	private int type;
	private String password = null;
	private File CurrentDir;

	public MultiUserFileAdapter(Context context, int textViewResourceId,
			List<File> objects) {
		this(context, textViewResourceId, objects, 0, null);
	}

	public MultiUserFileAdapter(Context context, int textViewResourceId,
			List<File> objects, int type, File CurrentDir) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.list = objects;
		this.lvResource = textViewResourceId;
		this.type = type;
		this.CurrentDir = CurrentDir;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (view == null)
			view = inflater.inflate(lvResource, parent, false);

		TextView tv = (TextView) view.findViewById(R.id.item_name);
		tv.setText(list.get(position).getName());

		File currentFile = list.get(position);
		Log.d("adapter", "Item: " + currentFile.getName() + " Is directory: "
				+ currentFile.isDirectory());
		if (currentFile.isDirectory())
			tv.setTypeface(Typeface.DEFAULT_BOLD);
		else
			tv.setTypeface(Typeface.DEFAULT);

		if (position == 0 && type == 1) {
			tv.setText("..");
		}

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String item = ((TextView) v.findViewById(R.id.item_name))
						.getText().toString();
				clickFile(item);
			}
		});

		return view;
	}

	private void clickFile(String item) {
		File userdir = null;
		Bundle data = new Bundle();
		Boolean isDir = true;

		for (int i = 0; i < list.size(); i++) {
			File temp = list.get(i);
			if (temp.getName().equals(item) && temp.isDirectory()) {
				userdir = list.get(i);
				break;
			} else if (item.equals("..")) {
				if (i == 0) {
					userdir = temp; // Going up a level
					break;
				}
			} else if (temp.getName().equals(item)) {
				userdir = temp;
				isDir = false;
			}
		}

		if (isDir) {
			if (userdir != null && type == 0) {
				// Login
				showLoginDialog(item, userdir);
			} else if (userdir != null && type == 1) {
				// navigate to folder instead
				if (userdir.getAbsolutePath().equals(
						"/storage/emulated/0/data/jerrysun21.nujnah.multi")) {
					((Activity) context).finish();
				} else {
					clear();
					File[] files = userdir.listFiles();
					ArrayList<File> newFiles = new ArrayList<File>();
					add(userdir.getParentFile());
					newFiles.add(userdir.getParentFile());
					for (int i = 0; i < files.length; i++) {
						newFiles.add(files[i]);
						add(files[i]);
					}
					list = newFiles;
					notifyDataSetChanged();
					CurrentDir = userdir;
				}
			}
		} else {
			try {
				String decryptedData = SecurityHelper.decryptFile(
						userdir.getAbsolutePath(), password);
				writeTempFile(
						userdir.getAbsolutePath().substring(0,
								userdir.getAbsolutePath().length() - 4)
								+ "-sec.txt", decryptedData);
				userdir = new File(userdir.getAbsolutePath().substring(0,
						userdir.getAbsolutePath().length() - 4)
						+ "-sec.txt");

				Intent fileintent = new Intent(Intent.ACTION_VIEW);
				Uri uri = Uri.fromFile(userdir.getAbsoluteFile());
				String url = uri.toString();

				// grab mime
				String newMimeType = MimeTypeMap.getSingleton()
						.getMimeTypeFromExtension(
								MimeTypeMap.getFileExtensionFromUrl(url));
				fileintent.setDataAndType(uri, newMimeType);
				try {
					context.startActivity(fileintent);
				} catch (ActivityNotFoundException e) {
					Log.e("tag",
							"No activity can handle picking a file. Showing alternatives.");
				}
			} catch (Exception e) {
				Log.e("jerry", "error reading file");
				e.printStackTrace();
			}
		}
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private void writeTempFile(String fileName, String dataToWrite)
			throws Exception {
		FileOutputStream fos = new FileOutputStream(new File(fileName));
		fos.write(dataToWrite.getBytes("UTF-8"));
		fos.flush();
		fos.close();
	}

	public File getCurrentDir() {
		return CurrentDir;
	}

	private void showLoginDialog(final String username, final File userdir) {
		final Dialog loginDialog = new Dialog((Activity) context);
		Button btnOk;
		Button btnCancel;
		TextView tvUser;

		loginDialog.setTitle("Login");
		loginDialog.setContentView(R.layout.dialog_login);

		btnOk = (Button) loginDialog.findViewById(R.id.login_OK);
		btnCancel = (Button) loginDialog.findViewById(R.id.login_Cancel);

		tvUser = (TextView) loginDialog.findViewById(R.id.login_id);
		tvUser.setText(username);

		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MultiUserInfo user = new MultiUserInfo("fake", "", "");
				Activity activity = (Activity) context;
				Bundle data = new Bundle();
				EditText etPassword = (EditText) loginDialog
						.findViewById(R.id.login_pw);

				String pw = etPassword.getText().toString();

				pw += MultiUserList.getInstance().nfcData;

				pw += Secure.getString(activity.getBaseContext()
						.getContentResolver(), Secure.ANDROID_ID);

				ArrayList<MultiUserInfo> users = MultiUserList.getInstance().users;
				for (int i = 0; i < users.size(); i++) {
					if (users.get(i).getUserId().equals(username))
						user = users.get(i);
				}

				Log.d("login", "password: " + pw);
				Log.d("jerry", "password rawwed: " + pw);
				Log.d("jerry", "password hashed: " + user.getPassword());
				Log.d("jerry", "password entred: " + MultiHelper.toSHA1(pw));

				if (MultiHelper.toSHA1(pw).equals(user.getRawPassword())) {
					
					byte[] checksum = SecurityHelper.generateChecksum(userdir.getAbsolutePath());
					String s = "";
					try {
						s = new String(checksum, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					Log.d("jerry", "Checksum of all the files: " + s);
					Log.d("jerry", "Checksum stored in the db: " + user.getNFCToken());
					if (user.getNFCToken().equals("NFC") || s.equals(user.getNFCToken())) {
						Log.d("jerry", "this works");
						// Send password
						data.putString("username", username);
						data.putString("userdir", userdir.getAbsolutePath());
						data.putString("password", pw);
						
						Intent intent = new Intent(context, UserFolderBrowser.class);
						intent.putExtras(data);
						context.startActivity(intent);
					} else {
						Toast.makeText(activity, "Files have been tampered with",
								Toast.LENGTH_SHORT).show();
						loginDialog.dismiss();
					}

				} else {
					Toast.makeText(activity, "Incorrect credentials",
							Toast.LENGTH_SHORT).show();
					loginDialog.dismiss();
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loginDialog.dismiss();

			}
		});

		loginDialog.show();
	}
}
