package com.purecode.imapp.controller.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.purecode.imapp.R;
import com.purecode.imapp.model.Model;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.exceptions.HyphenateException;

/**
 * Created by purecode on 16/6/20.
 */
public class NewGroupActivity extends Activity {

    private EditText groupNameField;
    private EditText groupDescField;
    private CheckBox isPublicCB;
    private CheckBox isOpenInvitationCB;
    private Activity me;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        me = this;
        findView();
        initView();
    }

    private void initView() {
        Button createGroupBtn = (Button) findViewById(R.id.btn_group_create);

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 进通讯录选人
                startActivityForResult(new Intent(NewGroupActivity.this, GroupPickContactsActivity.class), 0);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            createGroup(data.getStringArrayExtra("newmembers"));
        }

        super.onActivityResult(requestCode, resultCode, data);


    }

    private void createGroup(final String[] members){

        final String groupName = groupNameField.getText().toString();
        final String groupDesc = groupDescField.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(me);
        progressDialog.show();

        Model.getInstance().globalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                EMGroupManager.EMGroupStyle groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;

                if (isPublicCB.isChecked()) {
                    if (isOpenInvitationCB.isChecked()) {
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePublicOpenJoin;
                    } else {
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePublicJoinNeedApproval;
                    }
                } else {
                    if (isOpenInvitationCB.isChecked()) {
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
                    } else {
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePublicJoinNeedApproval;
                    }
                }

                EMGroupManager.EMGroupOptions option = new EMGroupManager.EMGroupOptions();
                option.maxUsers = 200;
                option.style = groupStyle;

                String reason = "invite you to join the group";

                try {
                    EMClient.getInstance().groupManager().createGroup(groupName, groupDesc, members, reason, option);
                    me.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(me, "群 : " + groupName + " 被创建成功", Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                        }

                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();

                    final String error = e.toString();
                    me.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(me, "群 : " + groupName + " 创建失败 ： " + error, Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                        }
                    });
                }
            }
        });
    }

    private void findView() {
        groupNameField = (EditText) findViewById(R.id.et_group_name);

        groupDescField = (EditText) findViewById(R.id.et_group_desc);

        isPublicCB = (CheckBox) findViewById(R.id.cb_is_public_group);
        isOpenInvitationCB = (CheckBox) findViewById(R.id.cb_open_invitation);
        
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
