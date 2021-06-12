package remoteit.remoteit;

import com.google.gson.annotations.SerializedName;

public class RegisteredService {
    @SerializedName("deviceaddress")
    public String UID;

    @SerializedName("name")
    public String Name;

    @SerializedName("created")
    public String Created;

    @SerializedName("owner")
    public String Owner;

    @SerializedName("enabled")
    public String Enabled;

    @SerializedName("alerted")
    public String Alerted;

    @SerializedName("title")
    public String Title;

    @SerializedName("devicetype")
    public String Type;

    @SerializedName("manufacturer")
    public String Manufacturer;

    @SerializedName("region")
    public String Region;

    @SerializedName("devicestate")
    public String State;

    @SerializedName("secret")
    public String Secret;
}
