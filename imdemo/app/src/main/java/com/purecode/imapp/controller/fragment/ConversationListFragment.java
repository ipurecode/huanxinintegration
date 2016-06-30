package com.purecode.imapp.controller.fragment;

import android.content.Intent;

import com.purecode.imapp.controller.activity.ChatActivity;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseConversationListFragment;

/**
 * Created by purecode on 2016/5/19.
 */
public class ConversationListFragment extends EaseConversationListFragment {

    @Override
    public void initView(){
        super.initView();

        setConversationListItemClickListener(new EaseConversationListItemClickListener() {
            @Override
            public void onListItemClicked(EMConversation conversation) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                intent.putExtra(EaseConstant.EXTRA_USER_ID,conversation.conversationId());

                if(conversation.getType() == EMConversation.EMConversationType.GroupChat){
                    intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE,EaseConstant.CHATTYPE_GROUP);
                }

                getActivity().startActivity(intent);
            }
        });
    }
}
