package com.example.android.twitmix.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jerrybarolo on 17/04/15.
 */

/**
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class TwitmixAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private TwitmixAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new TwitmixAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
