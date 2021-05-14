package com.remoteit.sdk_android.protocol;

import com.google.gson.annotations.SerializedName;

public class PacketPayload {
	@SerializedName("session")
	public Session Session;

	@SerializedName("data")
	public byte[] Data;

	public PacketPayload(Session Session, byte[] data) {
		this.Session = Session;
		this.Data = data;
	}
}
