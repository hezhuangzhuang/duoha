package com.zxwl.duoha.adapter;

import android.content.Context;

import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zxwl.duoha.bean.ChatMsg;

import java.util.List;

/**
 * author：hw
 * data:2017/6/6 11:36
 * 聊天界面适配器
 */
public class ChatAdapter extends MultiItemTypeAdapter<ChatMsg> {

    public ChatAdapter(Context context, List<ChatMsg> datas) {
        super(context, datas);
        addItemViewDelegate(new MsgSendItemDelagate());
        addItemViewDelegate(new MsgComingItemDelagate());
    }
}
