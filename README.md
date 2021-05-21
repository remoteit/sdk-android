# remote.it sdk-android

## Overview
> The sdk-android serves one primary purpose which is to create a Peer-to-Peer connection between a remote.it devices. It downloads and connect as module on the android application and then establish a Peer-to-Peer connection.

## Features

- Login with with existing remote.it credentials
- Login with AuthHash
- Register device
- Register service
- Bring device online
- Go device offline
- Unregister device
- Unregister service

## Platforms

- Android
    - minSdkVersion 20
    - targetSdkVersion 30
## Language

- Java

## Usage
* ### Using as module
    * Download https://github.com/remoteit/sdk-android/tree/inmplementation-v1
    * Unzip archive
    * In active project import new module ( Android Studio -> File -> ProjectStructure -> Modules -> Add (+) -> Import... ) select source directory **`sdk-android`** from unziped archive folder
    * Then press **``Finish``** -> **``Ok``**
    * Ready to use


## Acceptable examples
* *Note: A valid remote.it credentials required*
    
    * `Login with userpass`
    ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
    }
     ```

    * `Login with AuthHash`
     ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
            LoginResponse newLoginResponse = API.LoginWithAuthHash(r3AccountName, loginResponse);
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
    }
     ```

    * `Login with userpass` | `Register device`
     ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
            LoginResponse newLoginResponse = API.LoginWithAuthHash(r3AccountName, loginResponse);
        
            Device.Instance().Register(loginResponse, deviceName);
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
    }
     ```

    * `Login with userpass` | `Register device` | `Bring online`
     ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass( r3AccountName, r3AccountPass);
            LoginResponse newLoginResponse = API.LoginWithAuthHash(r3AccountName, loginResponse);
            Device.Instance().Register(loginResponse, deviceName);

            Device.Instance().BringOnline(connectionStatus -> Log.d(LogTag, connectionStatus.toString()), getApplicationContext());
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
    }
     ```

    * `Login with userpass` | `Register device` | `Bring online` | `Save to local settings`
     ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
            LoginResponse newLoginResponse = API.LoginWithAuthHash(r3AccountName, loginResponse);
            Device.Instance().Register(loginResponse, deviceName);
            Device.Instance().BringOnline(connectionStatus -> Log.d(LogTag, connectionStatus.toString()), getApplicationContext());

            String configAsString = new Gson().toJson(Device.Instance().Config());
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("r3settings", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(deviceName, configAsString).apply();
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
    }
     ```

    * `Register Device and Service` | `Init Config`
     ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
            RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
            RegisteredService registeredService = API.RegisterService(loginResponse, registeredDevice);

            Device.Instance().Config().R3DeviceID = registeredDevice.UID;
            Device.Instance().Config().R3DeviceName = registeredDevice.Name;
            Device.Instance().Config().R3DeviceSecret = registeredDevice.Secret;
            Device.Instance().Config().R3OneServiceID = registeredService.UID;
            Device.Instance().Config().R3OneServiceSecret = registeredService.Secret;

            String configAsString = new Gson().toJson(Device.Instance().Config());
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
    }
     ```

    * `Initialize device from Config File` | `Bring online`
     ```
    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("r3settings", Context.MODE_PRIVATE);
    String configAsString = sharedPreferences.getString(deviceName, null);
    if (configAsString != null && !configAsString.equals("")) {
        DeviceConfig deviceConfig = new Gson().fromJson(configAsString, DeviceConfig.class);
        Device.InitFromConfig(deviceConfig);
    
    new Thread(() -> {
        try {
            Device.Instance().BringOnline(connectionStatus -> Log.d(LogTag, connectionStatus.toString()), getApplicationContext());
        } catch (Exception e) {
            Helpers.ReportException(LogTag, e);
    }
     ```

    * `Go Offline` | `Unregister Device`
     ```
    new Thread(() -> {
        try {
            Device.Instance().GoOffline();
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
            Device.Instance().UnRegister(loginResponse);
        } catch (Exception e) {
            e.printStackTrace();
    }
     ```

    * `Unregister Device by UID`
     ```
    new Thread(() -> {
        try {
            RegisteredService registeredDevice = new RegisteredService();
            registeredDevice.UID = Device.Instance().Config().R3DeviceID;

            API.UnregisterDevice(loginResponse, registeredDevice);
        } catch (Exception e) {
            e.printStackTrace();
    }
     ```

    * `Register Service`
     ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
            RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
            RegisteredService registeredService = API.RegisterService(loginResponse, registeredDevice);
            String registeredDeviceAsString = new Gson().toJson(registeredService);
        } catch (Exception e) {
            e.printStackTrace();
    }
     ```

    * `Unregister Service`
     ```
    new Thread(() -> {
        try {
            LoginResponse loginResponse = API.LoginWithUserPass(r3AccountName, r3AccountPass);
            RegisteredService registeredDevice = API.RegisterDevice(loginResponse, deviceName);
            RegisteredService registeredService = API.RegisterService(loginResponse, registeredDevice);
            API.RemoveService(loginResponse, registeredService);
        } catch (Exception e) {
            e.printStackTrace();
    }
     ```

## Testing

Tests are written using junit.