package com.zxwl.duoha.adapter;

import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zxwl.duoha.R;
import com.zxwl.duoha.bean.ChatMsg;

/**
 * Created by zhy on 16/6/22.
 */
public class MsgSendItemDelagate implements ItemViewDelegate<ChatMsg> {

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_send_msg;
    }

    @Override
    public boolean isForViewType(ChatMsg item, int position) {
//        return !item.isComMeg();
        return ChatMsg.TYPE_SENT == item.type;
    }

    @Override
    public void convert(ViewHolder holder, ChatMsg chatMessage, int position) {
        holder.setText(R.id.tv_time,chatMessage.time);//时间
        holder.setText(R.id.tv_name,"Me");//名字
        holder.setText(R.id.tv_content,chatMessage.content);//消息内容
//        holder.setText(R.id.chat_send_content, chatMessage.getContent());
//        holder.setText(R.id.chat_send_name, chatMessage.getName());
//        holder.setImageResource(R.id.chat_send_icon, chatMessage.getIcon());
    }
}
