package com.ota.ibeacon4android;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ota.ibeacon4android.fw.BeaconData;
import com.ota.ibeacon4android.fw.BeaconDetectiveCircle;

public class BleDetailDispActivity extends Activity {

	private static String TAG = "com.ota.ibeacon4android";
	private BeaconDetectiveCircle beaconDetectiveCircleView;
	private Handler uihandler;
	private byte[] binaries;
	private BeaconData beacondata;
	private long lastchecktime;
	private boolean isIBeacon;
	private boolean isLost;
	private BluetoothAdapter mBluetoothAdapter;

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			// ここに結果に対して行う処理を記述する
			if (scanRecord.length > 30) {

				String uuid = IntToHex(scanRecord[9] & 0xff) + IntToHex(scanRecord[10] & 0xff) + IntToHex(scanRecord[11] & 0xff) + IntToHex(scanRecord[12] & 0xff) + "-"
						+ IntToHex(scanRecord[13] & 0xff) + IntToHex(scanRecord[14] & 0xff) + "-" + IntToHex(scanRecord[15] & 0xff) + IntToHex(scanRecord[16] & 0xff)
						+ "-" + IntToHex(scanRecord[17] & 0xff) + IntToHex(scanRecord[18] & 0xff) + "-" + IntToHex(scanRecord[19] & 0xff)
						+ IntToHex(scanRecord[20] & 0xff) + IntToHex(scanRecord[21] & 0xff) + IntToHex(scanRecord[22] & 0xff) + IntToHex(scanRecord[23] & 0xff)
						+ IntToHex(scanRecord[24] & 0xff);

				if (beacondata.getUuid().equals(uuid)) {
					binaries = scanRecord;
					lastchecktime = new Date().getTime();
					isLost = false;
					beaconDetectiveCircleView.setLost(false);

					int stddBm;
					double distance;
					//
					// iBeaconの場合
					//
					if ((scanRecord[5] == (byte) 0x4c) && (scanRecord[6] == (byte) 0x00) && (scanRecord[7] == (byte) 0x02) && (scanRecord[8] == (byte) 0x15)) {
						stddBm = scanRecord[29];
						distance = Math.pow(Math.pow(10, stddBm - rssi), 1d / 20d);
						//distance = Math.pow(10d, ((double) stddBm - rssi) / (10 * 2));
					} else {
						stddBm = 0;
						distance = 0;
					}
					class UIUpdater extends Thread {
						private int _rssi;
						private int _txpower;
						private long _distance;

						public UIUpdater(int rssi, int txpower, long distance) {
							_rssi = rssi;
							_txpower = txpower;
							_distance = distance;
						}

						public void run() {
							beaconDetectiveCircleView.setIBeacon(isIBeacon);
							beaconDetectiveCircleView.clearData();
							if (isIBeacon) {
								long i, j;
								if (_distance > 10000) {
									i = 100;
									j = 0;
								} else {
									i = Math.round(_distance / 100);
									j = 100 - i;
								}
								beaconDetectiveCircleView.addData(j, Color.parseColor("#3399CC"));
								beaconDetectiveCircleView.addData(i, Color.parseColor("#c3cbd4"));
								beaconDetectiveCircleView.setDistance(_distance);
								beaconDetectiveCircleView.setTxPower(_txpower);
								beaconDetectiveCircleView.setRssi(_rssi);
								beaconDetectiveCircleView.invalidate();
							} else {
								beaconDetectiveCircleView.addData(0, Color.parseColor("#3399CC"));
								beaconDetectiveCircleView.addData(100, Color.parseColor("#c3cbd4"));
								beaconDetectiveCircleView.setDistance(0);
								beaconDetectiveCircleView.setRssi(_rssi);
								beaconDetectiveCircleView.invalidate();
							}
						}
					}
					uihandler.post(new UIUpdater(rssi, stddBm, Math.round(distance * 100)));
				}

			}
		}
	};

	private String byteToHex(byte[] by) {
		StringBuffer sb = new StringBuffer();
		for (int b : by) {
			sb.append(Character.forDigit(b >> 4 & 0xF, 16));
			sb.append(Character.forDigit(b & 0xF, 16));
		}
		return new String(sb);
	}

	private String IntToHex(int i) {
		char hex_2[] = { Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16) };
		String hex_2_str = new String(hex_2);
		return hex_2_str.toUpperCase();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initial
		uihandler = new Handler();
		lastchecktime = new Date().getTime();
		Intent intent = getIntent();
		beacondata = (BeaconData) intent.getSerializableExtra("beacondata");
		isIBeacon = beacondata.isIBeacon();

		// 画面生成
		setContentView(R.layout.activity_sub);
		Button beaconNumButton;
		beaconNumButton = (Button) this.findViewById(R.id.beacon_num_btn);
		beaconNumButton.setText(beacondata.getNumStr());
		if (!beacondata.isIBeacon()) {
			beaconNumButton.setBackground(this.getResources().getDrawable(R.drawable.circle_number_notbeacon));
		}

		TextView uuidtext;
		uuidtext = (TextView) this.findViewById(R.id.beacon_uuid);
		uuidtext.setText(beacondata.getUuid());

		TextView beaconNametext;
		beaconNametext = (TextView) this.findViewById(R.id.beacon_name);
		beaconNametext.setText(beacondata.getBeaconName());

		TextView majortext;
		majortext = (TextView) this.findViewById(R.id.beacon_major);
		if (beacondata.isIBeacon()) {
			majortext.setText(beacondata.getMajor());
		} else {
			majortext.setText("-");
		}

		TextView minortext;
		minortext = (TextView) this.findViewById(R.id.beacon_minor);
		if (beacondata.isIBeacon()) {
			minortext.setText(beacondata.getMinor());
		} else {
			minortext.setText("-");
		}

		TextView modetext;
		modetext = (TextView) this.findViewById(R.id.beacon_mode);
		modetext.setText(beacondata.getMode());
//		if (beacondata.isIBeacon()) {
//			modetext.setText(beacondata.getMode());
//		} else {
//			modetext.setText("-");
//		}

		beaconDetectiveCircleView = (BeaconDetectiveCircle) this.findViewById(R.id.beacon_Ccl);
		beaconDetectiveCircleView.setTxPower(beacondata.getTxpower());
		displayLostStatusView();
		isLost = true;

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		// mBluetoothAdapter.startLeScan(beacondata.getUuids(),
		// mLeScanCallback);
		mBluetoothAdapter.startLeScan(mLeScanCallback);

		// Lost検知Thread実行
		class LostCheckerTask extends TimerTask {
			@Override
			public void run() {
				if (new Date().getTime() - lastchecktime > 5000) {
					uihandler.post(new Runnable() {
						public void run() {
							// Toast.makeText(BleDetailDispActivity.this,
							// "Lost!!!!!!!", Toast.LENGTH_SHORT).show();
							isLost = true;
							displayLostStatusView();
						}
					});
				}
			}
		}
		LostCheckerTask checktask = new LostCheckerTask();
		Timer mTimer = new Timer(true);
		mTimer.schedule(checktask, 1000, 1000);
	}

	private void displayLostStatusView() {
		beaconDetectiveCircleView.setLost(true);
		beaconDetectiveCircleView.clearData();
		beaconDetectiveCircleView.addData(0, Color.parseColor("#3399CC"));
		beaconDetectiveCircleView.addData(100, Color.parseColor("#c3cbd4"));
		beaconDetectiveCircleView.setDistance(0);
		// beaconDetectiveCircleView.setTxPower(_txpower);
		beaconDetectiveCircleView.setRssi(0);
		beaconDetectiveCircleView.invalidate();
		// Toast.makeText(BleDetailDispActivity.this, "Lost!!!!!!!",
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed(){
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	    super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Check HEX");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (isLost) {
			Toast.makeText(this, "Connection Has Been Lost.", Toast.LENGTH_SHORT).show();
		} else if (id == Menu.FIRST) {
			beacondata.setBinary(byteToHex(binaries));
			Intent intent = new Intent();
			intent.putExtra("beacondata", beacondata);
			intent.setClassName("com.ota.ibeacon4android", "com.ota.ibeacon4android.BinaryCheckActivity");
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
}
