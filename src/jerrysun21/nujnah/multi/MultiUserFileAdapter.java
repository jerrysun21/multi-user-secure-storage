package jerrysun21.nujnah.multi;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MultiUserFileAdapter extends ArrayAdapter<File> {

	private Context context;
	private List<File> list;
	private int lvResource;
	
	public MultiUserFileAdapter(Context context, int textViewResourceId,
			List<File> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = objects;
		this.lvResource = textViewResourceId;
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		View view = convertView;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (view == null)
			view = inflater.inflate(lvResource, parent, false);
		
		TextView tv = (TextView)view.findViewById(R.id.item_name);
		tv.setText(list.get(position).getName());
		
		File currentFile = list.get(position);
		
		if (currentFile.isDirectory())
			tv.setTypeface(Typeface.DEFAULT_BOLD);
		
		tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();				
			}
		});
		
		return view;
	}
}
