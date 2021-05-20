package com.remoteit.sdk.sdk_android;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.remoteit.sdk_android.Device;
import com.remoteit.sdk_android.DeviceConfig;
import com.remoteit.sdk_android.helpers.AppContext;
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

    @Test
    public void r3RegisterService() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
        RegisteredService registeredService = API.RegisterService(loginResponse, registeredDevice);

        assertNotNull(registeredDevice.Secret);
        assertNotNull(registeredService.Secret);

        String registeredDeviceAsString = new Gson().toJson(registeredService);
        Timber.d(registeredDeviceAsString);
        System.out.println(registeredDeviceAsString);
    }

    @Test
    public void r3UnregisterService() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
        RegisteredService registeredService = API.RegisterService(loginResponse, registeredDevice);

        assertNotNull(registeredDevice.Secret);
        assertNotNull(registeredService.Secret);

        API.RemoveService(loginResponse, registeredService);
        Timber.d(loginResponse.toString(), registeredDevice.toString());
        System.out.println(loginResponse.toString() + "\n" + registeredDevice.toString());
    }

    @Test
    public void r3RegisterDeviceAndServiceSaveConfig() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        Device.Instance().Register(loginResponse, deviceName);

        String saveRegisteredDeviceAsString = new Gson().toJson(Device.Instance().Config());
        Timber.d(saveRegisteredDeviceAsString);
        System.out.println(saveRegisteredDeviceAsString);
    }

    @Test
    public void r3InitDeviceFromConfig() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);

        Device.Instance().Register(loginResponse, deviceName);
        String saveRegisteredDeviceAsString = new Gson().toJson(Device.Instance().Config());
        DeviceConfig deviceConfig = new Gson().fromJson(saveRegisteredDeviceAsString, DeviceConfig.class);
        assertNotNull(loginResponse);
        assertNotNull(deviceConfig);

        Device.InitFromConfig(deviceConfig);
        Timber.d(loginResponse.toString(), deviceConfig.toString());
        System.out.println(loginResponse.toString() + "\n" + deviceConfig.toString());
    }

    @Test
    public void r3UnregisterDeviceAndServiceFromConfig() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);

        Device.Instance().Register(loginResponse, deviceName);
        String saveRegisteredDeviceAsString = new Gson().toJson(Device.Instance().Config());
        DeviceConfig deviceConfig = new Gson().fromJson(saveRegisteredDeviceAsString, DeviceConfig.class);
        Device.InitFromConfig(deviceConfig);
        assertNotNull(loginResponse);
        assertNotNull(deviceConfig);

        Device.Instance().UnRegister(loginResponse);
        Timber.d(loginResponse.toString(), deviceConfig.toString());
        System.out.println(loginResponse.toString() + "\n" + deviceConfig.toString());
    }

    @Test
    public void r3BringOnline() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        Device.Instance().Register(loginResponse, deviceName);
        assertNotNull(loginResponse);

        Device.Instance().BringOnline(connectionStatus -> {
            Timber.d(connectionStatus.toString());
            System.out.println(connectionStatus.toString());
        }, AppContext.getContext());
    }

    @Test
    public void r3GoOffline() {
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        Device.Instance().Register(loginResponse, "r3-DEBUG");

        Device.Instance().BringOnline(connectionStatus -> {
            Timber.d(connectionStatus.toString());
            System.out.println(connectionStatus.toString());
        }, AppContext.getContext());
        Device.Instance().GoOffline();
    }

    @Test
    public void r3RegisterDeviceServiceGoOnline() {
        // REMOTEIT: Register + Bring online with remoteit servers
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        Device.Instance().Register(loginResponse, deviceName);
        Device.Instance().BringOnline(connectionStatus -> {
            Timber.d(connectionStatus.toString());
            System.out.println(connectionStatus.toString());
        }, AppContext.getContext());

        // REMOTEIT: Save settings to local or choose another storage method (Cloud etc.)
        String configAsString = new Gson().toJson(Device.Instance().Config());
        SharedPreferences sharedPreferences = AppContext.getContext().getSharedPreferences("r3settings", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(deviceName, configAsString).apply();
        assertNotNull(sharedPreferences);
        assertNotNull(configAsString);
    }

    @Test
    public void r3GoOnlineOnCreate() {
        // REMOTEIT: Get settings from local or another storage where you kept it
        SharedPreferences sharedPreferences = AppContext.getContext().getSharedPreferences("r3settings", Context.MODE_PRIVATE);
        assertNotNull(sharedPreferences);
        String configAsString = sharedPreferences.getString(deviceName, null);
        if (configAsString != null && !configAsString.equals("")) {
            DeviceConfig deviceConfig = new Gson().fromJson(configAsString, DeviceConfig.class);
            Device.InitFromConfig(deviceConfig);

            //REMOTEIT: Bring Online
            Device.Instance().BringOnline(connectionStatus -> {
                Timber.d(connectionStatus.toString());
                System.out.println(connectionStatus.toString());
            }, AppContext.getContext());
        }
    }

    @Test
    public void r3RemoveRegisteredDevice() {
        // REMOTEIT: Delete settings from local or another storage where you kept it
        SharedPreferences sharedPreferences = AppContext.getContext().getSharedPreferences("r3settings", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // REMOTE-IT: unregister device
        Device.Instance().GoOffline();
        LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        assertNotNull(loginResponse);

        Device.Instance().UnRegister(loginResponse);
        Timber.d(loginResponse.toString());
        System.out.println(loginResponse.toString());
    }
}