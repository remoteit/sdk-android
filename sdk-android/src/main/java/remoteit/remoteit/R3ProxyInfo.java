package remoteit.remoteit;

import com.google.gson.annotations.SerializedName;

public class R3ProxyInfo {
    @SerializedName("deviceaddress")
    public String DeviceAddress;

    @SerializedName("expirationsec")
    public String ExpirationSec;

    @SerializedName("imageintervalms")
    public String ImageIntervalMS;

    @SerializedName("proxy")
    public String Proxy;

    @SerializedName("proxyport")
    public String ProxyPort;

    @SerializedName("proxyserver")
    public String ProxyServer;

    @SerializedName("requested")
    public String Requested;

    @SerializedName("status")
    public String Status;

    @SerializedName("streamscheme")
    public String StreamScheme;

    @SerializedName("streamuri")
    public String[] StreamURI;

    @SerializedName("url")
    public String[] URL;

    @SerializedName("requestedAt")
    public String RequestedAt;
}
