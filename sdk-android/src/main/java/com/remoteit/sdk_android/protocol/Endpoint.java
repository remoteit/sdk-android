package com.remoteit.sdk_android.protocol;

import com.remoteit.sdk_android.transport.Transport;

public interface Endpoint {
	void Connect();
	void Disconnect();
	void OnEndpointConnectionStatusChanged(EndpointConnectionStatusHandler endpointConnectionStatusHandler);

	void RequestSendData(long payloadID, byte[] data);
	void OnRequestSendDataAck(EndpointSendDataAckHandler endpointSendDataAckHandler);
	void OnEndpointDataIn(EndpointDataHandler endpointDataHandler);

	void SetTransport(Transport transport);
	Session GetSession();
}
