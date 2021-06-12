package remoteit.protocol;

public class RawPacket {
    public PacketType Type;
    public ProtocolStep Step;
    public long ID;
    public byte[] Payload;

    public RawPacket(PacketType type, ProtocolStep step, long id, byte[] payload) {
        this.Type = type;
        this.Step = step;
        this.ID = id;
        this.Payload = payload;
    }

    public static long IgnorePacketID = -1;
}
