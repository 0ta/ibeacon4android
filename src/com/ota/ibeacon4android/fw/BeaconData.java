package com.ota.ibeacon4android.fw;

import java.io.Serializable;
import java.util.UUID;

public class BeaconData implements Serializable {
	public String getBeaconName() {
		return beaconName;
	}
	public void setBeaconName(String beaconName) {
		this.beaconName = beaconName;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getMinor() {
		return minor;
	}
	public void setMinor(String minor) {
		this.minor = minor;
	}
	public long getDistance() {
		return distance;
	}
	public void setDistance(long distance) {
		this.distance = distance;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getBinary() {
		return binary;
	}
	public void setBinary(String binary) {
		this.binary = binary;
	}
	public String getNumStr() {
		return numStr;
	}
	public void setNumStr(String numStr) {
		this.numStr = numStr;
	}
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	public int getTxpower() {
		return txpower;
	}
	public void setTxpower(int txpower) {
		this.txpower = txpower;
	}
	public boolean isIBeacon() {
		return isIBeacon;
	}
	public void setIBeacon(boolean isIBeacon) {
		this.isIBeacon = isIBeacon;
	}
	public UUID[] getUuids() {
		return uuids;
	}
	public void setUuids(UUID[] uuids) {
		this.uuids = uuids;
	}
	private String numStr;
	private String beaconName;
	private String uuid;
	private String major;
	private String minor;
	private String mode;
	private int txpower;
	private String binary;
	private int rssi;
	private long distance;
	private boolean isIBeacon;
	private UUID[] uuids;
}
