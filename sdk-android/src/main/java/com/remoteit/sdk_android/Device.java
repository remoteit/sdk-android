package com.remoteit.sdk_android;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Base64;

import com.remoteit.sdk_android.helpers.Helpers;
import com.remoteit.sdk_android.networking.ProServerSocket;
import com.remoteit.sdk_android.protocol.Endpoint;
import com.remoteit.sdk_android.protocol.Packet;
import com.remoteit.sdk_android.protocol.PacketBuilder;
import com.remoteit.sdk_android.protocol.RawPacket;
import com.remoteit.sdk_android.remoteit.API;
import com.remoteit.sdk_android.remoteit.ConnectD;
import com.remoteit.sdk_android.remoteit.LoginResponse;
import com.remoteit.sdk_android.remoteit.RegisteredService;
import com.remoteit.sdk_android.transport.ProxyTransport;
import com.remoteit.sdk_android.transport.StreamsTransport;
import com.remoteit.sdk_android.transport.TransportConnectionStatus;
import com.remoteit.sdk_android.transport.TransportConnectionStatusHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.remoteit.sdk_android.networking.Helpers.MAX_DATA_LENGTH;

public class Device {
    private final static String LogTag = Device.class.toString();

    private static final int SERVICE_PORT = 65500;
    private static Device device;
    private DeviceConfig config;
    private ConnectD connectdDevice;
    private ConnectD connectdTransport;
    private ProServerSocket proServerSocket;
    private final List<DataEndpoint> pendingDataEndpoints;
    private NewPeerConnectedHandler newPeerConnectedHandler;

    private Device() {
        this.config = new DeviceConfig();
        this.pendingDataEndpoints = new ArrayList<>();
    }

    public static Device Instance() {
        if (device == null) {
            device = new Device();
        }
        return device;
    }

    public static void InitFromConfig(DeviceConfig config) {
        device = new Device();
        device.config = config;
    }

    public DeviceConfig Config() {
        return this.config;
    }

    public void Register(LoginResponse loginResponse, String deviceName) {
        RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
        RegisteredService registeredService = API.RegisterService(loginResponse, registeredDevice);

        this.config.R3DeviceID = registeredDevice.UID;
        this.config.R3DeviceName = registeredDevice.Name;
        this.config.R3DeviceSecret = registeredDevice.Secret;

        this.config.R3OneServiceID = registeredService.UID;
        this.config.R3OneServiceSecret = registeredService.Secret;
    }

    public void UnRegister(LoginResponse loginResponse) {
        RegisteredService registeredService = new RegisteredService();
        registeredService.UID = this.config.R3OneServiceID;
        API.RemoveService(loginResponse, registeredService);

        RegisteredService registeredDevice = new RegisteredService();
        registeredDevice.UID = this.config.R3DeviceID;
        API.UnregisterDevice(loginResponse, registeredDevice);
    }

    @SuppressLint("DefaultLocale")
    public void BringOnline(TransportConnectionStatusHandler connectionStatusHandler, Context context) {
//        FIXME: see if this is needed, have to add kill background tasks permissions - which can be a problem

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            activityManager.killBackgroundProcesses("libconnectd.so"); // getPackageName()
            //activityManager.forceStopPackage(PACKAGE_NAME);
            List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo task : tasks) {
                Timber.e(task.toString());
            }
        }

        // start device service - marks device online on remoteit servers
        new Thread(() -> {
            try {
                List<String> connectdArgs = new ArrayList<>();

                connectdArgs.add("max_depth 35");
                connectdArgs.add(String.format("application_type %d", API.BulkServiceID));
                connectdArgs.add(String.format("proxy_dest_ip %s", "127.0.0.1"));
                connectdArgs.add(String.format("manufacture_id %d", API.ManufactureID));
                connectdArgs.add(String.format("platform_version %d", API.PlatformID));

                if (false) {
                    // if logic.GetServiceProtocol(service.Type) == logic.UDP {
                    //	data = append(data,
                    //			fmt.Sprintf("proxy_local_udp_port %d", service.Port),
                    //			fmt.Sprintf("proxy_dest_udp_port %d", service.Port),
                    //			)

                } else {
                    connectdArgs.add(String.format("proxy_local_port %d", 65535));
                    connectdArgs.add(String.format("proxy_dest_port %d", 65535));
                }

                connectdArgs.add(String.format("application_type_overload %d", 40));

                connectdArgs.add(String.format("UID %s", this.config.R3DeviceID));
                connectdArgs.add(String.format("secret %s", this.config.R3DeviceSecret));

                connectdArgs.add("#");

                StringBuilder sb = new StringBuilder();
                for (String val : connectdArgs) {
                    sb.append(val).append("\n");
                }

                String connectdArgsAsString = sb.toString();
                byte[] connectdArgsAsBytes = connectdArgsAsString.getBytes();
                connectdArgsAsString = new String(Base64.encode(connectdArgsAsBytes, Base64.NO_WRAP));
                connectdArgsAsString = String.format("-s -e %s", connectdArgsAsString);

                this.connectdDevice = new ConnectD();

                this.connectdDevice.Run(context, connectdArgsAsString, connectionStatus -> {
                    if (connectionStatus == TransportConnectionStatus.Disconnected) {
                        GoOffline();

                        if (connectionStatusHandler != null) {
                            connectionStatusHandler.HandleTransportConnectionStatus(connectionStatus);
                        }
                    } else if (connectionStatus == TransportConnectionStatus.Connected) {
                        startTargetPipe(connectionStatusHandler, context);
                        startLocalSocketServer();
                    }
                });
            } catch (Exception e) {
                Helpers.ReportException(LogTag, e);
            }
        }).start();
    }

    @SuppressLint("DefaultLocale")
    private void startTargetPipe(TransportConnectionStatusHandler connectionStatusHandler, Context context) {

        // start tunnel/data transport service - marks service available on remoteit servers
        new Thread(() -> {
            try {
                List<String> connectdArgs = new ArrayList<>();

                connectdArgs.add("max_depth 35");
                connectdArgs.add(String.format("application_type %d", API.TCPServiceID));
                connectdArgs.add(String.format("proxy_dest_ip %s", "127.0.0.1"));
                connectdArgs.add(String.format("manufacture_id %d", API.ManufactureID));
                connectdArgs.add(String.format("platform_version %d", API.PlatformID));

                if (false) {
                    // if logic.GetServiceProtocol(service.Type) == logic.UDP {
                    //	data = append(data,
                    //			fmt.Sprintf("proxy_local_udp_port %d", service.Port),
                    //			fmt.Sprintf("proxy_dest_udp_port %d", service.Port),
                    //			)
                } else {
                    connectdArgs.add(String.format("proxy_local_port %d", SERVICE_PORT));
                    connectdArgs.add(String.format("proxy_dest_port %d", SERVICE_PORT));
                }

//			connectdArgs.add(String.format("application_type_overload %d", 40));

                connectdArgs.add(String.format("UID %s", this.config.R3OneServiceID));
                connectdArgs.add(String.format("secret %s", this.config.R3OneServiceSecret));

                connectdArgs.add("#");

                StringBuilder sb = new StringBuilder();
                for (String val : connectdArgs) {
                    sb.append(val).append("\n");
                }

                String connectdArgsAsString = sb.toString();
                byte[] connectdArgsAsBytes = connectdArgsAsString.getBytes();
                connectdArgsAsString = new String(Base64.encode(connectdArgsAsBytes, Base64.NO_WRAP));
                connectdArgsAsString = String.format("-s -e %s", connectdArgsAsString);

                this.connectdTransport = new ConnectD();
                this.connectdTransport.Run(context, connectdArgsAsString, connectionStatus -> {
                    if (connectionStatus == TransportConnectionStatus.Disconnected) {
                        GoOffline();

                        if (connectionStatusHandler != null) {
                            connectionStatusHandler.HandleTransportConnectionStatus(connectionStatus);
                        }
                    } else {
                        connectionStatusHandler.HandleTransportConnectionStatus(connectionStatus);
                    }
                });
            } catch (Exception e) {
                Helpers.ReportException(LogTag, e);
            }
        }).start();
    }

    private void startLocalSocketServer() {

        // listen on 127.0.0.1:SERVICE_PORT - connectd will forward traffic to this socket
        this.proServerSocket = new ProServerSocket(SERVICE_PORT, this::handleNewConnection);
        proServerSocket.Run(exception -> Helpers.ReportException(LogTag, exception));
    }

    public void GoOffline() {
        if (this.connectdDevice != null) {
            this.connectdDevice.Stop();
        }

        if (this.connectdTransport != null) {
            this.connectdTransport.Stop();
        }

        if (this.proServerSocket != null) {
            this.proServerSocket.Stop();
        }
    }

    public Endpoint ConnectToDevice(Endpoint endpoint, String destinationR3DeviceID) {
        endpoint.SetTransport(new ProxyTransport(destinationR3DeviceID));
        endpoint.Connect();
        return endpoint;
    }

    public void DisconnectFromDevice(Endpoint endpoint) {
        if (endpoint != null) {
            endpoint.Disconnect();
        }
    }

    @SuppressLint("LogNotTimber")
    private void handleNewConnection(InputStream inputStream, OutputStream outputStream) {
        try {
            // 1. read first bytes
            byte[] buffer = new byte[MAX_DATA_LENGTH];
            int readNrOfBytes = inputStream.read(buffer, 0, MAX_DATA_LENGTH);
            byte[] actualData = new byte[readNrOfBytes];
            System.arraycopy(buffer, 0, actualData, 0, readNrOfBytes);

            // 2. decide who will handle
            RawPacket rawPacket = PacketBuilder.UnPack(actualData);
            Packet packet = PacketBuilder.RawPacketToPacket(rawPacket);

            switch (packet.Type) {
                case Data:
                    DataEndpoint dataEndpoint = new DataEndpoint(packet.Payload.Session);
                    dataEndpoint.SetTransport(new StreamsTransport(inputStream, outputStream));
                    pendingDataEndpoints.add(dataEndpoint);

                    if (newPeerConnectedHandler != null) {
                        newPeerConnectedHandler.OnNewPeerConnected(dataEndpoint);
                    }
                    break;
                default:
                    Timber.d(LogTag, "handleNewConnection() - UNKNOWN packet type %d", packet.Type.toString());
                    break;
                case None:
                    break;
            }
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
        }
    }

    public void OnNewPeerConnected(NewPeerConnectedHandler newPeerConnectedHandler) {
        this.newPeerConnectedHandler = newPeerConnectedHandler;
    }

    public List<DataEndpoint> GetPendingChatEndpoints() {
        synchronized (this) {
            return this.pendingDataEndpoints;
        }
    }

    public void RemovePendingChatEndpoint(DataEndpoint chatEndpoint) {
        synchronized (this) {
            this.pendingDataEndpoints.remove(chatEndpoint);
        }
    }
}
