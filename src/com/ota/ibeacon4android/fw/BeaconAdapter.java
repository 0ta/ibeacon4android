package com.ota.ibeacon4android.fw;

import java.util.List;

import com.ota.ibeacon4android.R;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BeaconAdapter extends ArrayAdapter<BeaconData> {

	private LayoutInflater _layoutInflater;
	private Context _context;
	private int mLastAnimationPosition = 0;
	public BeaconAdapter(Context context, int textViewResourceId, List<BeaconData> objects) {
		super(context, textViewResourceId, objects);
		_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_context = context;
	}

	 @Override
	 public View getView(int position, View convertView, ViewGroup parent) {
		 System.out.println("Position:" + position);
		 // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
		 if (null == convertView) {
			 convertView = _layoutInflater.inflate(R.layout.beacon_listdata, null);
		 }
		 // 特定の行(position)のデータを得る
		 BeaconData item = (BeaconData) getItem(position);
		 if (item != null) {
			 // BeaconDataのデータをViewの各Widgetにセットする
			 Button beaconNumButton;
			 beaconNumButton = (Button)convertView.findViewById(R.id.beacon_num_btn);
			 beaconNumButton.setText(Integer.toString(position + 1));
			 if (!item.isIBeacon()) {
				 beaconNumButton.setBackground(convertView.getResources().getDrawable(R.drawable.circle_number_notbeacon));
			 }
			 
			 ImageView beaconImg;
			 beaconImg = (ImageView) convertView.findViewById(R.id.ibeaconImgView);
			 if (!item.isIBeacon()) {
				 beaconImg.setVisibility(ImageView.INVISIBLE);
			 }
			 
			 TextView beaconNametext;
			 beaconNametext = (TextView)convertView.findViewById(R.id.beacon_name);
			 beaconNametext.setText(item.getBeaconName());
			 
			 TextView uuidtext;
			 uuidtext = (TextView)convertView.findViewById(R.id.beacon_uuid);
			 uuidtext.setText(item.getUuid());

			 TextView majortext;
			 majortext = (TextView)convertView.findViewById(R.id.beacon_major);
			 if (item.isIBeacon()) {
				 majortext.setText(item.getMajor());
			 } else {
				 majortext.setText("-");
			 }
			 
			 TextView minortext;
			 minortext = (TextView)convertView.findViewById(R.id.beacon_minor);
			 if (item.isIBeacon()) {
				 minortext.setText(item.getMinor());
			 } else {
				 minortext.setText("-");
			 }
			 
			 TextView rssitext;
			 rssitext = (TextView)convertView.findViewById(R.id.rssi_num);
			 rssitext.setText(Integer.toString(item.getRssi()));
			 
			 long ldistance = item.getDistance();
			 TextView distancenumtext;
			 distancenumtext = (TextView)convertView.findViewById(R.id.distance_num);
			 TextView distanceunittext;
			 distanceunittext = (TextView)convertView.findViewById(R.id.distance_unit);
			 if (!item.isIBeacon()) {
				 distancenumtext.setText("- ");
				 distanceunittext.setText("m");
			 } else if (ldistance <= 100) {
				 distancenumtext.setText(Long.toString(ldistance));
				 distanceunittext.setText("cm");
			 } else {
				 distancenumtext.setText(Long.toString(Math.round(ldistance / 100)));
				 distanceunittext.setText("m");				 
			 }
		 }
//		 if (mLastAnimationPosition < position) {
//		     Animation animation = AnimationUtils.loadAnimation(_context, R.anim.list_motion);
//		     convertView.startAnimation(animation);
//		     mLastAnimationPosition = position;
//		 }		 
		 return convertView;
	 }
}
