package com.purecode.imapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.purecode.imapp.R;
import com.hyphenate.chat.EMGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by purecode on 16/6/21.
 */
public class MyGroupAdapter extends BaseAdapter {

    private List<EMGroup> groupList = new ArrayList<>();
    private Context context;

    public MyGroupAdapter(Context context){
        this.context = context;
    }

    public void refresh(List<EMGroup> groups){
        groupList.clear();
        groupList.addAll(groups);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(groupList == null){
            return 0;
        }

        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        if(groupList == null){
            return null;
        }

        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyGroupAdapter.ViewHolder viewHolder = null;

        EMGroup group = (EMGroup) getItem(position);

        if(convertView == null){
            viewHolder = new MyGroupAdapter.ViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.row_group_item,null);
            viewHolder.tvGroupName = (TextView) convertView.findViewById(R.id.tv_group_name);
            convertView.setTag(viewHolder);

        }else{
            viewHolder = (MyGroupAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.tvGroupName.setText(group.getGroupName());

        return convertView;
    }

    static class ViewHolder{
        ImageView ivGroupAvatar;
        TextView tvGroupName;
    }
}
