package com.zxwl.duoha.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.esdk.cc.MobileCC;
import com.huawei.esdk.cc.common.BroadMsg;
import com.huawei.esdk.cc.common.NotifyMessage;
import com.jaeger.library.StatusBarUtil;
import com.zxwl.duoha.AppManager;
import com.zxwl.duoha.R;
import com.zxwl.duoha.bean.UserInfo;
import com.zxwl.duoha.fragment.LoginFragment;
import com.zxwl.duoha.fragment.RegisterFragment;
import com.zxwl.duoha.net.Constant;
import com.zxwl.duoha.rx.RxBus;
import com.zxwl.duoha.utils.DisplayUtil;
import com.zxwl.duoha.utils.sharedpreferences.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 登录页面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private View includeTitleLayout;
    private ImageView ivBackOperate;//返回按钮
    private TextView tvTopTitle;//标题
    private TextView tvLoginTab;//登录的tab
    private TextView tvSignTab;//注册的tab
    private View moveLine;//下面移动的线
    private ViewPager vpContent;//

    private TextView tvArab;
    private TextView tvEnglish;

    private LoginAdapter loginAdapter;
    private List<Fragment> fragmentList;

    private int lineWidth = -1;//线的宽度
    private int cuttentIndex = 0;//当前下标

    private Subscription subscription;

    private IntentFilter loginFilter;//登录登出的广播
    private LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    private int count = 0;
    private IntentFilter chatFilter;

    /**
     * 判断是否已经点击过一次回退键
     */
    private boolean isBackPressed = false;

    @Override
    protected void initView() {
        includeTitleLayout = findViewById(R.id.include_title);
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        tvLoginTab = (TextView) findViewById(R.id.tv_login_tab);
        tvSignTab = (TextView) findViewById(R.id.tv_sign_tab);
        moveLine = (View) findViewById(R.id.move_line);
        vpContent = (ViewPager) findViewById(R.id.vp_content);

        tvArab = (TextView) findViewById(R.id.tv_arab);
        tvEnglish = (TextView) findViewById(R.id.tv_english);
    }

    @Override
    protected void initData() {
        StatusBarUtil.setTranslucentForImageView(LoginActivity.this, 0, includeTitleLayout);

        //初始化viewpager
        initPager();
        //设置顶部标题
        tvTopTitle.setText(getString(R.string.login));
        ivBackOperate.setVisibility(View.GONE);

        includeTitleLayout.setBackgroundColor(Color.parseColor("#00000000"));
        subscription = RxBus.getInstance()
                .toObserverable(UserInfo.class)
                .compose(this.<UserInfo>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserInfo>() {
                    @Override
                    public void call(UserInfo userInfo) {
                        vpContent.setCurrentItem(0);
                    }
                });

        //切换语言
//        switchLanguage(Constant.ARAB);
        //注册广播
        initReceiver();

        setLineWidth();
    }

    /**
     * 注册广播
     */
    private void initReceiver() {
        //登录相关的广播
        loginFilter = new IntentFilter();
        loginFilter.addAction(NotifyMessage.AUTH_MSG_ON_LOGIN);
        loginFilter.addAction(NotifyMessage.AUTH_MSG_ON_LOGOUT);
        //注册登录广播
        localBroadcastManager.registerReceiver(loginReceiver, loginFilter);

        chatFilter = new IntentFilter();
        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_CONNECTED);//与坐席连接成功
//        chatFilter.addAction(NotifyMessage.CALL_MSG_ON_DISCONNECTED);//与坐席断开连接
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
//        localBroadcastManager.registerReceiver(chatReceiver, chatFilter);
    }

    @Override
    protected void setListener() {
        tvLoginTab.setOnClickListener(this);//登录的tab
        tvSignTab.setOnClickListener(this);//注册

        ivBackOperate.setOnClickListener(this);//返回按钮
        tvArab.setOnClickListener(this);//阿拉伯语
        tvEnglish.setOnClickListener(this);//英语
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //登录
            case R.id.tv_login_tab:
                vpContent.setCurrentItem(0);
                break;

            //注册
            case R.id.tv_sign_tab:
                vpContent.setCurrentItem(1);
                break;

            //返回按钮
            case R.id.iv_back_operate:
                vpContent.setCurrentItem(0);
                MobileCC.getInstance().logout();
                break;

            //阿拉伯语
            case R.id.tv_arab:
                setLanguageTextColor(0);
                break;

            //英语
            case R.id.tv_english:
                setLanguageTextColor(1);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (0 == lineWidth) {
            //设置线的宽度
            setLineWidth();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != subscription && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        localBroadcastManager.unregisterReceiver(loginReceiver);
//        localBroadcastManager.unregisterReceiver(chatReceiver);
        MobileCC.getInstance().logout();
//        android.os.Process.killProcess(android.os.Process.myPid());
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

        Log.i("Main", "Calling-->" + intent.getAction());

        Message message = null;
        switch (intent.getAction()) {
            //与坐席连接成功
            case NotifyMessage.CALL_MSG_ON_CONNECTED:
                //是否排队的状态为false
                message = new Message();
                //文字连接成功时
                if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getRequestInfo().getMsg())) {
                    //获得文字能力
                    //呼叫视频
                    //int videoCode = MobileCC.getInstance().makeCall(Constant.AUDIO_ACCESS_CODE, MobileCC.VIDEO_CALL, "", "");
                    //TODO 打包时删除
                    //Toast.makeText(this, getString(R.string.video_return_code) + videoCode, Toast.LENGTH_SHORT).show();

                } else if (MobileCC.MESSAGE_TYPE_AUDIO.equals(broadMsg.getRequestInfo().getMsg())) {
                }
                break;

            //与坐席断开连接
            case NotifyMessage.CALL_MSG_ON_DISCONNECTED:
                message = new Message();
                if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getRequestInfo().getMsg())) {
                    message.obj = "文字断开";
                } else if (MobileCC.MESSAGE_TYPE_AUDIO.equals(broadMsg.getRequestInfo().getMsg())) {
//                    Toast.makeText(this, "语音能力断开", Toast.LENGTH_SHORT).show();
                }
                break;

            //会议创建成功
            case NotifyMessage.CALL_MSG_USER_STATUS:
                Log.i("Main", "会议创建成功");
                break;

            //释放呼叫
            case NotifyMessage.CALL_MSG_ON_DROPCALL:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(this, getString(R.string.release_call_failure_code) + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (MobileCC.MESSAGE_OK.equals(broadMsg.getRequestCode().getRetCode())) {
                    Toast.makeText(this, R.string.release_call_succeed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.release_call_failure_code) + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_SHORT).show();
                }
                break;

            //语音接通,语音连接成功
            case NotifyMessage.CALL_MSG_ON_SUCCESS:
                Log.i("Main", "语音呼叫成功");
                Toast.makeText(this, "语音接通", Toast.LENGTH_SHORT).show();
                //升级成语音
                MobileCC.getInstance().updateToVideo();
                break;

            //排队超时
            case NotifyMessage.CALL_MSG_ON_QUEUE_TIMEOUT:
                Toast.makeText(this, R.string.call_error, Toast.LENGTH_SHORT).show();
                //排队超时进行挂断操作
                break;

            //呼叫失败
            case NotifyMessage.CALL_MSG_ON_FAIL:
                //显示返回按钮
                Toast.makeText(this, R.string.call_error, Toast.LENGTH_SHORT).show();
                //排队超时进行挂断操作
                break;

            /**
             *获取文字能力
             */
            case NotifyMessage.CALL_MSG_ON_CONNECT:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    //没有收到服务器的retcode，判断是文字还是语音，打印出错误码
                    Toast.makeText(this, R.string.call_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                //收到服务器的retcode，判断是文字还是语音，打印出错误码
                if (MobileCC.MESSAGE_OK.equals(broadMsg.getRequestCode().getRetCode())) {
                    //文字连接成功
                    if (MobileCC.MESSAGE_TYPE_TEXT.equals(broadMsg.getType())) {
                        Toast.makeText(this, "获取文字能力,文字连接成功", Toast.LENGTH_LONG).show();
                    } else {//语音连接成功
                        Toast.makeText(this, "获取语音能力,语音连接成功", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.call_error, Toast.LENGTH_SHORT).show();
                }
                break;

            //申请会议时网络异常
            case NotifyMessage.CALL_MSG_ON_APPLY_MEETING:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(this, "申请会议时网络异常", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!MobileCC.MESSAGE_OK.equals(broadMsg.getRequestCode().getRetCode())) {
                    Toast.makeText(this, "申请会议时网络异常", Toast.LENGTH_SHORT).show();
                }
                break;

            //呼叫失败
            case NotifyMessage.CALL_MSG_ON_CALL_END:
                Toast.makeText(this, "呼叫失败", Toast.LENGTH_SHORT).show();
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
            //登录
            case NotifyMessage.AUTH_MSG_ON_LOGIN:
                //当retcode为空时，通过errorcode提示用户
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_error) + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_SHORT).show();
                    if ("10-100-008".equals(broadMsg.getRequestCode().getErrorCode())) {
                        Toast.makeText(this, "用户已登录", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                //登录成功,进行文字会谈
                if ("0".equals(broadMsg.getRequestCode().getRetCode())) {
                    //设置SIP地址
                    if (0 != MobileCC.getInstance().setSIPServerAddress(Constant.S_IP, Constant.S_PORT)) {
                        Toast.makeText(LoginActivity.this, R.string.sip_error, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //登录成功
                    Toast.makeText(LoginActivity.this, R.string.login_succeed, Toast.LENGTH_SHORT).show();
                    //登录成功跳转到呼叫等待页面
                    CallingActivity.startActivity(this);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_error) + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_SHORT).show();
                }
                break;

            //注销
            case NotifyMessage.AUTH_MSG_ON_LOGOUT:
                if (null == broadMsg.getRequestCode().getRetCode()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.logout_error) + broadMsg.getRequestCode().getErrorCode(), Toast.LENGTH_SHORT).show();
                    return;
                }
                //注销成功
                if ("0".equals(broadMsg.getRequestCode().getRetCode())) {
                    Toast.makeText(LoginActivity.this, getString(R.string.logout_succeed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.logout_error) + broadMsg.getRequestCode().getRetCode(), Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }


    /**
     * 设置线的颜色
     */
    private void setTabTextColor(int position) {
        tvLoginTab.setTextColor(Color.parseColor("#666666"));
        tvSignTab.setTextColor(Color.parseColor("#666666"));

        switch (position) {
            case 0:
                tvLoginTab.setTextColor(Color.parseColor("#12C58B"));
                break;

            case 1:
                tvSignTab.setTextColor(Color.parseColor("#12C58B"));
                break;

            default:
                break;
        }
    }

    /**
     * 设置文本的颜色
     *
     * @param position
     */
    private void setLanguageTextColor(int position) {
        tvArab.setTextColor(Color.parseColor("#333333"));
        tvEnglish.setTextColor(Color.parseColor("#333333"));

        switch (position) {
            case 0:
                tvArab.setTextColor(Color.parseColor("#999999"));
                //切换语言
//                switchLanguage(Constant.ARAB);
                break;

            case 1:
                tvEnglish.setTextColor(Color.parseColor("#999999"));
                //切换语言
//                switchLanguage(Constant.ENGLISH);
                break;

            default:
                break;
        }
    }

    /**
     * 切换语言
     *
     * @param language
     */
    private void switchLanguage(String language) {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        switch (language) {
            //阿拉伯语
            case Constant.ARAB:
                configuration.locale = new Locale("ar");
                break;

            //英语
            case Constant.ENGLISH:
                configuration.locale = Locale.ENGLISH;
                break;

            default:
                break;
        }
        resources.updateConfiguration(configuration, dm);

        //保存设置的语音
        PreferencesHelper.saveData(Constant.LANGUAGE, language);
    }

    /**
     * 初始化viewpager
     */
    private void initPager() {
        fragmentList = new ArrayList<>();
        fragmentList.add(LoginFragment.newInstance());
        fragmentList.add(RegisterFragment.newInstance());
        loginAdapter = new LoginAdapter(getSupportFragmentManager(), fragmentList);
        vpContent.setAdapter(loginAdapter);
        vpContent.setCurrentItem(cuttentIndex);

        vpContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int marginLeft = DisplayUtil.dp2px(LoginActivity.this, 40F);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) moveLine.getLayoutParams();

                if (cuttentIndex == 0 && position == 0) {//左->右
                    layoutParams.leftMargin = (int) (marginLeft + lineWidth * position + positionOffset * lineWidth);
                }
                moveLine.setLayoutParams(layoutParams);
            }

            @Override
            public void onPageSelected(int position) {
                //设置tab的颜色
                setTabTextColor(position);

                tvTopTitle.setText(0 == position ? getString(R.string.login) : getString(R.string.register));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 设置滑动线的宽度
     */
    private void setLineWidth() {
        tvLoginTab.post(new Runnable() {
            @Override
            public void run() {
                lineWidth = tvLoginTab.getWidth();
                ViewGroup.LayoutParams params = moveLine.getLayoutParams();
                params.width = lineWidth;
                moveLine.setLayoutParams(params);
            }
        });
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    private class LoginAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();

        public LoginAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return null == fragmentList ? 0 : fragmentList.size();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doublePressBackToast();
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    private void doublePressBackToast() {
        if (!isBackPressed) {
            Log.i("doublePressBackToast", "再次点击返回退出程序");
            isBackPressed = true;
            Toast.makeText(this, "再次点击返回退出程序", Toast.LENGTH_SHORT).show();
        } else {
            finish();
            AppManager.getInstance().appExit();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBackPressed = false;
            }
        }, 2000);
    }
}

