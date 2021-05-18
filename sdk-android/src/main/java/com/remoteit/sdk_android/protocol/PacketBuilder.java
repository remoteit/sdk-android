package com.remoteit.sdk_android.protocol;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.remoteit.sdk_android.helpers.Helpers;

import java.io.StringReader;
import java.nio.ByteBuffer;

public class PacketBuilder {
    private final static String LogTag = PacketBuilder.class.toString();

    public static byte[] Pack(RawPacket rawPacket) {
        int headerSize = 1 + 1 + Long.bitCount(8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(headerSize + rawPacket.Payload.length);

        byteBuffer.put((byte) rawPacket.Type.ordinal());
        byteBuffer.put((byte) rawPacket.Step.ordinal());
        byteBuffer.putLong(rawPacket.ID);
        byteBuffer.put(rawPacket.Payload);

        return byteBuffer.array();
    }

    public static RawPacket UnPack(byte[] payload) {
        RawPacket rawPacket = new RawPacket(PacketType.Data, ProtocolStep.Ping, RawPacket.IgnorePacketID, null);

        int val = payload[0] & 0xFF;
        switch (val) {
            case 0:
                rawPacket.Type = PacketType.None;
                break;
            case 1:
                rawPacket.Type = PacketType.Data;
                break;
        }

        val = payload[1] & 0xFF;
        switch (val) {
            case 0:
                rawPacket.Step = ProtocolStep.None;
                break;
            case 1:
                rawPacket.Step = ProtocolStep.Ping;
                break;
            case 2:
                rawPacket.Step = ProtocolStep.Pong;
                break;
            case 3:
                rawPacket.Step = ProtocolStep.Data;
                break;
            case 4:
                rawPacket.Step = ProtocolStep.DataAck;
                break;
        }

        ByteBuffer buffer = ByteBuffer.allocate(Long.bitCount(8));
        buffer.put(payload, 2, Long.bitCount(8));
        buffer.flip();
        rawPacket.ID = buffer.getLong();

        // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        int headerSize = 1 + 1 + Long.bitCount(8);
        rawPacket.Payload = new byte[payload.length - headerSize];
        System.arraycopy(payload, headerSize, rawPacket.Payload, 0, payload.length - headerSize);

        return rawPacket;
    }

    public static Packet RawPacketToPacket(RawPacket rawPacket) {
        PacketPayload packetPayload = null;
        try {
            JsonReader jsonReader = new JsonReader(new StringReader(new String(rawPacket.Payload)));
            jsonReader.setLenient(true);

            packetPayload = new Gson().fromJson(jsonReader, PacketPayload.class);
        } catch (Exception ex) {
            Helpers.ReportException(LogTag, ex);
        }

        return new Packet(
                rawPacket.Type,
                rawPacket.Step,
                rawPacket.ID,
                packetPayload
        );
    }

    public static RawPacket PacketToRawPacket(Packet packet) {
        RawPacket rawPacket = new RawPacket(packet.Type, packet.Step, packet.ID, null);

        String packetAsString = new Gson().toJson(packet.Payload);
        if (packetAsString != null) {
            rawPacket.Payload = packetAsString.getBytes();
        }

        return rawPacket;
    }
}
