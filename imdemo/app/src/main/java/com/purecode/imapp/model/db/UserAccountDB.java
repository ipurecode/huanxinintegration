package com.purecode.imapp.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.purecode.imapp.model.datamodel.IMUser;

/**
 * Created by purecode on 16/6/22.
 */
public class UserAccountDB {
    private static final int DB_VERSION = 1;
    private DBHelper dbHelper;

    public UserAccountDB(Context context){
        dbHelper = new DBHelper(context);
    }

    public void addAccount(IMUser user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserAccountTable.COL_USERNAME,user.getAppUser());
        values.put(UserAccountTable.COL_HXID,user.getHxId());
        values.put(UserAccountTable.COL_NICK,user.getNick());
        values.put(UserAccountTable.COL_AVATAR,user.getAvartar());

        db.replace(UserAccountTable.TABLE_NAME,null,values);

    }

    public IMUser getAccount(String appUser){
        IMUser account = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + UserAccountTable.TABLE_NAME + " where " + UserAccountTable.COL_USERNAME + " =? ", new String[]{appUser});

        if(cursor.moveToNext()){
            account = getUser(cursor);
        }

        cursor.close();

        return account;
    }

    public IMUser getAccountByHxId(String hxId){
        IMUser account = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + UserAccountTable.TABLE_NAME + " where " + UserAccountTable.COL_HXID + " =? ", new String[]{hxId});

        if(cursor.moveToNext()){
            account = getUser(cursor);
        }

        cursor.close();

        return account;
    }

    private IMUser getUser(Cursor cursor){
        String appUser = cursor.getString(cursor.getColumnIndex(UserAccountTable.COL_USERNAME));

        IMUser user = new IMUser(appUser);

        user.setHxId(cursor.getString(cursor.getColumnIndex(UserAccountTable.COL_HXID)));
        user.setNick(cursor.getString(cursor.getColumnIndex(UserAccountTable.COL_NICK)));
        user.setAvartar(cursor.getString(cursor.getColumnIndex(UserAccountTable.COL_AVATAR)));

        return user;
    }

    static class DBHelper extends SQLiteOpenHelper{

        DBHelper(Context context){
            super(context,"_user_account",null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(UserAccountTable.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}

class UserAccountTable{
    static final String TABLE_NAME = "_user_account";

    static final String COL_USERNAME = "_app_user";
    static final String COL_HXID = "_hx_id";
    static final String COL_AVATAR = "_user_avatar";
    static final String COL_NICK = "_nick";

    static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + "("
            + COL_USERNAME + " TEXT PRIMARY KEY, "
            + COL_HXID + " TEXT, "
            + COL_NICK + " TEXT, "
            + COL_AVATAR + " TEXT);";
}
