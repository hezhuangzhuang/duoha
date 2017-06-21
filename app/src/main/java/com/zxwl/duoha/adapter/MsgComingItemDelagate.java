package com.zxwl.duoha.adapter;

import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zxwl.duoha.R;
import com.zxwl.duoha.bean.ChatMsg;

/**
 * Created by zhy on 16/6/22.
 * 收到消息的页面
 */
public class MsgComingItemDelagate implements ItemViewDelegate<ChatMsg> {

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_from_msg;
    }

    @Override
    public boolean isForViewType(ChatMsg item, int position) {
//        return item.isComMeg();
        return ChatMsg.TYPE_RECEIVED == item.type;
    }

    @Override
    public void convert(ViewHolder holder, ChatMsg chatMessage, int position) {
        holder.setText(R.id.tv_time,chatMessage.time);
        holder.setText(R.id.tv_name,chatMessage.name);
        holder.setText(R.id.tv_content,chatMessage.content);
//        holder.setText(R.id.chat_from_content, chatMessage.getContent());
//        holder.setText(R.id.chat_from_name, chatMessage.getName());
//        holder.setImageResource(R.id.chat_from_icon, chatMessage.getIcon());
    }
}
