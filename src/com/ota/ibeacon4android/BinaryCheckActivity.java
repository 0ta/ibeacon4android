package com.ota.ibeacon4android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.ota.ibeacon4android.fw.BeaconData;
import com.ota.ibeacon4android.fw.BeaconDetectiveCircle;

public class BinaryCheckActivity extends Activity {

	private static String TAG = "com.ota.ibeacon4android";
	private BeaconData beacondata;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub_binary);
		
		Intent intent = getIntent();
		beacondata = (BeaconData) intent.getSerializableExtra("beacondata");
		
		TextView beaconNumtext;
		beaconNumtext = (TextView) this.findViewById(R.id.beacon_num_btn);
		beaconNumtext.setText(beacondata.getNumStr());
		if (!beacondata.isIBeacon()) {
			beaconNumtext.setBackground(this.getResources().getDrawable(R.drawable.circle_number_notbeacon));
		}

		TextView uuidtext;
		uuidtext = (TextView) this.findViewById(R.id.beacon_uuid);
		uuidtext.setText(beacondata.getUuid());
		
		TextView beaconNametext;
		beaconNametext = (TextView) this.findViewById(R.id.beacon_name);
		beaconNametext.setText(beacondata.getBeaconName());

		TextView majortext;
		majortext = (TextView) this.findViewById(R.id.beacon_major);
		majortext.setText(beacondata.getMajor());
		
		TextView minortext;
		minortext = (TextView) this.findViewById(R.id.beacon_minor);
		minortext.setText(beacondata.getMinor());
		
		TextView modetext;
		modetext = (TextView) this.findViewById(R.id.beacon_mode);
		modetext.setText(beacondata.getMode());
		
		TextView bintext;
		bintext = (TextView) this.findViewById(R.id.beacon_binary);
		bintext.setText(beacondata.getBinary());
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Send Mail");
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == Menu.FIRST) {
			Uri uri = Uri.parse ("mailto:");  
			Intent intent = new Intent(Intent.ACTION_SENDTO, uri);   
			intent.putExtra(Intent.EXTRA_SUBJECT, beacondata.getUuid());  
			intent.putExtra(Intent.EXTRA_TEXT, createMailText());  
			this.startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private String createMailText() {
		StringBuffer sb = new StringBuffer();
		sb.append("UUID:\n");
		sb.append(beacondata.getUuid() + "\n\n");
		sb.append("Device Name:\n");
		sb.append(beacondata.getBeaconName() + "\n\n");
		if (beacondata.isIBeacon()) {
			sb.append("Major:\n");
			sb.append(beacondata.getMajor() + "\n\n");
			sb.append("Minor:\n");
			sb.append(beacondata.getMinor() + "\n\n");
		}
		sb.append("Mode:\n");
		sb.append(beacondata.getMode() + "\n\n");
		sb.append("HEX:\n");
		sb.append(beacondata.getBinary());
		return sb.toString();
	}
}
