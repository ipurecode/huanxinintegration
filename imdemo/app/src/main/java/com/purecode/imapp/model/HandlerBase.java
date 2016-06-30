package com.purecode.imapp.model;

import android.content.Context;

import com.purecode.imapp.model.db.DBManager;

/**
 * Created by purecode on 2016/6/29.
 */
public class HandlerBase {
    private DBManager dbManager;
    private Context appContext;

    HandlerBase(Context context){
        appContext = context;
    }

    void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public DBManager getDbManager() {
        return dbManager;
    }

    public Context getAppContext() {
        return appContext;
    }
}
