package com.purecode.imapp.controller.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.purecode.imapp.R;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.Model;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

/**
 * Created by purecode on 2016/5/22.
 */
public class AddFriendActivity extends Activity{

    private LinearLayout linearLayout;
    private Button searchBtn;
    private TextView searchedContactTextView;
    private EditText searchContactField;
    private Activity me;
    private IMUser searchedUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        me = this;
        findView();
        initView();
    }

    private void initView() {
    }

    private void findView() {
        linearLayout = (LinearLayout) findViewById(R.id.ll_add_contact);
        linearLayout.setVisibility(View.GONE);

        searchBtn = (Button) findViewById(R.id.btn_search_contact);

        searchedContactTextView = (TextView) findViewById(R.id.tv_searched_contact);
        searchContactField = (EditText) findViewById(R.id.et_contact);

        Button addContactBtn = (Button) findViewById(R.id.btn_add_contact);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 新常见个线程去从服务器查找对应的IMUser
                // 通过返回的IMUser去获得环信的ID号，然后再用环信的ID发送好友邀请

                final String appUser = searchContactField.getText().toString();

                if (TextUtils.isEmpty(appUser)) {
                    Toast.makeText(AddFriendActivity.this, "搜索的联系人不能为空", Toast.LENGTH_LONG).show();

                    return;
                }

                Model.getInstance().globalThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        // 从服务器上获取app 信息 主要是获取hxid
                        searchedUser = Model.getInstance().getContactHandler().fetchUserFromServer(appUser);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.setVisibility(View.VISIBLE);

                                searchedContactTextView.setText(searchContactField.getText());
                            }
                        });
                    }
                });
            }
        });

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchedUser != null){
                    addContact(searchedUser.getHxId());
                }
            }
        });
    }

    private void addContact(final String hxId){
        Model.getInstance().globalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().addContact(hxId, "加个好友吧");

                    me.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(me, "好友邀请已经发送", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();

                    final String error = e.toString();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(me, "好友邀请发送失败 : " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
