package com.purecode.imapp.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.purecode.imapp.R;
import com.purecode.imapp.view.adapter.MyGroupAdapter;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.EaseConstant;

/**
 * Created by purecode on 16/6/20.
 */
public class GroupListActivity extends Activity {
    private ListView lvGroup;
    private LinearLayout llHeaderView;
    private MyGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_list);

        findView();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    private void initView() {
        adapter = new MyGroupAdapter(this);

        lvGroup.setAdapter(adapter);

        llHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // try to create a new group

                startActivity(new Intent(GroupListActivity.this, NewGroupActivity.class));
            }
        });

        lvGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 我们用position-1代表，第一个位置是创建群组的Item
                int index = position -1;
                Intent startChat = new Intent(GroupListActivity.this,ChatActivity.class);

                String groupId = EMClient.getInstance().groupManager().getAllGroups().get(index).getGroupId();

                startChat.putExtra(EaseConstant.EXTRA_USER_ID,groupId);
                startChat.putExtra(EaseConstant.EXTRA_CHAT_TYPE,EaseConstant.CHATTYPE_GROUP);
                startActivity(startChat);
                finish();
            }
        });

        refresh();
    }

    private void findView() {
        lvGroup = (ListView) findViewById(R.id.lv_group_list);

        llHeaderView = (LinearLayout) View.inflate(this, R.layout.activity_group_list_header_view,null);

        lvGroup.addHeaderView(llHeaderView);

    }

    private void refresh(){
        adapter.refresh(EMClient.getInstance().groupManager().getAllGroups());
    }
}

