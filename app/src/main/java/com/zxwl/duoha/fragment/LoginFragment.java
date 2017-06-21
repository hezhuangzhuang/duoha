package com.zxwl.duoha.fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.esdk.cc.MobileCC;
import com.huawei.esdk.cc.service.CCAPP;
import com.huawei.esdk.cc.utils.LogUtil;
import com.weiwangcn.betterspinner.library.BetterSpinner;
import com.zxwl.duoha.R;
import com.zxwl.duoha.bean.UserInfo;
import com.zxwl.duoha.net.Constant;
import com.zxwl.duoha.rx.RxBus;
import com.zxwl.duoha.utils.sharedpreferences.PreferencesHelper;

import java.io.IOException;
import java.io.InputStream;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * author：hw
 * data:2017/6/9 14:51
 * 登录的fragment
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener {
    private BetterSpinner bsAreaCode;//区号
    private EditText etPhone;//手机号
    private EditText etNickname;//昵称
    private TextView tvLogin;//登录
    private TextView tvRegister;//注册

    private ImageView ivPhoneDelete;
    private ImageView ivNameDelete;
    private Subscription subscription;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    protected void findViews(View view) {
        bsAreaCode = (BetterSpinner) view.findViewById(R.id.bs_area_code);
        etPhone = (EditText) view.findViewById(R.id.et_phone);
        etNickname = (EditText) view.findViewById(R.id.et_nickname);
        tvLogin = (TextView) view.findViewById(R.id.tv_login);
        tvRegister = (TextView) view.findViewById(R.id.tv_register);

        ivPhoneDelete = (ImageView) view.findViewById(R.id.iv_phone_delete);
        ivNameDelete = (ImageView) view.findViewById(R.id.iv_name_delete);
    }

    @Override
    protected void addListener() {
        bsAreaCode.setOnClickListener(this);//验证码
        tvLogin.setOnClickListener(this);//登录
        tvRegister.setOnClickListener(this);//注册

        ivPhoneDelete.setOnClickListener(this);
        ivNameDelete.setOnClickListener(this);

        setEditFocusListener();
    }

    @Override
    protected void initData() {
        final String[] list = getResources().getStringArray(R.array.month);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, list);
        bsAreaCode.setAdapter(adapter);
        bsAreaCode.setDropDownWidth(200);
        bsAreaCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), list[position], Toast.LENGTH_SHORT).show();
            }
        });

        //判断是否有昵称
        setPhoneAndNickName();
        //初始rxbus
        initRxBus();

        etNickname.setText(Constant.USER_NAME);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //登录
            case R.id.tv_login:
                String phone = etPhone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(getActivity(), R.string.phone_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                String nickName = etNickname.getText().toString().trim();
                if (TextUtils.isEmpty(nickName)) {
                    Toast.makeText(getActivity(), R.string.nick_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                //登录的网络请求
                login(phone, nickName);

                //登录
                loginICP(nickName);
                break;

            case R.id.iv_name_delete:
                etNickname.setText("");
                break;

            case R.id.iv_phone_delete:
                etPhone.setText("");
                break;

            default:
                break;
        }
    }

    /**
     * 设置端口和ip,然后登陆
     */
    private void loginICP(String nickName) {
        //设置传输加密
        MobileCC.getInstance().setTransportSecurity(false, false);
        //设置证书
        saveCA();
        //设置地址和端口
        if (0 != setHostAddress()) {
            Toast.makeText(getActivity(), R.string.network_configuration_wrong, Toast.LENGTH_SHORT).show();
            return;
        }

        //登录
        if (0 != MobileCC.getInstance().login("1", nickName)) {
            Toast.makeText(getActivity(), R.string.user_name_error, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 设置地址
     *
     * @return
     */
    private int setHostAddress() {
        return MobileCC.getInstance().setHostAddress(Constant.IP, Constant.PROT, false, MobileCC.SERVER_MS);
    }

    private void saveCA() {
        InputStream inputStream = null;
        try {
            inputStream = CCAPP.getInstances().getApplication().getAssets().open("certs/" + "server.cer");
            MobileCC.getInstance().setServerCertificateValidation(true, false, inputStream);

        } catch (IOException ioe) {
            LogUtil.d(TAG, "io Exception ");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtil.d(TAG, "io Exception ");
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ivNameDelete.setVisibility(View.GONE);
        ivPhoneDelete.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != subscription && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    /**
     * 登录
     *
     * @param phone    手机号
     * @param nickName 昵称
     */
    private void login(String phone, String nickName) {
        PreferencesHelper.saveData(Constant.PHONE, phone);
        PreferencesHelper.saveData(Constant.NICK_NAME, nickName);
//        ChatActivity.startActivity(getActivity());
    }

    private void setEditFocusListener() {
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i,
                                          int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1,
                                      int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = etPhone.getText().toString().trim();
                if (phone.length() > 0) {
                    ivPhoneDelete.setVisibility(View.VISIBLE);
                } else {
                    ivPhoneDelete.setVisibility(View.GONE);
                }
            }
        });

        etPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    String phone = etPhone.getText().toString().trim();
                    if (phone.length() > 0) {
                        ivPhoneDelete.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 此处为失去焦点时的处理内容
                    ivPhoneDelete.setVisibility(View.GONE);
                }
            }
        });

        etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i,
                                          int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1,
                                      int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pass = etNickname.getText().toString().trim();
                if (pass.length() > 0) {
                    ivNameDelete.setVisibility(View.VISIBLE);
                } else {
                    ivNameDelete.setVisibility(View.GONE);
                }
            }
        });

        etNickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    String pass = etNickname.getText().toString().trim();
                    if (pass.length() > 0) {
                        ivNameDelete.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 此处为失去焦点时的处理内容
                    ivNameDelete.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initRxBus() {
        subscription = RxBus.getInstance()
                .toObserverable(UserInfo.class)
                .compose(this.<UserInfo>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserInfo>() {
                    @Override
                    public void call(UserInfo userInfo) {
                        etPhone.setText(userInfo.phone);
                        etNickname.setText(userInfo.nickName);
                    }
                });
    }

    private void setPhoneAndNickName() {
        String phone = PreferencesHelper.getData(Constant.PHONE);
        String nickName = PreferencesHelper.getData(Constant.NICK_NAME);

        if (!TextUtils.isEmpty(phone)) {
            etPhone.setText(phone);
        }

        if (!TextUtils.isEmpty(nickName)) {
            etNickname.setText(nickName);
        }
    }

}
