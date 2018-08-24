package com.ota.ibeacon4android;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ota.ibeacon4android.fw.BeaconAdapter;
import com.ota.ibeacon4android.fw.BeaconData;

public class BleListDispActivity extends Activity {

	private static String TAG = "com.ota.ibeacon4android";
	private ListView beaconlistView;
	private BluetoothAdapter mBluetoothAdapter;
	private Handler uihandler;
	private BleListDispActivity me;
	private List<BeaconData> beacons;
	private ProgressBar findingProgressbar;
	private TextView headerTxtView;
	private boolean isFinding;
	private final int START_MENU = Menu.FIRST;
	private final int STOP_MENU = Menu.FIRST + 1;
	MenuItem actionItemStart;
	MenuItem actionItemStop;

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (scanRecord.length > 30) {
				//
				// 共通処理
				//
				// デバイス名
				String beaconName;
				if (device.getName() == null || "".equals(device.getName()) || " ".equals(device.getName())) {
					beaconName = "Undefined";
				} else {
					beaconName = device.getName();
				}
				// UUID
				String uuid = IntToHex(scanRecord[9] & 0xff) + IntToHex(scanRecord[10] & 0xff) + IntToHex(scanRecord[11] & 0xff) + IntToHex(scanRecord[12] & 0xff)
						+ "-" + IntToHex(scanRecord[13] & 0xff) + IntToHex(scanRecord[14] & 0xff) + "-" + IntToHex(scanRecord[15] & 0xff)
						+ IntToHex(scanRecord[16] & 0xff) + "-" + IntToHex(scanRecord[17] & 0xff) + IntToHex(scanRecord[18] & 0xff) + "-"
						+ IntToHex(scanRecord[19] & 0xff) + IntToHex(scanRecord[20] & 0xff) + IntToHex(scanRecord[21] & 0xff) + IntToHex(scanRecord[22] & 0xff)
						+ IntToHex(scanRecord[23] & 0xff) + IntToHex(scanRecord[24] & 0xff);
				// Beacon Mode
				List<String> beaconModes  = getDeviceMode(scanRecord[2] & 0xff);
				String major;
				String minor;
				int stddBm;
				double distance;
				boolean isIBeacon;
				
				//
				// iBeaconの場合
				//
				if ((scanRecord[5] == (byte) 0x4c) && (scanRecord[6] == (byte) 0x00) && (scanRecord[7] == (byte) 0x02) && (scanRecord[8] == (byte) 0x15)) {
					isIBeacon = true;
					
					// 距離
					stddBm = scanRecord[29];
					distance = Math.pow(Math.pow(10, stddBm - rssi), 1d / 20d);
					
					// Beacon Mode
					//beaconModes = getDeviceMode(scanRecord[2] & 0xff);

					//Major/Minor
					major = IntToHex(scanRecord[25] & 0xff) + IntToHex(scanRecord[26] & 0xff);
					minor = IntToHex(scanRecord[27] & 0xff) + IntToHex(scanRecord[28] & 0xff);
				} else {
					isIBeacon = false;
					//beaconModes = new ArrayList<String>();
					major = "0";
					minor = "0";
					stddBm = 0;
					distance = 0;
				}
				class UIUpdater extends Thread {
					private List<BeaconData> _beacons;
					public UIUpdater(List<BeaconData> beacons) {
						_beacons = beacons;
					}
					public void run() {
						BeaconAdapter beaconAdapater = new BeaconAdapter(me, 0, _beacons);
						beaconlistView.setAdapter(beaconAdapater);
					}
				}
				synchronized (BleListDispActivity.class) {
					if (!isExiting(uuid)) {
						BeaconData item = new BeaconData();
						item.setBeaconName(beaconName);
						item.setUuid(uuid);
						item.setMajor(Integer.valueOf(Integer.parseInt(major, 16)).toString());
						item.setMinor(Integer.valueOf(Integer.parseInt(minor, 16)).toString());
						item.setDistance(Math.round(distance * 100));
						item.setRssi(rssi);
						item.setMode(getModeStr(beaconModes));
						item.setTxpower(stddBm);
						item.setIBeacon(isIBeacon);
						//item.setUuids(getUUIDArray(device.getUuids()));
						beacons.add(item);
						uihandler.post(new UIUpdater(beacons));
					}
				}

			}
		}
	};

	private boolean isExiting(String uuid) {
		for (int i = 0; i < beacons.size(); i++) {
			if (uuid.equals(beacons.get(i).getUuid())) {
				return true;
			}
		}
		return false;
	};

	private String getModeStr(List<String> list) {
		if (list.size() == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				sb.append("\n");
			}
			sb.append(list.get(i));
		}
		return sb.toString();
	}

	private String IntToHex(int i) {
		char hex_2[] = { Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16) };
		String hex_2_str = new String(hex_2);
		return hex_2_str.toUpperCase();
	}

	private String byteToHex(byte[] by) {
		StringBuffer sb = new StringBuffer();
		for (int b : by) {
			sb.append(Character.forDigit(b >> 4 & 0xF, 16));
			sb.append(Character.forDigit(b & 0xF, 16));
		}
		return new String(sb);
	}

	private List<String> getDeviceMode(int i) {
		List<String> ret = new ArrayList<String>();
		String bin = Integer.toBinaryString(i);
		int padding = 8 - bin.length();
		for (int j = 0; j < padding; j++) {
			bin = "0" + bin;
		}
		if ("1".equals(bin.substring(7, 8))) {
			ret.add("LE Limited Discoverable Mode");
		}
		if ("1".equals(bin.substring(6, 7))) {
			ret.add("LE General Discoverable Mode");
		}
		if ("1".equals(bin.substring(5, 6))) {
			ret.add("BR/EDR Not Supported");
		}
		if ("1".equals(bin.substring(4, 5))) {
			ret.add("Simultaneous LE and BR/EDR to Same Device Capable (Controller)");
		}
		if ("1".equals(bin.substring(3, 4))) {
			ret.add("Simultaneous LE and BR/EDR to Same Device Capable (Host)");
		}
		return ret;
	}

	private String getProximity(double distance) {
		if (distance < 0.2d) {
			return "immediate";
		} else if (distance <= 2d) {
			return "near";
		} else {
			return "far";
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Init
		setContentView(R.layout.activity_main);
		me = this;
		beacons = new ArrayList<BeaconData>();
		uihandler = new Handler();
		isFinding = false;

		// Bluetooth related procedure
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Set listView
		beaconlistView = (ListView) findViewById(R.id.beacon_List);
		View headerview = (View) getLayoutInflater().inflate(R.layout.beacon_listdata_header, null);
		beaconlistView.addHeaderView(headerview, null, false);
		findingProgressbar = (ProgressBar) findViewById(R.id.finding_peripherals_progressbar);
		findingProgressbar.setVisibility(View.GONE);
		headerTxtView = (TextView) findViewById(R.id.headerTxtView);

		BeaconAdapter beaconAdapater = new BeaconAdapter(me, 0, beacons);
		beaconlistView.setAdapter(beaconAdapater);
		beaconlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// String item = (String)
				// listView.getItemAtPosition(position);
				if (isFinding) {
					Toast.makeText(BleListDispActivity.this, "Tap peripheral, after stopping search.", Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent();
					BeaconData beacondata = beacons.get(position - 1);
					beacondata.setNumStr(Integer.toString(position));
					intent.putExtra("beacondata", beacondata);
					intent.setClassName("com.ota.ibeacon4android", "com.ota.ibeacon4android.BleDetailDispActivity");
					startActivity(intent);
				}

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		actionItemStart = menu.add(Menu.NONE, START_MENU, Menu.NONE, "Start");
		actionItemStart.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		actionItemStop = menu.add(Menu.NONE, STOP_MENU, Menu.NONE, "Stop");
		actionItemStop.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		actionItemStop.setEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case START_MENU:
			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				Toast.makeText(BleListDispActivity.this, "BLE is not supported.", Toast.LENGTH_SHORT).show();
			} else {
				beacons = new ArrayList<BeaconData>();
				BeaconAdapter beaconAdapater = new BeaconAdapter(me, 0, beacons);
				beaconlistView.setAdapter(beaconAdapater);
				mBluetoothAdapter.startLeScan(mLeScanCallback);
				startFindingMode();				
			}
			break;
		case STOP_MENU:
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			stopFindingMode();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private UUID[] getUUIDArray(ParcelUuid[] puuids) {
		UUID[] uuids = new UUID[puuids.length + 1];
		int i = 0;
		for (ParcelUuid puuid: puuids) {
			uuids[i] = puuid.getUuid();
			i++;
		}
		return uuids;
	}
	
	private void startFindingMode() {
		findingProgressbar.setVisibility(View.VISIBLE);
		headerTxtView.setText(" Now Finding Peripherals");
		actionItemStart.setEnabled(false);
		actionItemStop.setEnabled(true);
		isFinding = true;
	}

	private void stopFindingMode() {
		findingProgressbar.setVisibility(View.GONE);
		int i = beacons.size();
		if (i == 0) {
			headerTxtView.setText(" No Peripherals Could Be Found");
		} else if (i == 1) {
			headerTxtView.setText(" 1 Peripheral Could Be Found");
		} else {
			headerTxtView.setText(" " + i + " Peripherals Could Be Found");			
		}
		actionItemStart.setEnabled(true);
		actionItemStop.setEnabled(false);
		isFinding = false;
	}
}
