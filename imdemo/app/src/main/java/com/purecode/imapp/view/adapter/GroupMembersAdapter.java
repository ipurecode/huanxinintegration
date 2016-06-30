package com.purecode.imapp.view.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.purecode.imapp.R;
import com.purecode.imapp.model.datamodel.IMUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by purecode on 16/6/22.
 */
public class GroupMembersAdapter extends BaseAdapter{
    private static final String TAG = "GroupMembersAdapter";
    private boolean deleteModel = false;
    private Context context;
    private OnGroupMembersListener groupMembersListener;
    private boolean canAddMember;
    private Handler mH = new Handler();

    private List<IMUser> members = new ArrayList<>();

    public GroupMembersAdapter(Context context, OnGroupMembersListener listener, boolean canAddMember){
        this.canAddMember = canAddMember;
        groupMembersListener = listener;
        this.context = context;
    }

    public void refresh(List<IMUser> aMembers){
        if(aMembers != null){
            members.clear();
            members.addAll(aMembers);
        }

        Log.d(TAG,"members : " + members);
        mH.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setDeleteModel(boolean deleteModel){
        this.deleteModel = deleteModel;
    }

    public boolean getDeleteModel(){
        return deleteModel;
    }

    @Override
    public int getCount() {
        return members.size() + 2;
    }

    @Override
    public IMUser getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if(convertView == null){
            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_member,null);

            viewHolder.memberName = (TextView) convertView.findViewById(R.id.tv_member_name);
            viewHolder.avartar = (ImageView) convertView.findViewById(R.id.iv_member_avartar);
            viewHolder.deleteView = (ImageView) convertView.findViewById(R.id.iv_member_delete);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //
        if(position == getCount()-1){
            viewHolder.avartar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!deleteModel){
                        deleteModel = true;
                        notifyDataSetChanged();
                    }
                }
            });
        }else if(position == getCount()-2){
            viewHolder.avartar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!deleteModel){
                        groupMembersListener.onAddMember();
                    }
                }
            });
        }else{
            viewHolder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(deleteModel){
                        groupMembersListener.onDeleteMember(getItem(position));
                    }
                }
            });
        }

        //
        if(position == getCount()-1){
            // 减成员的button

            if(deleteModel || !canAddMember){
                convertView.setVisibility(View.GONE);
            }else{
                convertView.setVisibility(View.VISIBLE);
                viewHolder.memberName.setText("");
                viewHolder.avartar.setImageResource(R.drawable.em_smiley_minus_btn);
                viewHolder.deleteView.setVisibility(View.GONE);
            }

        }else if(position == getCount() -2){
            // 添加成员button

            if(deleteModel || !canAddMember){
                convertView.setVisibility(View.GONE);
            }else {
                convertView.setVisibility(View.VISIBLE);
                viewHolder.memberName.setText("");
                viewHolder.avartar.setImageResource(R.drawable.em_smiley_add_btn);
                viewHolder.deleteView.setVisibility(View.GONE);
            }
        }else{
            convertView.setVisibility(View.VISIBLE);

            IMUser member = getItem(position);

            Log.d(TAG,"IMUser member at postion : " + position + " : " + member);
            viewHolder.memberName.setText(member.getNick());
            viewHolder.avartar.setImageResource(R.drawable.em_default_avatar);

            if(member.getAvartar() != null){
                //可以用第三方图片加载库，加载头像,例如 Glide
                //此处没有实现
            }else{
                viewHolder.avartar.setImageResource(R.drawable.em_default_avatar);
            }

            if(deleteModel){
                viewHolder.deleteView.setVisibility(View.VISIBLE);
            }else{
                viewHolder.deleteView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    static class ViewHolder{
        TextView memberName;
        ImageView avartar;
        ImageView deleteView;
    }

    public interface OnGroupMembersListener{
        void onAddMember();
        void onDeleteMember(IMUser member);
    }
}
