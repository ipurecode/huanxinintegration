package com.purecode.imapp.controller.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.purecode.imapp.R;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.Model;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import static android.widget.Toast.makeText;

/**
 * Created by purecode on 2016/5/18.
 */
public class LoginActivity extends Activity{

    private EditText mEtName;
    private EditText mEtPwd;

    private Button mRegisterBtn;
    private Button mLoginBtn;
    private Activity me;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        init();
    }

    private void init(){
        mEtName = (EditText) findViewById(R.id.et_user_name);
        mEtPwd = (EditText) findViewById(R.id.et_pwd);

        mRegisterBtn = (Button) findViewById(R.id.btn_register);
        mLoginBtn = (Button) findViewById(R.id.btn_login);

        me = this;

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void register() {
        if(!isValidNameOrPwd()){
            Toast.makeText(this,"invalid pwd or name",Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);

        pd.show();

        Model.getInstance().globalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                String appUser = mEtName.getText().toString();

                IMUser account = Model.getInstance().getUserAccountHandler().getAccount(appUser);

                if(account == null){
                    try {
                        account = Model.getInstance().getUserAccountHandler().createAppAccountFromAppServer(appUser);
                    } catch (Exception e) {

                        // 创建APP账号失败
                        showMessage(pd,e.toString());
                        return;
                    }
                }

                String pwd = mEtPwd.getText().toString();

                try {
                    EMClient.getInstance().createAccount(account.getHxId(),pwd);

                    Model.getInstance().getUserAccountHandler().addAccount(account);

                    showMessage(pd, "注册成功！");
                } catch (HyphenateException e) {
                    final String msg = e.toString();

                    showMessage(pd,"注册失败！" +  msg);
                }
            }
        });
    }

    private void login(){
        if(!isValidNameOrPwd()){
            Toast.makeText(this,"invalid pwd or name",Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);
        pd.show();

        final String pwd = mEtPwd.getText().toString();
        final String appUser = mEtName.getText().toString();

        IMUser account = Model.getInstance().getUserAccountHandler().getAccount(appUser);

        if(account == null){
            Model.getInstance().globalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    IMUser account = null;
                    try {
                        account = Model.getInstance().getUserAccountHandler().getAccountFromServer(appUser);
                    } catch (Exception e) {
                        showMessage(pd, e.toString());

                        return;
                    }

                    loginHX(account, pwd, pd);
                }
            });
        }else{
            loginHX(account,pwd,pd);
        }
    }

    private void loginHX(final IMUser user, String pwd, final ProgressDialog pd){
        Model.getInstance().preLogin(user);

        EMClient.getInstance().login(user.getHxId(), pwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                // 加用户账号到本地
                Model.getInstance().getUserAccountHandler().addAccount(user);

                // 通知model登录成功
                Model.getInstance().onLoginSuccess(user);

                // 取本地会话
                EMClient.getInstance().chatManager().loadAllConversations();

                // 取本地群组
                EMClient.getInstance().groupManager().loadAllGroups();

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.cancel();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));

                        finish();
                    }
                });
            }

            @Override
            public void onError(int errorCode, final String error) {
                showMessage(pd,error);
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    private boolean isValidNameOrPwd(){
        return (!TextUtils.isEmpty(mEtName.getText().toString()) && !TextUtils.isEmpty(mEtPwd.getText().toString()));
    }

    private void showMessage(final ProgressDialog pd, final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(me,message,Toast.LENGTH_LONG).show();
                pd.cancel();
            }
        });
    }
}


