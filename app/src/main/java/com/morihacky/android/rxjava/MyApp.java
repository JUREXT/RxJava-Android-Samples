package com.morihacky.android.rxjava;

import androidx.multidex.MultiDexApplication;

import timber.log.Timber;

public class MyApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // for better RxJava debugging
        //RxJavaHooks.enableAssemblyTracking();
        Timber.plant();
    }
}