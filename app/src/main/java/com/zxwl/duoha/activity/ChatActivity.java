package com.zxwl.duoha.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.esdk.cc.MobileCC;
import com.huawei.esdk.cc.common.BroadMsg;
import com.huawei.esdk.cc.common.NotifyMessage;
import com.jaeger.library.StatusBarUtil;
import com.lcodecore.tkrefreshlayout.utils.DensityUtil;
import com.zxwl.duoha.R;
import com.zxwl.duoha.adapter.ChatAdapter;
import com.zxwl.duoha.bean.ChatMsg;
import com.zxwl.duoha.utils.DateUtil;
import com.zxwl.duoha.utils.DisplayUtil;
import com.zxwl.duoha.utils.statusbar.SystemBarHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 通话界面
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout rlRoot;//根布局

    private FrameLayout rlVideo;//视频播放的布局
    private View controller;//控制按钮的父布局

    private FrameLayout flChat;
    private View redView;//消息提醒
    private TextView tvChat;//聊天
    private ImageView ivVolume;//音量
    private ImageView ivSwitchScreen;//切换视频
    private ImageView ivFullScreen;//全屏
    private TextView tvHangUp;//挂断

    private FrameLayout flRemote;//远程视频源
    private FrameLayout flLocal;//本地视频源

    private TextView tvSend;//发送
    private EditText etContent;//输入框

    private View topTitle;//顶部toolbar
    private ImageView ivBackOperate;//返回按钮
    private TextView tvTopTitle;//标题
    private ImageView ivRightOperate;//回到视频

    /*聊天列表-start*/
    private RecyclerView rvList;
    private ChatAdapter chatAdapter;
    private List<ChatMsg> chatMsgs = new ArrayList<>();
    /*聊天列表-end*/

    private IntentFilter loginFilter;//登录登出的广播
    private IntentFilter videoFilter;//视频相关的广播
    private IntentFilter chatFilter;//聊天相关的广播
    private LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

    private boolean mBackEnable = false;
    private boolean mIsBtnBack = false;
    private int rootBottom = Integer.MIN_VALUE;

    private boolean hasTextAbility = true;//正在文字

    private boolean isPause = false;//是否暂停

    private static final int RECEIVE_DATA = 1;//收到消息
    private static final int SEND_DATA = 4;//发送消息
    private static final int QUEUING = 2;//排队
    private static final int CLOSEDIALOG = 6;

    private boolean isPortrait = true;//是否是竖屏

    // 软键盘的显示状态
    private boolean ShowKeyboard;

    private boolean remoteTop = false;//聊天的大窗口在上,默认为false
    private boolean isMute = false;//是否静音
    private boolean isSend = false;//是否正在发送文本

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ChatActivity.class));
    }

    @Override
    protected void initView() {
        topTitle = findViewById(R.id.include_title);
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        ivRightOperate = (ImageView) findViewById(R.id.iv_right_operate);

        rlRoot = (LinearLayout) findViewById(R.id.rl_root);
        flRemote = (FrameLayout) findViewById(R.id.fl_remote);
        flLocal = (FrameLayout) findViewById(R.id.fl_local);
        rvList = (RecyclerView) findViewById(R.id.rv_list);
        tvSend = (TextView) findViewById(R.id.tv_send);
        etContent = (EditText) findViewById(R.id.et_content);
        rlVideo = (FrameLayout) findViewById(R.id.rl_video);

        controller = findViewById(R.id.controller);
        flChat = (FrameLayout) findViewById(R.id.fl_chat);
        redView = (View) findViewById(R.id.red_view);
        tvChat = (TextView) findViewById(R.id.tv_chat);
        ivVolume = (ImageView) findViewById(R.id.iv_volume);
        ivSwitchScreen = (ImageView) findViewById(R.id.iv_switch_screen);
        ivFullScreen = (ImageView) findViewById(R.id.iv_full_screen);
        tvHangUp = (TextView) findViewById(R.id.tv_hang_up);
    }

    @Override
    protected void initData() {
        /*-------------------5.0-------------------------*/
        /**
         * 5.0版本正常
         */
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.green), 0);
         /*-------------------5.0-------------------------*/


        //        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.green), 0);
//        StatusBarUtil.setTranslucentForImageView(this,0,topTitle);
//        StatusBarUtil.setTranslucent(this,0);
//        StatusBarUtil.setTranslucentForImageView(this, 0, topTitle);

//        SystemUiVisibilityUtil.hideStatusBar(getWindow(), true);
//        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) topTitle.getLayoutParams();
//        layoutParams.topMargin = DisplayUtil.dp2px(this,25);
//        topTitle.setLayoutParams(layoutParams);

        // 保持屏幕常亮
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        ivBackOperate.setImageResource(R.mipmap.icon_close);//返回按钮
        tvTopTitle.setText("title");//标题
        ivRightOperate.setImageResource(R.mipmap.icon_video);//回到视频

        //初始化广播
        initReceiver();

        //初始化列表
        initRecycler();

        //给最外层布局添加布局变化监听
        rlRoot.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

        Toast.makeText(this, "成功加入会议", Toast.LENGTH_SHORT).show();
        //设置远端与本地的画面
        MobileCC.getInstance().setVideoContainer(getApplication().getApplicationContext(), flLocal, flRemote);
    }

    @Override
    protected void setListener() {
        flLocal.setOnClickListener(this);//
        flRemote.setOnClickListener(this);

        ivBackOperate.setOnClickListener(this);//返回按钮
        ivRightOperate.setOnClickListener(this);//显示视频
        tvSend.setOnClickListener(this);//发送按钮
        tvChat.setOnClickListener(this);//回到聊天
        ivVolume.setOnClickListener(this);//音量
        ivSwitchScreen.setOnClickListener(this);//切换视频
        ivFullScreen.setOnClickListener(this);//全屏
        tvHangUp.setOnClickListener(this);//挂断
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    public void onBackPressed() {
        if (isPortrait) {
            logOut();
        } else {
            //设置为竖屏
            setViewPortrait();
        }
    }

    private void logOut() {
        //释放文字
//        MobileCC.getInstance().releaseWebChatCall();
        //1.第一步->释放呼叫
        MobileCC.getInstance().releaseCall();
    }

    /**
     * 初始化广播
     */
    private void initReceiver() {
        //聊天相关的广播
        chatFilter = new IntentFilter();
        chatFilter.addAction(NotifyMessage.CHAT_MSG_ON_RECEIVE);//接收消息
        chatFilter.addAction(NotifyMessage.CHAT_MSG_ON_SEND);//发送消息
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_DISCONNECTED);//与坐席断开连接
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_POLL);//轮询时网络异常
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_DROPCALL);//释放呼叫
        //注册聊天广播
        localBroadcastManager.registerReceiver(chatReceiver, chatFilter);

        //视频相关的会议
        videoFilter = new IntentFilter();
        videoFilter.addAction(NotifyMessage.CALL_MSG_USER_NETWORK_ERROR);//会议断线通知
        videoFilter.addAction(NotifyMessage.CALL_MSG_USER_END);//会议终止通知
        videoFilter.addAction(NotifyMessage.CALL_MSG_ON_STOP_MEETING);//退出会议时
        //注册视频广播
        localBroadcastManager.registerReceiver(videoReceiver, videoFilter);
    }

    /**
     * 聊天的广播
     */
    private BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setChatReceive(intent);
        }
    };

    /**
     * 设置聊天的广播
     *
     * @param intent
     */
    private void setChatReceive(Intent intent) {
        BroadMsg broadMsg = (BroadMsg) intent
                .getSerializableExtra(NotifyMessage.CC_MSG_CONTENT);
        if (null == broadMsg) {
            return;
        }

        Log.i("Main","chatActivity==="+intent.getAction());

        Message message = null;
        switch (intent.getAction()) {
            //收到IM消息
            case NotifyMessage.CHAT_MSG_ON_RECEIVE:
                //收到IM消息
                message = new Message();
                message.what = RECEIVE_DATA;
                message.obj = broadMsg.getRequestInfo().getMsg();
                handler.sendMessage(message);
                //当时横屏的时候则出现小红点
                if (!isPortrait) {
                    redView.setVisibility(View.VISIBLE);
                }
                break;

            //发送消息
            case NotifyMessage.CHAT_MSG_ON_SEND:
                //接收到发送消息的广播则将是否发送消息的标志标为false
                isSend = false;
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(ChatActivity.this, "文字发送失败，错误码码是：" + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_LONG).show();
                    return;
                }
                //retcode不为为空时
                String retcode = broadMsg.getRequestCode().getRetCode();
                if (MobileCC.MESSAGE_OK.equals(retcode)) {
                    //消息发送成功
                    String content = broadMsg.getRequestInfo().getMsg();
                    if (!"".equals(content)) {
                        message = new Message();
                        message.what = SEND_DATA;
                        message.obj = content;
                        handler.sendMessage(message);
                    }
                } else {
                    //消息发送未成功
                    Toast.makeText(ChatActivity.this, "文字发送失败，错误码码是：" + retcode, Toast.LENGTH_LONG).show();
                }
                break;

            //与坐席断开连接
            case NotifyMessage.CALL_MSG_ON_DISCONNECTED:
                message = new Message();
                message.what = RECEIVE_DATA;
                if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getRequestInfo().getMsg())) {
                    message.obj = "文字断开";
                    hasTextAbility = false;
                    handler.sendMessage(message);

                    //3.第三步->与坐席断开连接之后退出登录
                    MobileCC.getInstance().logout();
                    finish();
                } else if (MobileCC.MESSAGE_TYPE_AUDIO.equals(broadMsg.getRequestInfo().getMsg())) {
                    Toast.makeText(ChatActivity.this, "语音能力断开", Toast.LENGTH_SHORT).show();
                }
                break;

            //语音连接成功
            case NotifyMessage.CALL_MSG_ON_SUCCESS:
                Toast.makeText(this, "语音连接成功", Toast.LENGTH_SHORT).show();
                break;

            //轮询时网络异常
            case NotifyMessage.CALL_MSG_ON_POLL:
                int status = broadMsg.getRequestCode().getErrorCode();
                isPause = true;
                break;

            default:
                break;
        }
    }

    /**
     * 视频的广播
     */
    private BroadcastReceiver videoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //会议断线通知
                case NotifyMessage.CALL_MSG_USER_NETWORK_ERROR:
                    break;

                //会议终止通知
                case NotifyMessage.CALL_MSG_USER_END:
                    //2.第二步->视频结束之后释放文字
                    MobileCC.getInstance().releaseWebChatCall();
                    break;

                //退出会议时
                case NotifyMessage.CALL_MSG_ON_STOP_MEETING:
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //发送
            case R.id.tv_send:
                //如果当前没有发送文本则发送
                if (!isSend) {
                    if (!hasTextAbility) {
                        Toast.makeText(this, R.string.send_text_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //获得输入内容,判断是否为空
                    String content = etContent.getText().toString().trim();
                    if (TextUtils.isEmpty(content)) {
                        Toast.makeText(this, R.string.content_cannot_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //正在发送
                    isSend = true;
                    MobileCC.getInstance().sendMsg(content);
                }
                break;

            //本地视频
            case R.id.fl_local:
                break;

            //远端视频
            case R.id.fl_remote:
                break;

            //关闭界面
            case R.id.iv_back_operate:
            case R.id.tv_hang_up:
                isPortrait = true;
                onBackPressed();
                break;

            //显示视频
            case R.id.iv_right_operate:
                //隐藏键盘
                hideKeyboard();
                break;

            //设置音量
            case R.id.iv_volume:
                isMute = !isMute;
                if (isMute) {
                    MobileCC.getInstance().setMicMute(true);
                    ivVolume.setImageResource(R.mipmap.icon_mute);
                } else {
                    MobileCC.getInstance().setMicMute(false);
                    ivVolume.setImageResource(R.mipmap.icon_volume);
                }
                break;

            //回到聊天
            case R.id.tv_chat:
                //设置为竖屏
                setViewPortrait();
                break;

            //切换屏幕
            case R.id.iv_switch_screen:
                if (remoteTop) {
                    if (flLocal.getChildCount() > 0) {
                        flLocal.getChildAt(0).setVisibility(View.GONE);
                    }
                } else {
                    if (flLocal.getChildCount() > 0) {
                        flLocal.getChildAt(0).setVisibility(View.VISIBLE);
                    }
                }
                remoteTop = !remoteTop;

                break;

            //全屏切换
            case R.id.iv_full_screen:
                if (isPortrait) {//当前为竖屏
                    //设置为横屏
                    setViewLandscape();
                } else {//当前为横屏
                    setViewPortrait();
                }
                break;

            default:
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handlerSwitch(msg);
        }
    };

    private void handlerSwitch(Message msg) {
        ChatMsg chatMsg = null;
        switch (msg.what) {
            //接收数据
            case RECEIVE_DATA:
                chatMsg = new ChatMsg(DateUtil.getCurrentTime("HH:MM"), "zaida", (String) msg.obj, ChatMsg.TYPE_RECEIVED);
                chatMsgs.add(chatMsg);
                chatAdapter.notifyDataSetChanged();
                rvList.smoothScrollToPosition(chatMsgs.size());
                break;

            //排队
            case QUEUING:
                chatMsg = new ChatMsg(DateUtil.getCurrentTime("HH:MM"), "zaida", (String) msg.obj, ChatMsg.TYPE_RECEIVED);
                chatMsgs.add(chatMsg);
                chatAdapter.notifyDataSetChanged();
                rvList.smoothScrollToPosition(chatMsgs.size());
                break;

            //发送数据
            case SEND_DATA:
                etContent.setText("");
                chatMsg = new ChatMsg(DateUtil.getCurrentTime("HH:MM"), "Me", (String) msg.obj, ChatMsg.TYPE_SENT);
                chatMsgs.add(chatMsg);
                chatAdapter.notifyDataSetChanged();
                rvList.smoothScrollToPosition(chatMsgs.size());
                break;

            //隐藏dialog
            case CLOSEDIALOG:
                break;

            default:
                break;
        }
    }

    /**
     * 设置为竖屏
     */
    private void setViewPortrait() {
         /*-------------------5.0-------------------------*/
        /**
         * 5.0正常
         */
//        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.green), 0);
         /*-------------------5.0-------------------------*/

        //设置状态栏颜色
//        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.green), 0);
//        StatusBarUtil.setTranslucentForImageView(this, 255, null);
//        SystemUiVisibilityUtil.hideStatusBar(getWindow(), false);

        //SDK==19
//        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.green), 0);
//        ViewGroup.MarginLayoutParams topTitleParams = (ViewGroup.MarginLayoutParams) topTitle.getLayoutParams();
//        topTitleParams.topMargin = DisplayUtil.dp2px(this,25);
//        topTitle.setLayoutParams(topTitleParams);


        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) flLocal.getLayoutParams();
        layoutParams.topMargin = DisplayUtil.dp2px(this, 10);
        flLocal.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams fl_lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                DensityUtil.dp2px(ChatActivity.this, 200)
        );
        rlVideo.setLayoutParams(fl_lp);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        isPortrait = true;

        //隐藏聊天按钮和小红点
        flChat.setVisibility(View.GONE);
        redView.setVisibility(View.GONE);

        //显示toolbar
        topTitle.setVisibility(View.VISIBLE);
        //摄像头角度设为270
        MobileCC.getInstance().setVideoRotate(270);
    }

    /**
     * 设置为横屏
     */
    private void setViewLandscape() {
        /*---------------5.0----------------*/
        /**
         * 5.0正常
         */
//        StatusBarUtil.setTranslucentForImageView(this, 0, rlVideo);
        /*---------------5.0----------------*/

//        StatusBarUtil.hideFakeStatusBarView(this);
//        StatusBarUtil.setTranslucentForImageView(this, 0, null);
//        StatusBarUtil.setTranslucent(this, 0);
//        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorAccent), 0);
//        StatusBarUtil.setTranslucent(this);
//        SystemUiVisibilityUtil.hideStatusBar(getWindow(), true);

//sdk==19
//        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorAccent), 0);
//        ViewGroup.MarginLayoutParams topTitleParams = (ViewGroup.MarginLayoutParams) topTitle.getLayoutParams();
//        topTitleParams.topMargin = 0;
//        topTitle.setLayoutParams(topTitleParams);

//        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) flLocal.getLayoutParams();
//        layoutParams.topMargin = SystemBarHelper.getStatusBarHeight(ChatActivity.this) + DisplayUtil.dp2px(this, 10);
//        flLocal.setLayoutParams(layoutParams);

        //切换成横屏
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                DisplayUtil.getScreenHeight(ChatActivity.this),
                DisplayUtil.getScreenWidth(ChatActivity.this)
                        - SystemBarHelper.getStatusBarHeight(ChatActivity.this)
        );
        //设置activity为横屏
        rlVideo.setLayoutParams(screenParams);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isPortrait = false;

        //显示聊天按钮
        flChat.setVisibility(View.VISIBLE);

        //隐藏显示toolbar
        topTitle.setVisibility(View.GONE);

        //摄像头角度设为0
        MobileCC.getInstance().setVideoRotate(0);
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        //收起小键盘
        View view = ChatActivity.this.getCurrentFocus();
        IBinder binder = null;
        if (view == null) {
            return;
        } else {
            binder = view.getWindowToken();
        }

        if (binder == null) {
            return;
        }
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);

        rvList.smoothScrollToPosition(chatAdapter.getItemCount());
    }

    /**
     * 键盘的显示隐藏监听
     */
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            //判断键盘是否显示
            setKeyboardShow();
        }
    };

    /**
     * 判断键盘是否显示
     */
    private void setKeyboardShow() {
        // 应用可以显示的区域。此处包括应用占用的区域，包括标题栏不包括状态栏
        Rect r = new Rect();
        rlRoot.getWindowVisibleDisplayFrame(r);
        // 键盘最小高度
        int minKeyboardHeight = 150;
        // 获取状态栏高度
        int statusBarHeight = SystemBarHelper.getStatusBarHeight(ChatActivity.this);
        // 屏幕高度,不含虚拟按键的高度
        int screenHeight = rlRoot.getRootView().getHeight();
        // 在不显示软键盘时，height等于状态栏的高度
        int height = screenHeight - (r.bottom - r.top);

        if (ShowKeyboard) {
            // 如果软键盘是弹出的状态，并且height小于等于状态栏高度，
            // 说明这时软键盘已经收起
            if (height - statusBarHeight < minKeyboardHeight) {
                ShowKeyboard = false;
                //键盘隐藏了
                //显示视频播放界面
                rlVideo.setVisibility(View.VISIBLE);

                //本地视频显示
                flLocal.setVisibility(View.VISIBLE);
                if (flLocal.getChildCount() > 0) {
                    flLocal.getChildAt(0).setVisibility(View.VISIBLE);
                }

                //隐藏右上角按钮
                ivRightOperate.setVisibility(View.GONE);
            }
        } else {
            // 如果软键盘是收起的状态，并且height大于状态栏高度，
            // 说明这时软键盘已经弹出
            if (height - statusBarHeight > minKeyboardHeight) {
                ShowKeyboard = true;
                //键盘显示了
                //隐藏视频播放界面
                rlVideo.setVisibility(View.GONE);

                //本地视频隐藏
                flLocal.setVisibility(View.GONE);
                if (flLocal.getChildCount() > 0) {
                    flLocal.getChildAt(0).setVisibility(View.GONE);
                }

                //显示右上角按钮
                ivRightOperate.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 初始化recyclerview
     */
    private void initRecycler() {
        //聊天列表
        chatAdapter = new ChatAdapter(this, chatMsgs);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(chatAdapter);
    }


    @Override
    protected void onDestroy() {
        if (chatReceiver != null) {
            localBroadcastManager.unregisterReceiver(chatReceiver);
        }

        if (null != videoReceiver) {
            localBroadcastManager.unregisterReceiver(videoReceiver);
        }
        super.onDestroy();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            rlRoot.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
        } else {
            rlRoot.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        }
    }
}
