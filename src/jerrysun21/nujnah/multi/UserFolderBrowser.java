package jerrysun21.nujnah.multi;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class UserFolderBrowser extends Activity {
	File UserDir;
	String strUserDir;
	MultiUserFileAdapter adapter;
	TextView status;
	ListView lv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_folder_browser);
		Bundle data = getIntent().getExtras();
		status = (TextView)findViewById(R.id.ufb_status_text);
		lv = (ListView)findViewById(R.id.ufb_file_list);
		
		if (data != null) {
			strUserDir = data.getString("userdir");
		}
		UserDir = new File(strUserDir);
		
		if (UserDir.listFiles().length > 0 ) {
			status.setVisibility(View.GONE);
		} else {
			status.setVisibility(View.VISIBLE);
			status.setText("Empty Folder");
		}
		
		showFileList(UserDir);
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
		adapter = new MultiUserFileAdapter(this, R.layout.folder_list_item, fileList, 1);
		lv.setAdapter(adapter);
	}
}
