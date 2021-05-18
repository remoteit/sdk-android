package com.remoteit.sdk_android.networking;

import java.io.IOException;
import java.io.InputStream;

import static com.remoteit.sdk_android.helpers.Helpers.ReportException;

public class Helpers {
    private final static String LogTag = Helpers.class.toString();

    public static final int MAX_DATA_LENGTH = 4096; // seems like a frequent number in networking
    protected static int readNrOfBytes = -1;

    public static void ReadFromStream(InputStream inputStream, RawDataHandler rawDataHandler) {

        try {
            byte[] buffer = new byte[MAX_DATA_LENGTH];

            while ((readNrOfBytes = inputStream.read(buffer, 0, MAX_DATA_LENGTH)) > -1) {

                if (rawDataHandler != null) {
                    byte[] actualData = new byte[readNrOfBytes];

                    // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
                    System.arraycopy(buffer, 0, actualData, 0, readNrOfBytes);

                    rawDataHandler.HandleRawData(actualData);
                }
            }
        } catch (IOException e) {
            ReportException(LogTag, e);
        }
    }
}
