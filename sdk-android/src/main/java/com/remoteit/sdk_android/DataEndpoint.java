package com.remoteit.sdk_android;

import com.remoteit.sdk_android.protocol.Endpoint;
import com.remoteit.sdk_android.protocol.EndpointConnectionStatus;
import com.remoteit.sdk_android.protocol.EndpointConnectionStatusHandler;
import com.remoteit.sdk_android.protocol.EndpointDataHandler;
import com.remoteit.sdk_android.protocol.EndpointSendDataAckHandler;
import com.remoteit.sdk_android.protocol.Packet;
import com.remoteit.sdk_android.protocol.PacketBuilder;
import com.remoteit.sdk_android.protocol.PacketPayload;
import com.remoteit.sdk_android.protocol.PacketType;
import com.remoteit.sdk_android.protocol.ProtocolStep;
import com.remoteit.sdk_android.protocol.RawPacket;
import com.remoteit.sdk_android.protocol.Session;
import com.remoteit.sdk_android.transport.Transport;
import com.remoteit.sdk_android.transport.TransportConnectionStatus;

import timber.log.Timber;

public class DataEndpoint implements Endpoint {
    private final static String LogTag = DataEndpoint.class.toString();

    private final Session session;
    private ProtocolStep expectFromPeer;

    private Transport transport;

    private EndpointConnectionStatusHandler endpointConnectionStatusHandler;
    private EndpointSendDataAckHandler endpointSendDataAckHandler;
    private EndpointDataHandler endpointDataHandler;

    public DataEndpoint(Session session) {
        this.session = session;
        this.expectFromPeer = ProtocolStep.None;
        this.transport = null;
    }

    @Override
    public void Connect() {
        if (this.transport != null) {
            this.transport.Connect();
        }
    }

    @Override
    public void Disconnect() {
        this.expectFromPeer = ProtocolStep.None;

        if (this.transport != null) {
            this.transport.Disconnect();
        }
    }

    @Override
    public void OnEndpointConnectionStatusChanged(EndpointConnectionStatusHandler endpointConnectionStatusHandler) {
        this.endpointConnectionStatusHandler = endpointConnectionStatusHandler;
    }

    @Override
    public void RequestSendData(long payloadID, byte[] data) {
        if (this.expectFromPeer != ProtocolStep.Data) {
            return;
        }

        this.expectFromPeer = ProtocolStep.DataAck;
        Packet packet = new Packet(PacketType.Data, ProtocolStep.Data, payloadID, new PacketPayload(this.session, data));
        this.transport.SendTransportData(PacketBuilder.Pack(PacketBuilder.PacketToRawPacket(packet)));
    }

    @Override
    public void OnRequestSendDataAck(EndpointSendDataAckHandler endpointSendDataAckHandler) {
        this.endpointSendDataAckHandler = endpointSendDataAckHandler;
    }

    @Override
    public void OnEndpointDataIn(EndpointDataHandler endpointDataHandler) {
        this.endpointDataHandler = endpointDataHandler;
    }

    @Override
    public void SetTransport(Transport transport) {
        this.transport = transport;
        this.transport.OnTransportConnectionStatusChanged(this::handleTransportConnectionStatus);
        this.transport.OnTransportDataIn(this::handleTransportData);
    }

    @Override
    public Session GetSession() {
        return this.session;
    }

    private void handleTransportConnectionStatus(TransportConnectionStatus transportConnectionStatus) {
        if (this.endpointConnectionStatusHandler != null) {
            switch (transportConnectionStatus) {
                case Disconnected:
                    handleTransportConnectionStatus_Disconnected();
                    break;

                case Connecting:
                    handleTransportConnectionStatus_Connecting();
                    break;

                case Connected:
                    handleTransportConnectionStatus_Connected();
                    break;
            }
        }
    }

    private void handleTransportConnectionStatus_Disconnected() {
        Timber.d("Disconnected");

        this.expectFromPeer = ProtocolStep.None;

        this.endpointConnectionStatusHandler.HandleEndpointConnectionStatus(EndpointConnectionStatus.Disconnected);
    }

    private void handleTransportConnectionStatus_Connecting() {
        Timber.d("Connecting");

        this.expectFromPeer = ProtocolStep.None;

        this.endpointConnectionStatusHandler.HandleEndpointConnectionStatus(EndpointConnectionStatus.Connecting);
    }

    private void handleTransportConnectionStatus_Connected() {
        Timber.d("Connected");

        this.expectFromPeer = ProtocolStep.Pong;
        Packet packet = new Packet(PacketType.Data, ProtocolStep.Ping, RawPacket.IgnorePacketID, new PacketPayload(session, null));
        this.transport.SendTransportData(PacketBuilder.Pack(PacketBuilder.PacketToRawPacket(packet)));
    }

    private void handleTransportData(byte[] data) {
        Packet packet = PacketBuilder.RawPacketToPacket(PacketBuilder.UnPack(data));
        if (packet.Type != PacketType.Data) {
            Timber.d(LogTag, "WRONG-PACKET-TYPE: %s%s", packet.Type.toString());
            return;
        }

        switch (packet.Step) {
            case Ping:
                this.handleTransportData_Ping(packet);
                break;
            case Pong:
                this.handleTransportData_Pong(packet);
                break;
            case Data:
                this.handleTransportData_Data(packet);
                break;
            case DataAck:
                this.handleTransportData_DataAck(packet);
                break;
        }
    }

    private void handleTransportData_Ping(Packet packet) {
        Packet replyPacket = new Packet(PacketType.Data, ProtocolStep.Pong, packet.ID, new PacketPayload(this.session, null));
        this.transport.SendTransportData(PacketBuilder.Pack(PacketBuilder.PacketToRawPacket(replyPacket)));
    }

    private void handleTransportData_Pong(Packet packet) {
        if (this.expectFromPeer == ProtocolStep.Pong) {
            this.expectFromPeer = ProtocolStep.Data;
            Packet replyPacket = new Packet(PacketType.Data, ProtocolStep.Pong, packet.ID, new PacketPayload(this.session, null));
            this.transport.SendTransportData(PacketBuilder.Pack(PacketBuilder.PacketToRawPacket(replyPacket)));

            this.endpointConnectionStatusHandler.HandleEndpointConnectionStatus(EndpointConnectionStatus.Connected);
        }
    }

    private void handleTransportData_Data(Packet packet) {
        new Thread(() -> {
            Packet replyPacket = new Packet(PacketType.Data, ProtocolStep.DataAck, packet.ID, new PacketPayload(this.session, null));
            this.transport.SendTransportData(PacketBuilder.Pack(PacketBuilder.PacketToRawPacket(replyPacket)));
        }).start();

        if (this.endpointDataHandler != null) {
            this.endpointDataHandler.HandleEndpointData(packet.Payload.Data);
        }
    }

    private void handleTransportData_DataAck(Packet packet) {
        this.expectFromPeer = ProtocolStep.Data;

        if (this.endpointSendDataAckHandler != null) {
            this.endpointSendDataAckHandler.HandleEndpointSendDataAck(packet.ID);
        }
    }
}
