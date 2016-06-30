package com.purecode.imapp.view.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.purecode.imapp.R;
import com.purecode.imapp.model.datamodel.IMUser;
import com.purecode.imapp.model.datamodel.InvitationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by purecode on 16/6/21.
 */
public class MyInvitationAdapter extends BaseAdapter {
    private final Context context;
    private final OnInvitationListener invitationListener;
    private List<InvitationInfo> inviteInfos;
    private Handler mH = new Handler();

    public MyInvitationAdapter(Context context, OnInvitationListener invitationListener, List<InvitationInfo> inviteInfos){
        inviteInfos = new ArrayList<>();

        this.inviteInfos = new ArrayList<>();
        this.inviteInfos.addAll(inviteInfos);
        this.context = context;
        this.invitationListener = invitationListener;
    }

    @Override
    public int getCount() {
        return inviteInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return inviteInfos.get(position
        );
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MyInvitationAdapter.ViewHolder holder = null;

        final InvitationInfo inviteInfo = inviteInfos.get(position);

        final IMUser user  = inviteInfos.get(position).getUser();

        boolean isGroupInvite = (user == null);
        if(convertView == null){
            holder = new MyInvitationAdapter.ViewHolder();

            convertView = View.inflate(context, R.layout.row_contact_invitation,null);

            holder.name = (TextView) convertView.findViewById(R.id.tv_user_name);
            holder.reason = (TextView) convertView.findViewById(R.id.tv_invite_reason);

            holder.btnAccept = (Button) convertView.findViewById(R.id.btn_accept);
            holder.btnReject = (Button) convertView.findViewById(R.id.btn_reject);

            convertView.setTag(holder);
        }else{
            holder = (MyInvitationAdapter.ViewHolder) convertView.getTag();
        }

        if(!isGroupInvite){
            if(inviteInfo.getStatus() == InvitationInfo.InvitationStatus.NEW_INVITE){
                holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invitationListener.onAccepted(inviteInfo);
                    }
                });

                holder.btnReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invitationListener.onRejected(inviteInfo);
                    }
                });

                if(inviteInfo.getReason() != null){
                    holder.reason.setText(inviteInfo.getReason());
                }else{
                    holder.reason.setText("加个好友吧!");
                }
            }else if(inviteInfo.getStatus() == InvitationInfo.InvitationStatus.INVITE_ACCEPT){
                holder.reason.setText("your added new friend " + user.getNick());

                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            }else if(inviteInfo.getStatus() == InvitationInfo.InvitationStatus.INVITE_ACCEPT_BY_PEER){
                holder.reason.setText(user.getNick() + " accepted your invitation");
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            }

            holder.name.setText(user.getNick());
        }else{// group invitation
            holder.name.setText(inviteInfo.getGroupInfo().getGroupName() + " : " + inviteInfo.getGroupInfo().getInviteTriggerUser());
            holder.btnReject.setVisibility(View.GONE);
            holder.btnAccept.setVisibility(View.GONE);

            switch(inviteInfo.getStatus()){
                case GROUP_APPLICATION_ACCEPTED:
                    holder.reason.setText("您的群申请请已经被接受");
                    break;

                case GROUP_INVITE_ACCEPTED:
                    holder.reason.setText("您的群邀请已经被接收");
                    break;

                case GROUP_APPLICATION_DECLINED:
                    holder.reason.setText("你的群申请已经被拒绝");
                    break;

                case GROUP_INVITE_DECLINED:
                    holder.reason.setText("您的群邀请已经被拒绝");
                    break;

                case NEW_GROUP_INVITE:
                    holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            invitationListener.onGroupInvitationAccept(inviteInfo);
                        }
                    });

                    holder.btnReject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            invitationListener.onGroupInvitationReject(inviteInfo);
                        }
                    });

                    holder.btnReject.setVisibility(View.VISIBLE);
                    holder.btnAccept.setVisibility(View.VISIBLE);

                    holder.reason.setText("您收到了群邀请");
                    break;

                case NEW_GROUP_APPLICATION:
                    holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            invitationListener.onGroupApplicationAccept(inviteInfo);
                        }
                    });

                    holder.btnReject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            invitationListener.onGroupApplicationReject(inviteInfo);
                        }
                    });

                    holder.btnReject.setVisibility(View.VISIBLE);
                    holder.btnAccept.setVisibility(View.VISIBLE);
                    holder.reason.setText("您收到了群申请");
                    break;

                case GROUP_ACCEPT_INVITE:
                    holder.reason.setText("你接受了群邀请");
                    break;

                case GROUPO_ACCEPT_APPLICATION:
                    holder.reason.setText("您批准了群加入");
                    break;
            }
        }

        return convertView;
    }

    public void refresh(final List<InvitationInfo> inviteInfos){
        MyInvitationAdapter.this.inviteInfos.clear();
        MyInvitationAdapter.this.inviteInfos.addAll(inviteInfos);

        mH.removeCallbacks(refreshRunnable);
        mH.post(refreshRunnable);
    }

    Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    static class ViewHolder{
        TextView name;
        TextView reason;
        Button btnAccept;
        Button btnReject;
    }

    public interface OnInvitationListener {
        void onAccepted(InvitationInfo invitationInfo);
        void onRejected(InvitationInfo invitationInfo);
        void onGroupApplicationAccept(InvitationInfo invitationInfo);
        void onGroupInvitationAccept(InvitationInfo invitationInfo);

        void onGroupApplicationReject(InvitationInfo invitationInfo);
        void onGroupInvitationReject(InvitationInfo invitationInfo);
    }
}
