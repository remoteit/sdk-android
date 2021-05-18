package com.remoteit.sdk_android.remoteit;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.remoteit.sdk_android.helpers.Helpers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

class r3LoginWithUserPassRequest {
    @SerializedName("username")
    public String UserName;

    @SerializedName("password")
    public String Password;
}

class r3LoginWithUserAuthHashRequest {
    @SerializedName("username")
    public String UserName;

    @SerializedName("authhash")
    public String AuthHash;
}

class r3GenerateIDResponse {
    @SerializedName("deviceaddress")
    public String UID;

    @SerializedName("status")
    public String Status;

    @SerializedName("reason")
    public String Reason;
}

class r3CreateServiceRequest {
    @SerializedName("deviceaddress")
    public String UID;

    @SerializedName("devicetype")
    public String ServiceType;
}

class r3CreateServiceResponse {
    @SerializedName("status")
    public String Status;

    @SerializedName("reason")
    public String Reason;
}

class r3RegisterServiceRequest {
    @SerializedName("deviceaddress")
    public String UID;

    @SerializedName("devicetype")
    public String ServiceType;

    @SerializedName("devicealias")
    public String Name;

    @SerializedName("hardwareid")
    public String HardwareID;

    @SerializedName("skipsecret")
    public String SkipSecret;

    @SerializedName("skipemail")
    public String SkipEmail;
}

class r3RegisterServiceResponse {
    @SerializedName("status")
    public String Status;

    @SerializedName("reason")
    public String Reason;

    @SerializedName("secret")
    public String Secret;

    @SerializedName("device")
    public RegisteredService Service;
}

class r3DeleteDeviceResponse {
    @SerializedName("status")
    public String Status;

    @SerializedName("reason")
    public String Reason;
}

class r3RemoveServiceRequest {
    @SerializedName("deviceaddress")
    public String UID;
}

class r3RemoveServiceResponse {
    @SerializedName("status")
    public String Status;

    @SerializedName("reason")
    public String Reason;
}

class r3CreateProxyRequest {
    @SerializedName("deviceaddress")
    public String DeviceAddress;

    @SerializedName("devicetype")
    public int DeviceType;

    @SerializedName("hostip")
    public String HostIP;

    @SerializedName("wait")
    public String Wait;

    @SerializedName("isolate")
    public String Isolate;

    @SerializedName("concurrent")
    public boolean Concurrent;

    @SerializedName("proxyType")
    public String ProxyType;
}

class r3DeleteProxyRequest {
    @SerializedName("deviceaddress")
    public String DeviceAddress;

    @SerializedName("connectionid")
    public String ConnectionID;
}

class r3DeleteProxyResponse {
    @SerializedName("status")
    public String Status;
}

public class API {
    private final static String LogTag = API.class.toString();

    public static final int TCPServiceID = 1;
    public static final int BulkServiceID = 35;
    public static final int ManufactureID = 33024; // RemoteIT CLI
    public static final int PlatformID = 769; // PlatformCodeGenericLinux
    private static final String r3ApiKey = "remote.it.developertoolsHW9iHnd";
    private static final String r3UserPassLoginURL = "https://api.remot3.it/apv/v27/user/login";
    private static final String r3UserAuthHashLoginURL = "https://api.remot3.it/apv/v27/user/login/authhash";
    private static final String r3CreateProxyURL = "https://api.remot3.it/apv/v27/device/connect";
    private static final String r3DeleteProxyURL = "https://api.remot3.it/apv/v27/device/connect/stop";
    private static final String r3GenerateIDURL = "https://api.remot3.it/apv/v27/device/address/NTNDNDkwODItODdEMS0yRjlGLTg2MEYtNzZENjgwRDRBNzQz/ODMyQTlCNjUtNUUwMC1DOUM1LUY0MDItMTAzQThEOEM0RUQ1";
    private static final String r3CreateServiceURL = "https://api.remot3.it/apv/v27/device/create";
    private static final String r3RegisterServiceURL = "https://api.remot3.it/apv/v27/device/register";
    private static final String r3DeleteDeviceURL = "https://api.remot3.it/apv/v27/developer/device/delete/registered/";
    private static final String r3RemoveServiceURL = "https://api.remot3.it/apv/v27//device/delete";
    private static final String CONNECTION_IP_LATCHING = "255.255.255.255";
    private static final String PROXY_CREATE_WAIT = "true";
    private static final String PROXY_CREATE_ISOLATE = "domain=app.remote.it";
    private static final boolean PROXY_CREATE_CONCURRENT = true;
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static final int MultiPortServiceID = 40;
    private static final String DefaultServiceType = "00:00:00:00:00:01:00:00:04:60:00:00";

    public static LoginResponse LoginWithUserPass(String user, String pass) {
        r3LoginWithUserPassRequest loginRequest = new r3LoginWithUserPassRequest();
        loginRequest.UserName = user;
        loginRequest.Password = pass;

        String data = new Gson().toJson(loginRequest);
        String loginResponseAsRaw = sendPOST_JSONGetJSON(r3UserPassLoginURL, null, data);

        return new Gson().fromJson(loginResponseAsRaw, LoginResponse.class);
    }

    public static LoginResponse LoginWithAuthHash(String user, LoginResponse loginResponse) {
        r3LoginWithUserAuthHashRequest loginRequest = new r3LoginWithUserAuthHashRequest();
        loginRequest.UserName = user;
        loginRequest.AuthHash = loginResponse.AuthHash;

        String data = new Gson().toJson(loginRequest);
        String loginResponseAsRaw = sendPOST_JSONGetJSON(
                r3UserAuthHashLoginURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }}, data
        );

        return new Gson().fromJson(loginResponseAsRaw, LoginResponse.class);
    }

    public static RegisteredService RegisterDevice(LoginResponse loginResponse, String deviceName) {
        String deviceID = generateR3ID(loginResponse);

        if (deviceID.equals("")) {
            return new RegisteredService();
        }

        // 1. create a blank service to be configured by the API
        r3CreateServiceRequest createServiceRequest = new r3CreateServiceRequest();
        createServiceRequest.UID = deviceID; // ex: "80:00:00:00:01:09:63:5C"
        createServiceRequest.ServiceType = getServiceType(BulkServiceID); // ex: "00:23:81:00:00:01:00:00:03:01:00:28"

        String data = new Gson().toJson(createServiceRequest);
        String createServiceResponseAsRaw = sendPOST_JSONGetJSON(
                r3CreateServiceURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }},
                data
        );

        r3CreateServiceResponse createServiceResponse = new Gson().fromJson(createServiceResponseAsRaw, r3CreateServiceResponse.class);
        if (createServiceResponse != null
                && createServiceResponse.Status != null
                && createServiceResponse.Status.equals("true")) {

            new RegisteredService();
        }

        // 2. associate a blank service UID to create a service
        r3RegisterServiceRequest registerServiceRequest = new r3RegisterServiceRequest();
        registerServiceRequest.UID = deviceID;
        registerServiceRequest.ServiceType = createServiceRequest.ServiceType;
        registerServiceRequest.Name = deviceName;
        registerServiceRequest.HardwareID = deviceID;
        registerServiceRequest.SkipSecret = "true";
        registerServiceRequest.SkipEmail = "false"; // don't skip for bulk, don't ask why

        data = new Gson().toJson(registerServiceRequest);
        String registerServiceResponseAsRaw = sendPOST_JSONGetJSON(
                r3RegisterServiceURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }},
                data
        );

        r3RegisterServiceResponse registerServiceResponse = new Gson().fromJson(registerServiceResponseAsRaw, r3RegisterServiceResponse.class);

        // 3. return device ID
        if (registerServiceResponse != null && registerServiceResponse.Service != null) {

            registerServiceResponse.Service.Secret = registerServiceResponse.Secret.replaceAll(":", "");
            return registerServiceResponse.Service;
        }

        return new RegisteredService();
    }


    public static void UnregisterDevice(LoginResponse loginResponse, RegisteredService registeredDevice) {
        String deleteDeviceResponseAsRaw = sendPOST_JSONGetJSON(
                r3DeleteDeviceURL + registeredDevice.UID,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }},
                ""
        );

        r3DeleteDeviceResponse deleteDeviceResponse = new Gson().fromJson(deleteDeviceResponseAsRaw, r3DeleteDeviceResponse.class);
        if (deleteDeviceResponse != null
                && deleteDeviceResponse.Status != null
                && !deleteDeviceResponse.Status.equals("true")) {

            Timber.e("Can't DELETE DEVICE: %s", deleteDeviceResponse.Reason);
            System.out.println("Can't DELETE DEVICE: %s" + deleteDeviceResponse.Reason);
        }
    }

    public static RegisteredService RegisterService(LoginResponse loginResponse, RegisteredService registeredDevice) {
        String serviceID = generateR3ID(loginResponse);

        if (serviceID.equals("")) {
            return new RegisteredService();
        }

        // 1. create a blank service to be configured by the API
        r3CreateServiceRequest createServiceRequest = new r3CreateServiceRequest();
        createServiceRequest.UID = serviceID; // ex: "80:00:00:00:01:09:63:5C"
        createServiceRequest.ServiceType = getServiceType(TCPServiceID); // ex: "00:23:81:00:00:01:00:00:03:01:00:28"

        String data = new Gson().toJson(createServiceRequest);
        String createServiceResponseAsRaw = sendPOST_JSONGetJSON(
                r3CreateServiceURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }},
                data
        );

        r3CreateServiceResponse createServiceResponse = new Gson().fromJson(createServiceResponseAsRaw, r3CreateServiceResponse.class);
        if (createServiceResponse != null
                && createServiceResponse.Status != null
                && createServiceResponse.Status.equals("true")) {

            new RegisteredService();
        }

        // 2. associate a blank service UID to create a service
        r3RegisterServiceRequest registerServiceRequest = new r3RegisterServiceRequest();
        registerServiceRequest.UID = serviceID;
        registerServiceRequest.ServiceType = createServiceRequest.ServiceType;
        registerServiceRequest.Name = "d360-data-transport";
        registerServiceRequest.HardwareID = registeredDevice.UID;
        registerServiceRequest.SkipSecret = "true";
        registerServiceRequest.SkipEmail = "true"; // skip for non-bulk, don't ask why

        data = new Gson().toJson(registerServiceRequest);
        String registerServiceResponseAsRaw = sendPOST_JSONGetJSON(
                r3RegisterServiceURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }},
                data
        );

        r3RegisterServiceResponse registerServiceResponse = new Gson().fromJson(registerServiceResponseAsRaw, r3RegisterServiceResponse.class);

        // 3. return device ID
        if (registerServiceResponse != null && registerServiceResponse.Service != null) {

            registerServiceResponse.Service.Secret = registerServiceResponse.Secret.replaceAll(":", "");
            return registerServiceResponse.Service;
        }

        return new RegisteredService();
    }

    public static void RemoveService(LoginResponse loginResponse, RegisteredService registeredService) {
        r3RemoveServiceRequest removeServiceRequest = new r3RemoveServiceRequest();
        removeServiceRequest.UID = registeredService.UID;

        String data = new Gson().toJson(removeServiceRequest);

        String removeServiceResponseAsRaw = sendPOST_JSONGetJSON(
                r3RemoveServiceURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }},
                data
        );

        r3RemoveServiceResponse removeServiceResponse = new Gson().fromJson(removeServiceResponseAsRaw, r3RemoveServiceResponse.class);
        if (removeServiceResponse != null
                && removeServiceResponse.Status != null
                && !removeServiceResponse.Status.equals("true")) {

            Timber.e("Can't DELETE DEVICE: %s", removeServiceResponse.Reason);
        }
    }

    public static R3CreateProxyResponse CreateProxy(String destinationService, LoginResponse loginResponse) {
        r3CreateProxyRequest createProxyRequest = new r3CreateProxyRequest();
        createProxyRequest.DeviceAddress = destinationService;
        createProxyRequest.DeviceType = 1; // tcp
        createProxyRequest.HostIP = CONNECTION_IP_LATCHING;
        createProxyRequest.Wait = PROXY_CREATE_WAIT;
        createProxyRequest.Isolate = PROXY_CREATE_ISOLATE;
        createProxyRequest.Concurrent = PROXY_CREATE_CONCURRENT;
        createProxyRequest.ProxyType = "port";

        String data = new Gson().toJson(createProxyRequest);
        String createProxyResponseAsRaw = sendPOST_JSONGetJSON(
                r3CreateProxyURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }}, data
        );

        return new Gson().fromJson(createProxyResponseAsRaw, R3CreateProxyResponse.class);
    }

    public static void DeleteProxy(LoginResponse loginResponse, R3CreateProxyResponse r3CreateProxyResponse) {
        if (r3CreateProxyResponse == null
                || r3CreateProxyResponse.ProxyInfo == null || r3CreateProxyResponse.ProxyInfo.DeviceAddress == null
                || r3CreateProxyResponse.ConnectionID == null) {

            return;
        }

        r3DeleteProxyRequest deleteProxyRequest = new r3DeleteProxyRequest();
        deleteProxyRequest.DeviceAddress = r3CreateProxyResponse.ProxyInfo.DeviceAddress;
        deleteProxyRequest.ConnectionID = r3CreateProxyResponse.ConnectionID;

        String data = new Gson().toJson(deleteProxyRequest);
        String deleteProxyResponseAsRaw = sendPOST_JSONGetJSON(
                r3DeleteProxyURL,
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }}, data
        );

        r3DeleteProxyResponse deleteProxyResponse = new Gson().fromJson(deleteProxyResponseAsRaw, r3DeleteProxyResponse.class);
        if (!"true".equals(deleteProxyResponse.Status)) {
            Timber.e("DeleteProxy() error");
        }
    }

    private static String generateR3ID(LoginResponse loginResponse) {
        String generateIDResponseAsRaw = sendGET_GetJSON(
                new HashMap<String, String>() {{
                    put("token", loginResponse.Token);
                }}
        );

        r3GenerateIDResponse generateIDResponse = new Gson().fromJson(generateIDResponseAsRaw, r3GenerateIDResponse.class);

        if (generateIDResponse != null && generateIDResponse.UID != null) {
            return generateIDResponse.UID;
        }

        return "";
    }

    private static String sendPOST_JSONGetJSON(String url, Map<String, String> headers, String data) {
        String response = "";

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("apikey", r3ApiKey);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // For POST only - START
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            int httpCode = connection.getResponseCode();
            if (httpCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                for (; true; ) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    } else {
                        sb.append(line).append("\n");
                    }
                }
                in.close();

                response = sb.toString();
            }
        } catch (IOException e) {
            Helpers.ReportException(LogTag, e);
        }

        return response;
    }

    private static String sendGET_GetJSON(Map<String, String> headers) {
        String response = "";

        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(API.r3GenerateIDURL)).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("apikey", r3ApiKey);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            int httpCode = connection.getResponseCode();
            if (httpCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                for (; true; ) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    } else {
                        sb.append(line).append("\n");
                    }
                }
                in.close();

                response = sb.toString();
            }
        } catch (IOException e) {
            Helpers.ReportException(LogTag, e);
        }

        return response;
    }

    private static String getServiceType(int applicationType) {
        byte[] deviceTypeAsBytes = parseHex(API.DefaultServiceType);

        // buffer.writeUInt16BE(applicationType, 0)
        byte[] applicationTypeAsHex = intToHex(applicationType);
        writeBytesToBuffer(deviceTypeAsBytes, applicationTypeAsHex, 0);

        // buffer.writeUInt16BE(applicationType === 35 ? 40 : 0, 10)
        byte[] val1AsHex = intToHex(0);
        writeBytesToBuffer(deviceTypeAsBytes, val1AsHex, 10);

        if (applicationType == BulkServiceID) {
            byte[] multiPortServiceIDAsHex = intToHex(MultiPortServiceID);
            writeBytesToBuffer(deviceTypeAsBytes, multiPortServiceIDAsHex, 10);
        }

        // if (manufacturer) buffer.writeUInt16BE(manufacturer, 2)
        byte[] manufacturerAsHex = intToHex(API.ManufactureID);
        writeBytesToBuffer(deviceTypeAsBytes, manufacturerAsHex, 2);

        // if (platform) buffer.writeUInt16BE(platform, 8)
        byte[] platformAsHex = intToHex(API.PlatformID);
        writeBytesToBuffer(deviceTypeAsBytes, platformAsHex, 8);

        return formatHex(deviceTypeAsBytes, 2);
    }

    private static byte[] intToHex(int n) {
        return parseHex(String.format("%04x", n));
    }

    private static byte[] parseHex(String bytesAsString) {

//		byte[] values = new byte[bytesAsString.length()];
        ByteArrayOutputStream values = new ByteArrayOutputStream();

        String[] bytesAsStringArray = bytesAsString.split(":");
        for (String byteAsString : bytesAsStringArray) {
            byte[] b = hexDecodeString(byteAsString);
//			values = append(values, b...)
            try {
                values.write(b);
            } catch (IOException e) {
                Helpers.ReportException(LogTag, e);
            }
        }

        return values.toByteArray();
    }

    private static String formatHex(byte[] buffer, int split) {
        if (buffer.length == 0) {
            return "";
        }

        if (split == 0) {
            split = 2;
        }

        String bufferAsString = bytesToHex(buffer);
        bufferAsString = bufferAsString.toUpperCase();

//		re := regexp.MustCompile(fmt.Sprintf(`(\S{%d})`, split))
//		return strings.Join(re.FindAllString(bufferAsString, -1), ":")

        List<String> bytesAsString = splitString(bufferAsString); // bufferAsString.split("(?<=\\G.{2})");


        StringBuilder sb = new StringBuilder();
        for (String part : bytesAsString) {
            sb.append(part);
            sb.append(":");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private static byte[] writeBytesToBuffer(byte[] dst, byte[] src, int startIndex) {
        for (byte b : src) {
            dst[startIndex] = b;
            startIndex++;
        }
        return dst;
    }

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    private static byte[] hexDecodeString(String hexString) {
//		if (hexString.length() % 2 == 1) {
//			throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
//		}

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    private static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int toDigit(char hexChar) {
//		if(digit == -1) {
//			throw new IllegalArgumentException("Invalid Hexadecimal Character: "+ hexChar);
//		}

        return Character.digit(hexChar, 16);
    }

    private static List<String> splitString(String string) {
        List<String> parts = new ArrayList<>();
        int len = string.length();
        for (int i = 0; i < len; i += 2) {
            parts.add(string.substring(i, Math.min(len, i + 2)));
        }
        return parts;
    }
}
