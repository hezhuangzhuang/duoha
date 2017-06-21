package com.zxwl.duoha.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.zxwl.duoha.R;
import com.zxwl.duoha.utils.statusbar.SystemUiVisibilityUtil;


public class WelcomActivity extends AppCompatActivity {

    static final long DELAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        SystemUiVisibilityUtil.hideStatusBar(getWindow(),true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean hsaLogin = false;
                if (hsaLogin) {
                    MainActivity.startActivity(WelcomActivity.this);
                } else {
                    //TODO 修改登录界面
                    MainActivity.startActivity(WelcomActivity.this);
//                    startActivity(LoginActivity.getLaunchIntent(WelcomActivity.this));
                }
                finish();
            }
        }, DELAY_TIME);
    }
}
