package com.purecode.imapp;

import android.app.Application;

import com.purecode.imapp.model.Model;

/**
 * Created by purecode on 2016/5/18.
 */
public class IMDemoApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();

        Model.getInstance().init(this);
    }
}
