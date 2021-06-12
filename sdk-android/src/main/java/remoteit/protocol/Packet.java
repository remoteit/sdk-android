package remoteit.protocol;

public class Packet {
    public PacketType Type;
    public ProtocolStep Step;
    public long ID;
    public PacketPayload Payload;

    public Packet(PacketType type, ProtocolStep step, long id, PacketPayload payload) {
        this.Type = type;
        this.Step = step;
        this.ID = id;
        this.Payload = payload;
    }
}
