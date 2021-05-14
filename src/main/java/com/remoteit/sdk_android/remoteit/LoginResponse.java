package com.remoteit.sdk_android.remoteit;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("service_authhash")
    public String AuthHash;

    @SerializedName("guid")
    public String Guid;

    @SerializedName("status")
    public String Status;

    @SerializedName("reason")
    public String Reason;

    @SerializedName("code")
    public String Code;

    @SerializedName("token")
    public String Token;
}
