package com.zxwl.duoha.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.esdk.cc.MobileCC;
import com.huawei.esdk.cc.common.BroadMsg;
import com.huawei.esdk.cc.common.NotifyMessage;
import com.jaeger.library.StatusBarUtil;
import com.skyfishjy.library.RippleBackground;
import com.zxwl.duoha.R;
import com.zxwl.duoha.net.Constant;
import com.zxwl.duoha.utils.DisplayUtil;


/**
 * 呼叫等待页面
 */
public class CallingActivity extends BaseActivity implements View.OnClickListener {
    private RippleBackground ripple;
    private ImageView ivHangUp;
    private ImageView ivBackOperate;
    private TextView tvTopTitle;

    private IntentFilter chatFilter;//聊天相关的广播
    private IntentFilter loginFilter;//登录的广播过滤
    private IntentFilter videoFilter;//视频的广播过滤
    private LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

    private boolean hasAudioAbility = false;//正在语音
    private boolean hasTextAbility = false;//正在文字
    private boolean isQueuing = false;//是否排队
    private boolean isAudioConnected = false;//
    private boolean isPause = false;//是否暂停

    private static final int GET_VERIFYCODE = 8;
    private static final int RECEIVE_DATA = 1;//收到消息
    private static final int SEND_DATA = 4;//发送消息
    private static final int CANCELQUEUE = 3;
    private static final int QUEUING = 2;//排队
    private static final int AUDIOCONNECTED = 5;
    private static final int CLOSEDIALOG = 6;
    private int count = 0;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, CallingActivity.class));
    }

    @Override
    protected void initView() {
        ripple = (RippleBackground) findViewById(R.id.ripple);
        ivHangUp = (ImageView) findViewById(R.id.iv_hang_up);
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
    }

    @Override
    protected void initData() {
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.green), 0);
        //设置标题
        tvTopTitle.setText("Calling");
        //注册广播
        initReceiver();
        //设置涟漪的尺寸
        setRippleParams();

        //1.第一步->文字呼叫
        if (0 != MobileCC.getInstance().webChatCall(Constant.ACCESS_CODE, "", "")) {
            Toast.makeText(this, R.string.call_error, Toast.LENGTH_SHORT).show();
            ivHangUp.performClick();
        }
    }

    @Override
    protected void setListener() {
        ivBackOperate.setOnClickListener(this);
        ivHangUp.setOnClickListener(this);
//        ripple.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_calling;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ripple.stopRippleAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ripple.startRippleAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ripple.stopRippleAnimation();

        localBroadcastManager.unregisterReceiver(chatReceiver);
        localBroadcastManager.unregisterReceiver(loginReceiver);
        localBroadcastManager.unregisterReceiver(videoReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_operate:
            case R.id.iv_hang_up:
                logOut();
                break;

            case R.id.ripple:
                //1.第一步->文字呼叫
                if (0 != MobileCC.getInstance().webChatCall(Constant.ACCESS_CODE, "", "")) {
                    Toast.makeText(this, R.string.call_error, Toast.LENGTH_SHORT).show();
                    ivHangUp.performClick();
                }
                break;
        }
    }

    //返回按钮
    @Override
    public void onBackPressed() {
        logOut();
    }

    private void logOut() {
        //如果在排队则取消排队
        if(hasAudioAbility){
            MobileCC.getInstance().releaseCall();
        }
        if(hasAudioAbility){
            MobileCC.getInstance().releaseWebChatCall();
        }
        MobileCC.getInstance().logout();
    }

    /**
     * 初始化广播
     */
    private void initReceiver() {
        //聊天相关的广播
        chatFilter = new IntentFilter();
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_CONNECTED);//与坐席连接成功
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_DISCONNECTED);//与坐席断开连接
        chatFilter.addAction(NotifyMessage.CALL_MSG_USER_STATUS);//会议创建通知
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_QUEUE_INFO);//排队信息
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_DROPCALL);//释放呼叫
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_SUCCESS);//语音接通
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_QUEUE_TIMEOUT);//排队超时
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_FAIL);//呼叫失败
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_CANCEL_QUEUE);//取消排队
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_NET_QUALITY_LEVEL);//w网络状态
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_QUEUING);//正在排队
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_POLL);//轮询时网络异常
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_CONNECT);//获取文字能力
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_APPLY_MEETING);//申请会议时网络异常
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_CALL_END);//呼叫失败
        localBroadcastManager.registerReceiver(chatReceiver, chatFilter);

        //登录相关的广播
        loginFilter = new IntentFilter();
        loginFilter.addAction(NotifyMessage.AUTH_MSG_ON_LOGOUT);
        //注册登录广播
        localBroadcastManager.registerReceiver(loginReceiver, loginFilter);

        //视频的广播
        videoFilter = new IntentFilter();
        videoFilter.addAction(NotifyMessage.CALL_MSG_USER_JOIN);//加入会议
        //注册登录广播
        localBroadcastManager.registerReceiver(videoReceiver, videoFilter);
    }

    /**
     * 视频的广播
     */
    private BroadcastReceiver videoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                //加入会议通知
                case NotifyMessage.CALL_MSG_USER_JOIN:
                    ripple.stopRippleAnimation();

                    //4.第四步->当成功加入会议时,进入聊天界面
                    ChatActivity.startActivity(CallingActivity.this);
                    finish();
                    break;

                default:
                    break;
            }
        }
    };

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

        if (!"CALL_MSG_ON_NET_QUALITY_LEVEL".equals(intent.getAction())) {
            Log.i("Main", "action === " + intent.getAction());
        }

        Message message = null;
        switch (intent.getAction()) {
            //与坐席连接成功
            case NotifyMessage.CALL_MSG_ON_CONNECTED:
                //是否排队的状态为false
                isQueuing = false;
                message = new Message();
                //2.第二步->文字连接成功时,呼叫语音
                if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getRequestInfo().getMsg())) {
                    //获得文字能力
                    hasTextAbility = true;
                    //呼叫语音
                    MobileCC.getInstance().makeCall(Constant.AUDIO_ACCESS_CODE, MobileCC.AUDIO_CALL, "", "");
                }else if (MobileCC.MESSAGE_TYPE_AUDIO.equals(broadMsg.getRequestInfo().getMsg())) {
                    hasAudioAbility = true;
                }
                break;

            //与坐席断开连接
            case NotifyMessage.CALL_MSG_ON_DISCONNECTED:
                message = new Message();
                message.what = RECEIVE_DATA;
                if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getRequestInfo().getMsg())) {
                    message.obj = "文字断开";
                    hasTextAbility = false;
                } else if (MobileCC.MESSAGE_TYPE_AUDIO.equals(broadMsg.getRequestInfo().getMsg())) {
//                    Toast.makeText(CallingActivity.this, "语音能力断开", Toast.LENGTH_SHORT).show();
                    isAudioConnected = false;
                }
                break;

            //释放呼叫
            case NotifyMessage.CALL_MSG_ON_DROPCALL:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(CallingActivity.this, getString(R.string.release_call_failure_code) + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (MobileCC.MESSAGE_OK.equals(broadMsg.getRequestCode().getRetCode())) {
                    Toast.makeText(CallingActivity.this, R.string.release_call_succeed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CallingActivity.this, getString(R.string.release_call_failure_code) + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_SHORT).show();
                }
                break;

            //语音接通,语音连接成功
            case NotifyMessage.CALL_MSG_ON_SUCCESS:
                isAudioConnected = true;

                //3.第三步->当语音呼叫成功时升级成视频
                MobileCC.getInstance().updateToVideo();
                break;

            //排队超时
            case NotifyMessage.CALL_MSG_ON_QUEUE_TIMEOUT:
                Toast.makeText(this, R.string.call_error, Toast.LENGTH_SHORT).show();

                //排队超时进行挂断操作
                ivHangUp.performClick();
                break;

            //呼叫失败
            case NotifyMessage.CALL_MSG_ON_FAIL:
                //呼叫失败显示返回按钮
                isPause = true;
                //显示返回按钮
                Toast.makeText(CallingActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();

                //排队超时进行挂断操作
                ivHangUp.performClick();
                break;

            //获取文字能力
            case NotifyMessage.CALL_MSG_ON_CONNECT:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    //没有收到服务器的retcode，判断是文字还是语音，打印出错误码
                    if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getType())) {
//                        Toast.makeText(CallingActivity.this, "文字连接失败，错误码是：" + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(CallingActivity.this, "语音连接失败，错误码是：" + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_LONG).show();
                        isPause = true;
                    }
                    Toast.makeText(CallingActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();

                    //挂断操作
                    ivHangUp.performClick();
                    return;
                }
                //收到服务器的retcode，判断是文字还是语音，打印出错误码
                if (MobileCC.MESSAGE_OK.equals(broadMsg.getRequestCode().getRetCode())) {
                    //文字连接成功
                    if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getType())) {
//                        Toast.makeText(CallingActivity.this, "获取文字能力,文字连接成功", Toast.LENGTH_LONG).show();
                    } else {//语音连接成功
//                        Toast.makeText(CallingActivity.this, "获取语音能力,语音连接成功", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getType())) {
//                        Toast.makeText(CallingActivity.this, "文字连接失败，错误码是：" + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_LONG).show();
                        //显示返回按钮
                        // btnLeave.setVisibility(View.VISIBLE);
                    } else {
//                        Toast.makeText(CallingActivity.this, "语音连接失败，错误码是：" + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_LONG).show();
                        isPause = true;
                    }
                    Toast.makeText(CallingActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();

                    //挂断操作
                    ivHangUp.performClick();
                }
                break;

            //申请会议时网络异常
            case NotifyMessage.CALL_MSG_ON_APPLY_MEETING:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(CallingActivity.this, "申请会议失败，错误码是" + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(CallingActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();

                    //挂断操作
                    ivHangUp.performClick();
                    return;
                }

                if (!MobileCC.MESSAGE_OK.equals(broadMsg.getRequestCode().getRetCode())) {
                    Toast.makeText(CallingActivity.this, "申请会议失败，错误码是" + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(CallingActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();

                    //挂断操作
                    ivHangUp.performClick();
                }
                break;

            //呼叫失败
            case NotifyMessage.CALL_MSG_ON_CALL_END:
                isPause = true;
                Toast.makeText(CallingActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();

                //挂断操作
                ivHangUp.performClick();
                break;

            default:
                break;
        }
    }

    /**
     * 监听登录，登出，获得验证码的广播
     */
    private BroadcastReceiver loginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setLoginBroadcast(intent);
        }
    };

    /**
     * 登陆的广播监听
     *
     * @param intent
     */
    private void setLoginBroadcast(Intent intent) {
        BroadMsg broadMsg = (BroadMsg) intent
                .getSerializableExtra(NotifyMessage.CC_MSG_CONTENT);

        switch (intent.getAction()) {
            //注销
            case NotifyMessage.AUTH_MSG_ON_LOGOUT:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(this, R.string.logout_error + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_SHORT).show();
                    return;
                }

                //注销成功
                if ("0".equals(broadMsg.getRequestCode().getRetCode())) {
                    Toast.makeText(this, R.string.logout_succeed, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.logout_error + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    private void setRippleParams() {
        int screenWidth = DisplayUtil.getScreenWidth(this);

        ViewGroup.LayoutParams layoutParams = ripple.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenWidth;

        ripple.setLayoutParams(layoutParams);
    }

}
