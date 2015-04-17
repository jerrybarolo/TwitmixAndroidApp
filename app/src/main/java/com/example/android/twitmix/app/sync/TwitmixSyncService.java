package com.example.android.twitmix.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by jerrybarolo on 17/04/15.
 */
public class TwitmixSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static TwitmixSyncAdapter sTwitmixSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("TwitmixSyncService", "onCreate - TwitmixSyncService");
        synchronized (sSyncAdapterLock) {
            if (sTwitmixSyncAdapter == null) {
                sTwitmixSyncAdapter = new TwitmixSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sTwitmixSyncAdapter.getSyncAdapterBinder();
    }
}
