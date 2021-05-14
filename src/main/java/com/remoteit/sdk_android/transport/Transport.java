package com.remoteit.sdk_android.transport;

public interface Transport {
	void Connect();
	void Disconnect();
	void OnTransportConnectionStatusChanged(TransportConnectionStatusHandler transportConnectionStatusHandler);

	void SendTransportData(byte[] data);
	void OnTransportDataIn(TransportDataHandler transportDataHandler);
}
