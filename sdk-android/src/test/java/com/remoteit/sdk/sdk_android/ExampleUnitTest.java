package com.remoteit.sdk.sdk_android;

import com.google.gson.Gson;
import com.remoteit.sdk_android.Device;
import com.remoteit.sdk_android.remoteit.API;
import com.remoteit.sdk_android.remoteit.LoginResponse;
import com.remoteit.sdk_android.remoteit.RegisteredService;

import org.junit.Test;

import timber.log.Timber;

import static org.junit.Assert.assertNotNull;

public class ExampleUnitTest {
    // In order to run the tests, initialize the variables below with existing values.
    public static String r3AccountName;
    public static String r3AccountPass;

    public static final String deviceName = "r3-DEBUG";

    @Test
    public void r3LoginUserPass() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        assertNotNull(loginResponse.AuthHash);
        Timber.d(loginResponse.AuthHash);
        System.out.println(loginResponse.AuthHash);
    }

    @Test
    public void r3LoginUserAuthHash() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        LoginResponse newLoginResponse = API.LoginWithAuthHash(r3AccountName, loginResponse);
        assertNotNull(loginResponse.AuthHash);
        assertNotNull(newLoginResponse.AuthHash);
        Timber.d(newLoginResponse.AuthHash);
        System.out.println(newLoginResponse.AuthHash);
    }

    @Test
    public void r3RegisterDevice() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
        assertNotNull(registeredDevice.Secret);

        String saveRegisteredDeviceAsString = new Gson().toJson(registeredDevice);
        Timber.d(saveRegisteredDeviceAsString);
        System.out.println(saveRegisteredDeviceAsString);
    }

    @Test
    public void r3RegisterDeviceSaveConfig() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
        assertNotNull(registeredDevice.Secret);

        Device.Instance().Config().R3DeviceID = registeredDevice.UID;
        Device.Instance().Config().R3DeviceName = registeredDevice.Name;
        Device.Instance().Config().R3DeviceSecret = registeredDevice.Secret;

        String saveRegisteredDeviceAsString = new Gson().toJson(Device.Instance().Config());
        Timber.d(saveRegisteredDeviceAsString);
        System.out.println(saveRegisteredDeviceAsString);
    }

    @Test
    public void r3UnregisterDevice() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
        assertNotNull(registeredDevice.Secret);

        API.UnregisterDevice(loginResponse, registeredDevice);
        Timber.d(loginResponse.toString(), registeredDevice.toString());
        System.out.println(loginResponse.toString() + "\n" + registeredDevice.toString());
    }

    @Test
    public void r3UnregisterDeviceByUID() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);

        Device.Instance().Config().R3DeviceID = registeredDevice.UID;
        registeredDevice.UID = Device.Instance().Config().R3DeviceID;

        API.UnregisterDevice(loginResponse, registeredDevice);
        Timber.d(loginResponse.toString(), registeredDevice.toString());
        System.out.println(loginResponse.toString() + "\n" + registeredDevice.toString());
    }


}
