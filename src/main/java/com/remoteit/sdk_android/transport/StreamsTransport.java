package com.remoteit.sdk_android.transport;

import com.remoteit.sdk_android.networking.Helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamsTransport implements Transport {
	private final static String LogTag = StreamsTransport.class.toString();

	private final InputStream inputStream;
	private final OutputStream outputStream;

	private boolean disconnectOccur;
	private TransportConnectionStatus transportConnectionStatus;

	private TransportConnectionStatusHandler transportConnectionStatusHandler;
	private TransportDataHandler transportDataHandler;

	public StreamsTransport(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;

		this.disconnectOccur = false;
		this.transportConnectionStatus = TransportConnectionStatus.Disconnected;
	}

	@Override
	public void Connect() {
		if (this.transportConnectionStatus != TransportConnectionStatus.Disconnected) {
			return;
		}

		// we can't reconnect to a lost stream
		if (this.disconnectOccur) {
			this.Disconnect();
			return;
		}

		new Thread(()->{
			try{
				// INFO: nothing to do, this is already connected

				updateConnectionStatus(TransportConnectionStatus.Connected);


				Helpers.ReadFromStream(inputStream, (data) -> {
					if (transportDataHandler != null) {
						transportDataHandler.HandleTransportData(data);
					}
				});

				this.Disconnect();

			} catch (Exception e) {
				com.remoteit.sdk_android.helpers.Helpers.ReportException(LogTag, e);
				this.Disconnect();
			}
		}).start();
	}

	@Override
	public void Disconnect() {
		try {
			this.disconnectOccur = true;

			if (this.inputStream != null) {
				this.inputStream.close();
			}
			if (this.outputStream != null) {
				this.outputStream.close();
			}

			updateConnectionStatus(TransportConnectionStatus.Disconnected);

		} catch (IOException e) {
			com.remoteit.sdk_android.helpers.Helpers.ReportException(LogTag, e);
			this.Disconnect();
		}
	}

	@Override
	public void OnTransportConnectionStatusChanged(TransportConnectionStatusHandler transportConnectionStatusHandler) {
		this.transportConnectionStatusHandler = transportConnectionStatusHandler;
	}

	@Override
	public void SendTransportData(byte[] data) {
		try {
			if (this.outputStream != null) {
				this.outputStream.write(data);
				this.outputStream.flush();
			}
		} catch (IOException e) {
			com.remoteit.sdk_android.helpers.Helpers.ReportException(LogTag, e);
			this.Disconnect();
		}
	}

	@Override
	public void OnTransportDataIn(TransportDataHandler transportDataHandler) {
		this.transportDataHandler = transportDataHandler;
	}

	private void updateConnectionStatus(TransportConnectionStatus transportConnectionStatus) {
		this.transportConnectionStatus = transportConnectionStatus;

		if (this.transportConnectionStatusHandler != null) {
			this.transportConnectionStatusHandler.HandleTransportConnectionStatus(transportConnectionStatus);
		}
	}
}
