package com.dolphin.android.imgdownloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dolphin.android.imgdownloader.R;

/**
 * Created by Administrator on 2/13/14.
 */
public class ListDrawerAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final String[] values;

	public ListDrawerAdapter(Context context, String[] values) {
		super(context, R.layout.item_list_drawer_320, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.item_list_drawer_320, parent,
				false);
		TextView textView = (TextView) rowView.findViewById(R.id.txtTitle);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.imgIcon);
		textView.setText(values[position]);
		// change the icon for Windows and iPhone
		String s = values[position];
		switch (position) {
		case 0:
			imageView.setImageResource(R.drawable.start_drawer_photo);
			break;
		case 1:
			imageView.setImageResource(R.drawable.start_drawer_account);
			break;
		case 2:
			imageView.setImageResource(R.drawable.start_drawer_config);
			break;
		case 3:
			imageView.setImageResource(R.drawable.start_drawer_about);
			break;
		case 4:
			imageView.setImageResource(R.drawable.start_drawer_exit);
		default:
			break;
		}
		// if (s.startsWith("iPhone")) {
		// imageView.setImageResource(R.drawable.ic_drawer);
		// } else {
		// imageView.setImageResource(R.drawable.ic_launcher);
		// }
		return rowView;
	}
}
