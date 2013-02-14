package jerrysun21.nujnah.multi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MultiUserSecureStorageActivity extends Activity {    
    
    final String strAppDir = "jerrysun21.nujnah.multi";
    final String strUserFile = "users";
    ArrayList<String> users = new ArrayList<String>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
        		// This folder not showing up on explorer
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
        	}
        	
        	//if (diskState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
        }
    }
        
    private File getFile(String directoryName, File[] fileList) {
    	for (int i = 0; i < fileList.length; i++)
    		if (fileList[i].getName().equals(directoryName))
    			return fileList[i];
    	return null;
    }
    
    // Probably move this into another thread, would get slow once there are a lot of users
    private ArrayList<String> getUserList(File userFile) {
    	ArrayList<String> list = new ArrayList<String> ();
    	String line;
    	
    	try {
    		InputStream inStream = this.getAssets().open(userFile.getAbsolutePath());
    		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
    		while ((line = reader.readLine()) != null) {
    			list.add(line);
    		reader.close();
    		inStream.close();
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return list;
    }
    
    private void createUserDialog(Activity activity, final File appDir) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	
    	LayoutInflater inflater = activity.getLayoutInflater();
    	View dialogLayout = inflater.inflate(R.layout.dialog_create_user, null);
    	final EditText usernameEdit = (EditText)dialogLayout.findViewById(R.id.create_user_user_name);
    	// handle password later
    	
    	builder.setView(inflater.inflate(R.layout.dialog_create_user, null))
    		.setPositiveButton("Create", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String username = usernameEdit.getText().toString();
					users.add(username);
					createUser(username, appDir);
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Don't know what else to do but cancel
					dialog.cancel();
				}
			});
    	builder.create().show();
    }
    
    // Appends username to file & creates a folder for the user
    private void createUser(String username, final File appDir) {
		File userFolder = new File (appDir.getAbsolutePath(), username);
		// This folder is not being created
		userFolder.mkdir();
		File userList = getFile(strUserFile, appDir.listFiles());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(userList, true));
			// Append not working
			writer.append(username);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /*
   		lv = (ListView)findViewById(R.id.tag_list);
		ArrayList<String> tag_list = new ArrayList<String>();
		for (int i = 0; i < 20; i++)
			tag_list.add("Sidebar Item");		// test data for now
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_tag_list_item , tag_list);
		lv.setAdapter(adapter);
     */
}