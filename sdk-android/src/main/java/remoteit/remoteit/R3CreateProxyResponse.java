package remoteit.remoteit;

import com.google.gson.annotations.SerializedName;

public class R3CreateProxyResponse {
    @SerializedName("status")
    public String Status;

    @SerializedName("reason")
    public String Reason;

    @SerializedName("connection")
    public R3ProxyInfo ProxyInfo;

    @SerializedName("connectionid_cached")
    public String ConnectionIDCached;

    @SerializedName("connectionid")
    public String ConnectionID;
}
