package com.remoteit.sdk_android.protocol;

import com.google.gson.annotations.SerializedName;

public class Session {
	@SerializedName("fromUserID")
	public String FromUserID;

	@SerializedName("fromDeviceID")
	public String FromDeviceID;

	@SerializedName("toUserID")
	public String ToUserID;

	@SerializedName("toDeviceID")
	public String ToDeviceID;

	public Session(String fromUserID, String fromDeviceID, String toUserID, String toDeviceID) {
		this.FromUserID = fromUserID;
		this.FromDeviceID = fromDeviceID;
		this.ToUserID = toUserID;
		this.ToDeviceID = toDeviceID;
	}
}
