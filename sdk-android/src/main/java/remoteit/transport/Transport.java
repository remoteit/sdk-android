package remoteit.transport;

public interface Transport {
    void Connect();

    void Disconnect();

    void OnTransportConnectionStatusChanged(TransportConnectionStatusHandler transportConnectionStatusHandler);

    void SendTransportData(byte[] data);

    void OnTransportDataIn(TransportDataHandler transportDataHandler);
}
