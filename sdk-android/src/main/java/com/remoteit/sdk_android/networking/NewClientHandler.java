package com.remoteit.sdk_android.networking;

import java.io.InputStream;
import java.io.OutputStream;

public interface NewClientHandler {
    void HandleNewClient(InputStream inputStream, OutputStream outputStream);
}
