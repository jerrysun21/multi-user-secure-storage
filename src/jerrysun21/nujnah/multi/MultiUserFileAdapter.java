package jerrysun21.nujnah.multi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MultiUserFileAdapter extends ArrayAdapter<File> {

	private Context context;
	private List<File> list;
	private int lvResource;
	private int type;
	
	public MultiUserFileAdapter(Context context, int textViewResourceId,
			List<File> objects) {
		this(context, textViewResourceId, objects, 0);
		// TODO Auto-generated constructor stub
	}
	public MultiUserFileAdapter(Context context, int textViewResourceId,
			List<File> objects, int type) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = objects;
		this.lvResource = textViewResourceId;
		this.type = type;
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

		for (int i = 0; i < list.size(); i++) {
			File temp = list.get(i);
			if (temp.getName().equals(item) && temp.isDirectory()) {
				userdir = list.get(i);
				break;
			} else if (item.equals("..")) {
				if (i == 0) {
					userdir = temp; 		// Going up a level
					break;
				}
			}
		}

		if (userdir != null && type == 0) {
			data.putString("userdir", userdir.getAbsolutePath());

			Intent intent = new Intent(context, UserFolderBrowser.class);
			intent.putExtras(data);
			context.startActivity(intent);
		} else if (userdir != null && type == 1) {
			// navigate to folder instead
			if (userdir.getAbsolutePath().equals("/storage/emulated/0/data/jerrysun21.nujnah.multi")) {
				((Activity)context).finish();
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
			}
		}
	}
}
