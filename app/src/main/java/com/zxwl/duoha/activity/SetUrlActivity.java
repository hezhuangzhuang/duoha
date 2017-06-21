package com.zxwl.duoha.activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zxwl.duoha.R;
import com.zxwl.duoha.net.Constant;


/**
 * 设置ip和sip
 */
public class SetUrlActivity extends BaseActivity {
    private EditText etIpAddress;
    private EditText etIpPort;
    private EditText etAccessCode;
    private EditText etSipAddress;
    private EditText etSipPort;
    private EditText etAudioAccessCode;
    private Button tvOk;

    @Override
    protected void initView() {
        etIpAddress = (EditText) findViewById(R.id.et_ip_address);
        etIpPort = (EditText) findViewById(R.id.et_ip_port);
        etAccessCode = (EditText) findViewById(R.id.et_access_code);
        etSipAddress = (EditText) findViewById(R.id.et_sip_address);
        etSipPort = (EditText) findViewById(R.id.et_sip_port);
        etAudioAccessCode = (EditText) findViewById(R.id.et_audio_access_code);
        tvOk = (Button) findViewById(R.id.tv_ok);
    }

    @Override
    protected void initData() {
        etIpAddress.setText(Constant.IP);
        etIpPort.setText(Constant.PROT);
        etAccessCode.setText(Constant.ACCESS_CODE);
        etSipAddress.setText(Constant.S_IP);
        etSipPort.setText(Constant.S_PORT);
        etAudioAccessCode.setText(Constant.AUDIO_ACCESS_CODE);
    }

    @Override
    protected void setListener() {
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = etIpAddress.getText().toString().trim();
                String ipPort = etIpPort.getText().toString().trim();
                String accessCode = etAccessCode.getText().toString().trim();
                String sipAddress = etSipAddress.getText().toString().trim();
                String sipPort = etSipPort.getText().toString().trim();
                String audioAccessCode = etAudioAccessCode.getText().toString().trim();

                Constant.IP = ipAddress;
                Constant.PROT = ipPort;
                Constant.ACCESS_CODE = accessCode;
                Constant.S_IP = sipAddress;
                Constant.S_PORT = sipPort;
                Constant.AUDIO_ACCESS_CODE = audioAccessCode;
                LoginActivity.startActivity(SetUrlActivity.this);
                finish();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set_url;
    }

}
