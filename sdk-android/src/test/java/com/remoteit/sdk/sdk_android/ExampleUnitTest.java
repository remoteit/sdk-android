package com.remoteit.sdk.sdk_android;

import com.remoteit.sdk_android.remoteit.API;
import com.remoteit.sdk_android.remoteit.LoginResponse;

import org.junit.Test;

import timber.log.Timber;

import static org.junit.Assert.assertNotNull;

public class ExampleUnitTest{
    // In order to run the tests, initialize the variables below with existing values.
    public static String r3AccountName;
    public static String r3AccountPass;

    @Test
    public void r3LoginUserPass(){
        LoginResponse loginResponse=API.LoginWithUserPass(r3AccountName,r3AccountPass);
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


}
