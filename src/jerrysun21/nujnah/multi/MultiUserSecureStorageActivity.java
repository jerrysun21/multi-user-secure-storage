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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MultiUserSecureStorageActivity extends Activity {    
    
    final String strAppDir = "jerrysun21.nujnah.multi";
    final String strUserFile = "users";
    ArrayList<MultiUserInfo> users = new ArrayList<MultiUserInfo>();
    Button btnTapNFC;
    // TextView for the line of text in the main activity
    TextView tvMainText;
    ArrayAdapter<String> adapter;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        btnTapNFC = (Button)findViewById(R.id.main_tap_NFC);
        tvMainText = (TextView)findViewById(R.id.main_text);
        
        tvMainText.setVisibility(View.GONE);
        btnTapNFC.setVisibility(View.GONE);
        
        String diskState = Environment.getExternalStorageState();
        
        if (diskState.equals(Environment.MEDIA_UNMOUNTED) || diskState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
        	Toast.makeText(getApplicationContext(), "Storage not useable", Toast.LENGTH_SHORT).show();
        } else {
        	File sd = Environment.getExternalStorageDirectory();
        	
        	// Get the data directory in SD card root (/something-something/0)
        	sd = getFile("data", sd.listFiles());
        	if (sd == null) {
        		File newDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "data");
        		newDir.mkdir();
        		sd = getFile("data", Environment.getExternalStorageDirectory().listFiles());
        	}
        	
        	// Check if application directory exists
        	File appDir = getFile(strAppDir, sd.listFiles());
        	if (appDir == null) {
        		// This folder not showing up on explorer (windows)
        		appDir = new File(sd.getAbsolutePath(), strAppDir);
        		appDir.mkdir();
        	}
        	
        	// Check for a list of users, make my life easy and use a textfile
        	
        	File userFile = getFile(strUserFile, appDir.listFiles());
        	if (userFile == null) {
        		userFile = new File (appDir.getAbsoluteFile(), strUserFile);
        		try {
					userFile.createNewFile();
					userFile.setWritable(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	
        	// Build list of users
        	users = getUserList(userFile);
        	if (users.size() == 0) {
        		Toast.makeText(getApplicationContext(), "No users found please create a user", Toast.LENGTH_SHORT).show();
        		createUserDialog(this, appDir);
        		tvMainText.setVisibility(View.VISIBLE);
        		tvMainText.setText("No users found, please create a new user");
        	} else {
        		btnTapNFC.setVisibility(View.VISIBLE);
        	}
        }
    }
        
    private File getFile(String directoryName, File[] fileList) {
    	for (int i = 0; i < fileList.length; i++)
    		if (fileList[i].getName().equals(directoryName))
    			return fileList[i];
    	return null;
    }
    
    // Probably move this into another thread, would get slow once there are a lot of users
    private ArrayList<MultiUserInfo> getUserList(File userFile) {
    	ArrayList<MultiUserInfo> list = new ArrayList<MultiUserInfo> ();
    	String line;
    	
    	try {
    		FileInputStream fin = new FileInputStream(userFile);
    		BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
    		while ((line = reader.readLine()) != null) {
    			String[] temp;
    			// Split the line by '-' and create a temporary user
    			temp = line.split("-");
    			MultiUserInfo tempUser = new MultiUserInfo(temp[0], temp[1], temp[2]);
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
    	dialog.setContentView(R.layout.dialog_create_user);
    	
    	final EditText usernameEdit = (EditText)dialog.findViewById(R.id.create_user_user_name);
    	final EditText passwordEdit = (EditText)dialog.findViewById(R.id.create_user_password);
    	
    	Button okButton = (Button)dialog.findViewById(R.id.cuOK);
    	Button cancelButton = (Button)dialog.findViewById(R.id.cuCancel);
    	
    	okButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String username = usernameEdit.getText().toString();
				// Need to verify password length/strength
				String password = MultiHelper.toSHA1(passwordEdit.getText().toString());
				String nfc = "NFC";		// Temporary
				MultiUserInfo newUser = new MultiUserInfo(username, password, nfc);
				createUser(newUser, appDir);
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
		File userFolder = new File (appDir.getAbsolutePath(), newUser.getUserId());
		userFolder.mkdir();
		File userList = getFile(strUserFile, appDir.listFiles());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(userList, true));
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
			users = getUserList(userList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }
    


    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        //textView = (TextView) findViewById(R.id.textView);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        tvMainText.setText(new String(msg.getRecords()[0].getPayload()));
    }
    
    public void launchEmit(View v) {
    	Intent intent = new Intent(this, PasswordEmitActivity.class);
    	startActivity(intent);
    }

}