package com.remoteit.sdk_android.transport;

import com.remoteit.sdk_android.networking.Helpers;
import com.remoteit.sdk_android.remoteit.API;
import com.remoteit.sdk_android.remoteit.LoginResponse;
import com.remoteit.sdk_android.remoteit.R3CreateProxyResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static com.remoteit.sdk_android.helpers.Helpers.ReportException;

public class ProxyTransport implements Transport {
    private final static String LogTag = ProxyTransport.class.toString();

    private final String r3destinationDevice;
    private LoginResponse loginResponse;
    private R3CreateProxyResponse createProxyResponse;

    private InputStream inputStream;
    private OutputStream outputStream;

    private TransportConnectionStatus transportConnectionStatus;

    private TransportConnectionStatusHandler transportConnectionStatusHandler;
    private TransportDataHandler transportDataHandler;

    public ProxyTransport(String r3destinationDevice) {
        this.r3destinationDevice = r3destinationDevice;

        this.inputStream = null;
        this.outputStream = null;

        this.transportConnectionStatus = TransportConnectionStatus.Disconnected;
    }

    @Override
    public void Connect() {
        if (this.transportConnectionStatus != TransportConnectionStatus.Disconnected) {
            return;
        }

        new Thread(() -> {
            try {
                updateConnectionStatus(TransportConnectionStatus.Connecting);

                //this.loginResponse = API.LoginWithUserPass(API.r3AccountName, API.r3AccountPass);
                this.createProxyResponse = API.CreateProxy(this.r3destinationDevice, this.loginResponse);
                if (createProxyResponse == null
                        || createProxyResponse.ProxyInfo == null
                        || createProxyResponse.ProxyInfo.ProxyServer == null
                        || createProxyResponse.ProxyInfo.ProxyServer.isEmpty()
                ) {
                    updateConnectionStatus(TransportConnectionStatus.Disconnected);
                    return;
                }

                InetAddress serverAddr = InetAddress.getByName(createProxyResponse.ProxyInfo.ProxyServer);
                Socket clientSocket = new Socket(serverAddr, Integer.parseInt(createProxyResponse.ProxyInfo.ProxyPort));

                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                updateConnectionStatus(TransportConnectionStatus.Connected);

                Helpers.ReadFromStream(inputStream, (data) -> {
                    if (transportDataHandler != null) {
                        transportDataHandler.HandleTransportData(data);
                    }
                });

                this.Disconnect();

            } catch (Exception e) {
                ReportException(LogTag, e);
                this.Disconnect();
            }
        }).start();
    }

    @Override
    public void Disconnect() {
        try {
            if (this.inputStream != null) {
                this.inputStream.close();
            }
            if (this.outputStream != null) {
                this.outputStream.close();
            }

            API.DeleteProxy(this.loginResponse, this.createProxyResponse);
            updateConnectionStatus(TransportConnectionStatus.Disconnected);

        } catch (IOException e) {
            ReportException(LogTag, e);
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
            ReportException(LogTag, e);
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
