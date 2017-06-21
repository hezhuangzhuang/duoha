package com.zxwl.duoha.bean;

/**
 * author：hw
 * data:2017/6/6 11:37
 */

public class ChatMsg {
    /*接收的消息标记*/
    public static final int TYPE_RECEIVED = 0;

    /*发出的消息标记*/
    public static final int TYPE_SENT = 1;

    /*消息时间*/
    public String time;

    /*发送消息的名称*/
    public String name;

    /*消息内容*/
    public String content;

    /*消息类型：接收或者发出的*/
    public int type;

    public ChatMsg(String time, String name, String content, int type) {
        this.time = time;
        this.name = name;
        this.content = content;
        this.type = type;
    }
    //    public ChatMsg(String content, int type) {
//        this.content = content;
//        this.type = type;
//    }
}
