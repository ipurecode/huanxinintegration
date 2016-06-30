package com.purecode.imapp.model;

import android.content.Context;

import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.db.UserAccountDB;

/**
 * Created by purecode on 2016/6/29.
 *
 * 用户账号管理器
 *
 * 用来管理多账号登录
 */
public class UserAccountHandler extends HandlerBase {
    private UserAccountDB userAccountDB;

    UserAccountHandler(Context context,UserAccountDB accountDB) {
        super(context);

        userAccountDB = accountDB;
    }

    public void addAccount(IMUser account){
        userAccountDB.addAccount(account);
    }

    public IMUser getAccount(String appUser){
        return userAccountDB.getAccount(appUser);
    }

    public IMUser getAccountFromServer(String appUser) throws Exception{
        return new IMUser(appUser);
    }

    public IMUser getAccountByHxId(String hxId){
        return userAccountDB.getAccountByHxId(hxId);
    }

    public IMUser createAppAccountFromAppServer(String appUser) throws Exception{

        //试图去创建一个APP 用户
        //如果成功就返回IMUser，如果不成功就抛异常
        return new IMUser(appUser);
    }
}
