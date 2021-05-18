package com.remoteit.sdk_android;

import com.google.gson.annotations.SerializedName;

public class DeviceConfig {
    @SerializedName("r3deviceId")
    public String R3DeviceID;

    @SerializedName("r3deviceName")
    public String R3DeviceName;

    @SerializedName("r3deviceSecret")
    public String R3DeviceSecret;

    @SerializedName("r3oneServiceId")
    public String R3OneServiceID;

    @SerializedName("r3oneServiceSecret")
    public String R3OneServiceSecret;
}
