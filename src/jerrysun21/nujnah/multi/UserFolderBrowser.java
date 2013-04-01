package jerrysun21.nujnah.multi;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;

public class UserFolderBrowser extends Activity {
	File UserDir;
	String strUserDir;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_folder_browser);
		Bundle data = getIntent().getExtras();
		
		if (data != null) {
			strUserDir = data.getString("userdir");
		}
		UserDir = new File(strUserDir);
		
	}
}
