package com.zirkler.czannotationviewsample;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Configuration.Builder config = new Configuration.Builder(this);
        config.addModelClasses(Drawing.class);
        ActiveAndroid.initialize(config.create());
        ActiveAndroid.initialize(this);
    }
}
