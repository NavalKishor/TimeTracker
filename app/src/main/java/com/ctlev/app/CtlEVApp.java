package com.ctlev.app;

import android.app.Application;

import com.ctlev.app.persistence.AppExecutors;
import com.ctlev.app.persistence.db.AppDatabase;

public class CtlEVApp extends Application {
    private AppExecutors mAppExecutors;
    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }


}
