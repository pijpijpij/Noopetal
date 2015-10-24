package com.pij.noopetal.example;

import android.app.Application;

import com.example.butterknife.BuildConfig;

import butterknife.ButterKnife;

public class SimpleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(BuildConfig.DEBUG);
    }
}
