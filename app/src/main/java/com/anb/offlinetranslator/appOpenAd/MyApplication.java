package com.anb.offlinetranslator.appOpenAd;


import android.app.Application;

import com.anb.offlinetranslator.utils.TinyDB;
import com.google.android.gms.ads.MobileAds;

public class MyApplication extends Application {
    private static AppOpenManager appOpenManager;
    public static TinyDB TinyDB;
    @Override
    public void onCreate() {
        super.onCreate();
       TinyDB = new TinyDB(this);
        MobileAds.initialize(
                this,
                initializationStatus -> {});

        appOpenManager = new AppOpenManager(this);
    }

}
