package com.purecode.imapp.model;

import android.content.Context;
import android.util.Log;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.purecode.imapp.event.GlobalEventNotifer;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.db.PreferenceUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by purecode on 2016/6/29.
 *
 * 联系人管理器
 *
 * 次demo利用环信的好友关系，作为次demo app的好友关系，所以必须要以环信的好友位基准来同步app服务器的好友关系
 *
 * 如果APP不依赖于环信的好友关系，APP可以按照自己的业务需求处理自己的好友关系，例如如何建立一个好友，以下的一些代码
 * 将可以被去掉，例如从环信那里去取好友
 */
public class ContactHandler extends HandlerBase{
    private Map<String,IMUser> mContacts = new HashMap<>();
    private boolean mIsContactSynced = false;
    PreferenceUtils mPreference;

    ContactHandler(Context context,PreferenceUtils preferenceUtils) {
        super(context);
        mPreference = preferenceUtils;
    }

    /**
     * 从远程服务器获取联系人信息
     * ->从APP服务器上获取联系人
     *  ->APP服务器再去环信服务器上去同步联系人(如果依赖环信的好友体系)
     *   ->APP服务器返回给前端
     *    ->前端更新本地数据库
     */
    public void asyncfetchUsers() {
        Model.getInstance().globalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                mIsContactSynced = false;

                try {
                    fetchAppUsersFromServer();
                    mIsContactSynced = true;
                    mPreference.setContactSynced(true);
                } catch (Exception e) {
                    GlobalEventNotifer.getInstance().notifyContactSyncChanged(false);
                    e.printStackTrace();

                    return;
                }

                // 最后要更新本地数据库
                getDbManager().saveContacts(mContacts.values());

                GlobalEventNotifer.getInstance().notifyContactSyncChanged(true);
            }
        });
    }

    /**
     * 先加载本地的联系人
     */
    public void loadLocalContacts(){
        Log.d("Model", "load local contacts");
        List<IMUser> users = getDbManager().getContacts();

        if(users != null){
            mContacts.clear();

            for(IMUser user:users){
                mContacts.put(user.getHxId(),user);
            }
        }
    }

    public void addHXUser(String hxId){
        if(getUserByHx(hxId) != null){
            return;
        }

        IMUser user = fetchUserFromServerByHXID(hxId);

        mContacts.put(user.getAppUser(), user);

        // 记住应该还要去自己的APP服务器上去获取联系人信息
        fetchUserFromAppServer(user);

        // save to db;
        getDbManager().saveContact(user);
    }

    public void deleteContactByHXID(String hxId){
        IMUser user = getUserByHx(hxId);

        if(user == null){
            return;
        }

        mContacts.remove(user.getAppUser());
        getDbManager().deleteContact(user);
        getDbManager().removeInvitation(user.getHxId());
    }

    public Map<String,IMUser> getContacts(){
        return mContacts;
    }

    /**
     *
     * @return
     */
    public boolean isContactSynced(){
        return mIsContactSynced;
    }

    public List<IMUser> getContactsByHxIds(List<String> hxIds){
        return getDbManager().getContactsByHx(hxIds);
    }

    /**
     * 用来从APP服务器获取非好友的用户信息，根据环信的id列表
     * @param members
     * @return
     */
    public List<IMUser> fetchUsersFromServerByHXIDs(List<String> members){
        List<IMUser> users = new ArrayList<>();

        for(String id:members){
            // 伪代码设置昵称，和头像
            IMUser user = new IMUser();

            user.setAppUser(id);
            user.setHxId(id);
            user.setNick(NICKS[new Random().nextInt(100)%(NICKS.length-1)]);
            user.setAvartar(AVARTARS[new Random().nextInt(100) % (AVARTARS.length - 1)]);

            users.add(user);
        }

        saveNonFriends(users);

        return users;
    }

    /**
     * 用来根据APP user名称获取，详细的用户信息，其中包括环信ID
     * @param appUser
     * @return
     */
    public  IMUser fetchUserFromServer(String appUser){
        // 伪代码设置昵称，和头像
        IMUser user = new IMUser();

        user.setAppUser(appUser);
        user.setHxId(appUser);
        user.setNick(appUser + "_" + NICKS[new Random().nextInt(100)%(NICKS.length-1)]);
        user.setAvartar(AVARTARS[new Random().nextInt(100) % (AVARTARS.length - 1)]);

        return user;
    }

    /**
     * 从后台获取所有的好友详情列表
     *
     * 由于缺乏后台，我们这里直接从环信后台获取
     * @throws Exception
     */
    private void fetchAppUsersFromServer() throws Exception{
        // 由于缺乏后台server，我们直接从环信后台获取
        List<String> hxUsers = null;
        try {
            hxUsers = EMClient.getInstance().contactManager().getAllContactsFromServer();
        } catch (HyphenateException e) {
            e.printStackTrace();

            return;
        }

        mContacts.clear();

        // 为了演示头像和昵称，我们填入固定的数据
        int index = 0;
        for(String hxId:hxUsers){
            IMUser user = new IMUser();

            user.setAppUser(hxId);
            user.setHxId(hxId);
            user.setNick(user.getHxId() + "_" + NICKS[index % (NICKS.length-1)]);
            user.setAvartar(AVARTARS[index%(AVARTARS.length-1)]);

            mContacts.put(user.getAppUser(),user);

            index++;
        }
    }
    /**
     * try to fetch the user info from app server
     * and when fecting is done, update the cache and the db
     * @param user
     */
    private void fetchUserFromAppServer(IMUser user) {
        user.setNick(user.getHxId() + "_凤凰");
    }

    IMUser getUserByHx(String hxId){
        for(IMUser user:mContacts.values()){
            if(user.getHxId().equals(hxId)){
                return user;
            }
        }

        return null;
    }

    void reset(){
        mPreference.setContactSynced(false);
        mIsContactSynced = false;

        mContacts.clear();
    }

    /**
     * 根据环信id来获取用户详情
     * @param hxId
     * @return
     */
    private IMUser fetchUserFromServerByHXID(String hxId){

        // 伪代码设置昵称，和头像
        IMUser user = new IMUser();

        user.setAppUser(hxId);
        user.setHxId(hxId);
        user.setNick(hxId+ "_" + NICKS[new Random().nextInt(100)%(NICKS.length-1)]);
        user.setAvartar(AVARTARS[new Random().nextInt(100) % (AVARTARS.length - 1)]);
        return user;
    }

    private void saveNonFriends(Collection<IMUser> contacts){
        getDbManager().saveNonFriends(contacts);
    }

    // 就为了展示头像和昵称做的简单的假数据
    private static String[] NICKS = new String[]{"老虎","熊猫","猴子","猎豹","灰熊","企鹅"};
    private static String[] AVARTARS = new String[]{
            "http://hiphotos.baidu.com/zhixin/abpic/item/34bbf8cd7b899e516c616dcc40a7d933c9950d3f.jpg",
            "http://c.hiphotos.baidu.com/baike/pic/item/b8014a90f603738d2be54617b61bb051f819ec5c.jpg",
            "http://c.hiphotos.baidu.com/baike/w%3D268%3Bg%3D0/sign=788945d218178a82ce3c78a6ce3814b0/8435e5dde71190ef8c692dc1c81b9d16fdfa601a.jpg",
            "http://e.hiphotos.baidu.com/baike/pic/item/2f738bd4b31c87015d939086277f9e2f0708ffad.jpg"};
}
