package com.purecode.imapp.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.purecode.imapp.model.datamodel.IMInvitationGroupInfo;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.datamodel.InvitationInfo;
import com.purecode.imapp.model.datamodel.InvitationInfo.InvitationStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by purecode on 2016/5/19.
 */
public class DBManager {
    private static final int DB_VERSION = 1;
    private static final String TAG = "DBManager";
    private DBHelper mHelper;

    public DBManager(Context context, String dbName){
        init(context, dbName);
    }

    public void close(){
        mHelper.close();
    }

    private void init(Context context, String dbName){
        mHelper = new DBHelper(context,dbName);
    }

    public boolean saveContacts(Collection<IMUser> contacts){
        if(contacts== null ||contacts.isEmpty()){
            return false;
        }

        checkAvailability();

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();

        for(IMUser user:contacts){
            saveContact(user,db,true);
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        return true;
    }

    public void saveNonFriends(Collection<IMUser> contacts){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();

        for(IMUser user:contacts){
            saveContact(user,db,false);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<IMUser> getContacts(){
        checkAvailability();

        List<IMUser> users = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * from " + UserTable.TABLE_NAME + " where " + UserTable.COL_MYCONTACT + " = 1", null);

        while(cursor.moveToNext()){
            IMUser user = getContact(cursor);
            users.add(user);
        }

        cursor.close();

        return users;
    }

    public List<IMUser> getContactsByHx(List<String> hxIds){
        if(hxIds == null || hxIds.size() <= 0){
            return null;
        }

        List<IMUser> appUsers = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();

        for(String hxId:hxIds){
            Cursor cursor = db.rawQuery("select * from " + UserTable.TABLE_NAME + " where " + UserTable.COL_HXID + " =? ",new String[]{hxId});

            if(cursor.moveToNext()){
                appUsers.add(getContact(cursor));
            }

            cursor.close();
        }

        return appUsers;
    }

    private IMUser getContact(Cursor cursor){
        IMUser user = new IMUser();
        user.setAppUser(cursor.getString(cursor.getColumnIndex(UserTable.COL_USERNAME)));
        user.setNick(cursor.getString(cursor.getColumnIndex(UserTable.COL_NICK)));
        user.setHxId(cursor.getString(cursor.getColumnIndex(UserTable.COL_HXID)));
        user.setAvartar(cursor.getString(cursor.getColumnIndex(UserTable.COL_AVATAR)));

        return user;
    }

    public void saveContact(IMUser user){
        SQLiteDatabase db = mHelper.getWritableDatabase();

        saveContact(user,db,true);
    }

    public void saveContact(IMUser user, SQLiteDatabase db, boolean isMyFriend){
        ContentValues values = new ContentValues();

        values.put(UserTable.COL_HXID,user.getHxId());
        values.put(UserTable.COL_USERNAME, user.getAppUser());
        values.put(UserTable.COL_AVATAR, user.getAvartar());
        values.put(UserTable.COL_NICK, user.getNick());
        values.put(UserTable.COL_MYCONTACT, isMyFriend?1:0);

        db.replace(UserTable.TABLE_NAME, null, values);
    }

    public void deleteContact(IMUser user){
        SQLiteDatabase db = mHelper.getWritableDatabase();

        db.delete(UserTable.TABLE_NAME, UserTable.COL_USERNAME + " = ? ", new String[]{user.getAppUser()});
    }

    public List<InvitationInfo> getInvitations(){

        List<InvitationInfo> inviteInfos = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + InvitationMessageTable.TABLE_NAME,null);

        while(cursor.moveToNext()){
            String groupId = cursor.getString(cursor.getColumnIndex(InvitationMessageTable.COL_GROUP_ID));

            boolean isGroupInvite = (groupId != null);

            InvitationInfo info = new InvitationInfo();
            String name = cursor.getString(cursor.getColumnIndex(InvitationMessageTable.COL_USERNAME));

            if(!isGroupInvite){
                IMUser user = new IMUser();
                user.setHxId(cursor.getString(cursor.getColumnIndex(InvitationMessageTable.COL_HXID)));
                user.setNick(name);

                info.setUser(user);
            }else{
                IMInvitationGroupInfo groupInfo = new IMInvitationGroupInfo();

                groupInfo.setGroupId(groupId);
                groupInfo.setGroupName(cursor.getString(cursor.getColumnIndex(InvitationMessageTable.COL_GROUP_NAME)));
                groupInfo.setInviteTriggerUser(name);

                info.setGroupInfo(groupInfo);
            }


            info.setStatus(int2InviteStatus(cursor.getInt(cursor.getColumnIndex(InvitationMessageTable.COL_INVITE_STATUS))));
            info.setReason(cursor.getString(cursor.getColumnIndex(InvitationMessageTable.COL_REASON)));
            inviteInfos.add(info);
        }

        cursor.close();
        return inviteInfos;
    }

    private InvitationStatus int2InviteStatus(int intStatus){
        if(intStatus == InvitationStatus.NEW_INVITE.ordinal()){
            return InvitationStatus.NEW_INVITE;
        }

        if(intStatus == InvitationStatus.INVITE_ACCEPT.ordinal()){
            return InvitationStatus.INVITE_ACCEPT;
        }

        if(intStatus == InvitationStatus.INVITE_ACCEPT_BY_PEER.ordinal()){
            return InvitationStatus.INVITE_ACCEPT_BY_PEER;
        }

        if(intStatus == InvitationStatus.NEW_GROUP_INVITE.ordinal()){
            return InvitationStatus.NEW_GROUP_INVITE;
        }

        if(intStatus == InvitationStatus.NEW_GROUP_APPLICATION.ordinal()){
            return InvitationStatus.NEW_GROUP_APPLICATION;
        }

        if(intStatus == InvitationStatus.GROUP_INVITE_ACCEPTED.ordinal()){
            return InvitationStatus.GROUP_INVITE_ACCEPTED;
        }

        if(intStatus == InvitationStatus.GROUP_APPLICATION_ACCEPTED.ordinal()){
            return InvitationStatus.GROUP_APPLICATION_ACCEPTED;
        }

        if(intStatus == InvitationStatus.GROUP_INVITE_DECLINED.ordinal()){
            return InvitationStatus.GROUP_INVITE_DECLINED;
        }

        if(intStatus == InvitationStatus.GROUP_APPLICATION_DECLINED.ordinal()){
            return InvitationStatus.GROUP_APPLICATION_DECLINED;
        }

        if(intStatus == InvitationStatus.GROUP_ACCEPT_INVITE.ordinal()){
            return InvitationStatus.GROUP_ACCEPT_INVITE;
        }

        if(intStatus == InvitationStatus.GROUPO_ACCEPT_APPLICATION.ordinal()){
            return InvitationStatus.GROUPO_ACCEPT_APPLICATION;
        }

        if(intStatus == InvitationStatus.GROUP_REJECT_APPLICATION.ordinal()){
            return InvitationStatus.GROUP_REJECT_APPLICATION;
        }

        if(intStatus == InvitationStatus.GROUP_REJECT_INVITE.ordinal()){
            return InvitationStatus.GROUP_REJECT_INVITE;
        }

        return null;
    }

    public void addInvitation(InvitationInfo invitationInfo){
        SQLiteDatabase db = mHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(InvitationMessageTable.COL_INVITE_STATUS,invitationInfo.getStatus().ordinal());
        values.put(InvitationMessageTable.COL_REASON,invitationInfo.getReason());

        if(invitationInfo.getUser() != null){
            values.put(InvitationMessageTable.COL_HXID,invitationInfo.getUser().getHxId());
            values.put(InvitationMessageTable.COL_USERNAME,invitationInfo.getUser().getNick());
        }else{
            values.put(InvitationMessageTable.COL_HXID,invitationInfo.getGroupInfo().getInviteTriggerUser());
            values.put(InvitationMessageTable.COL_GROUP_ID,invitationInfo.getGroupInfo().getGroupId());
            values.put(InvitationMessageTable.COL_GROUP_NAME,invitationInfo.getGroupInfo().getGroupName());
            values.put(InvitationMessageTable.COL_USERNAME,invitationInfo.getGroupInfo().getInviteTriggerUser());
        }

        long rt = db.replace(InvitationMessageTable.TABLE_NAME, null, values);

        Log.d(TAG, db.getPath() + " : reslult : " + rt + " content values : " + values.toString());
    }

    public void removeInvitation(String hxId){
        SQLiteDatabase db = mHelper.getWritableDatabase();

        db.delete(InvitationMessageTable.TABLE_NAME, InvitationMessageTable.COL_HXID + " =? ", new String[]{hxId});
    }

    public void updateInvitationStatus(InvitationStatus invitationStatus,String hxId){
        ContentValues values = new ContentValues();
        values.put(InvitationMessageTable.COL_INVITE_STATUS, invitationStatus.ordinal());

        updateInvitationInfo(values, hxId);
    }

    public void updateInvitationUserName(String username,String hxId){
        ContentValues values = new ContentValues();
        values.put(InvitationMessageTable.COL_USERNAME,username);

        updateInvitationInfo(values, hxId);
    }

    private void updateInvitationInfo(ContentValues updateValues, String hxId){
        SQLiteDatabase db = mHelper.getWritableDatabase();

        db.update(InvitationMessageTable.TABLE_NAME, updateValues, InvitationMessageTable.COL_HXID + "=?", new String[]{hxId});
    }

    private void checkAvailability(){
        if(mHelper == null){
            throw new RuntimeException("the helper is null, please init the db mananger");
        }
    }

    public void updateInvitateNoify(boolean hasNotif){
        checkAvailability();

        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NotificationTable.COL_NOTIF_NAME,NotificationTable.INVITE_NOTIF_NAME);
        values.put(NotificationTable.COL_MARKED,hasNotif?1:0);
        db.update(NotificationTable.TABLE_NAME,values,null,null);
        //db.replace(NotificationTable.TABLE_NAME, null, values);
    }

    public boolean hasInviteNotif(){
        checkAvailability();

        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + NotificationTable.TABLE_NAME + " WHERE " + NotificationTable.COL_NOTIF_NAME + "=?", new String[]{NotificationTable.INVITE_NOTIF_NAME});

        while (cursor.moveToNext()){
            int notif = cursor.getInt(cursor.getColumnIndex(NotificationTable.COL_MARKED));

            if(notif > 0){
                return true;
            }
        }

        cursor.close();
        return false;
    }

    class DBHelper extends SQLiteOpenHelper{


        public DBHelper(Context context,String name) {
            super(context, name, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(UserTable.SQL_CREATE_TABLE);
            db.execSQL(InvitationMessageTable.SQL_CREATE_TABLE);
            NotificationTable.onCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}

class UserTable{
    static final String TABLE_NAME = "user";

    static final String COL_USERNAME = "_user_name";
    static final String COL_HXID = "_hx_id";
    static final String COL_AVATAR = "_user_avatar";
    static final String COL_NICK = "_nick";

    /**
     * indicate if this is my friend or the user used for user info caching
     */
    static final String COL_MYCONTACT = "_is_my_friend";

    static final String SQL_CREATE_TABLE = "CREATE TABLE "
                                            + TABLE_NAME + "("
                                            + COL_USERNAME + " TEXT, "
                                            + COL_HXID + " TEXT PRIMARY KEY, "
                                            + COL_MYCONTACT + " INTEGER, "
                                            + COL_NICK + " TEXT, "
                                            + COL_AVATAR + " TEXT);";

}

class InvitationMessageTable {
    static final String TABLE_NAME = "invitation_message";
    static final String COL_HXID = "_hx_id";
    static final String COL_USERNAME = "_username";
    static final String COL_GROUP_NAME = "_group_name";
    static final String COL_GROUP_ID = "_group_id";
    static final String COL_GROUP_INVITE_TRIGGER_USER = "_invite_trigger_user";
    static final String COL_REASON ="_reason";
    static final String COL_INVITE_STATUS ="_invite_status";

    static final String SQL_CREATE_TABLE = "CREATE TABLE "
                                            + TABLE_NAME + " ("
                                            + COL_INVITE_STATUS + " INTEGER , "
                                            + COL_REASON + " TEXT, "
                                            + COL_USERNAME + " TEXT, "
                                            + COL_GROUP_NAME + " TEXT, "
                                            + COL_GROUP_ID + " TEXT, "
                                            + COL_HXID + " TEXT PRIMARY KEY);";
}

class NotificationTable{
    static final String TABLE_NAME = "_notif";
    static final String COL_NOTIF_NAME = "_notif_name";
    static final String COL_MARKED = "_marked";
    static final String SQL_CREATE_TABLE = "CREATE TABLE "
                                            + TABLE_NAME + "("
                                            + COL_NOTIF_NAME + " TEXT PRIMARY KEY, "
                                            + COL_MARKED + " INTEGER);";

    static final String INVITE_NOTIF_NAME = "invite_notif";

    static void onCreate(SQLiteDatabase db){
        // 创建notification表
        db.execSQL(SQL_CREATE_TABLE);

        // 插入固定的行
        ContentValues values = new ContentValues();

        values.put(COL_NOTIF_NAME,INVITE_NOTIF_NAME);
        values.put(COL_MARKED,0);

        db.insert(TABLE_NAME,null,values);
    }
}