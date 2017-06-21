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

import com.weiwangcn.betterspinner.library.BetterSpinner;
import com.zxwl.duoha.R;
import com.zxwl.duoha.bean.UserInfo;
import com.zxwl.duoha.rx.RxBus;

/**
 * author：hw
 * data:2017/6/9 14:51
 * 注册的fragment
 */
public class RegisterFragment extends BaseFragment implements View.OnClickListener {
    private BetterSpinner bsAreaCode;//区号
    private EditText etPhone;//手机号
    private EditText etNickname;//昵称
    private TextView tvRegister;//注册

    private ImageView ivPhoneDelete;
    private ImageView ivNameDelete;

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
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
        tvRegister = (TextView) view.findViewById(R.id.tv_login);

        ivPhoneDelete = (ImageView) view.findViewById(R.id.iv_phone_delete);
        ivNameDelete = (ImageView) view.findViewById(R.id.iv_name_delete);
    }

    @Override
    protected void addListener() {
        bsAreaCode.setOnClickListener(this);//验证码
        tvRegister.setOnClickListener(this);//登录
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

        //设置
        tvRegister.setText(getString(R.string.register));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //注册
            case R.id.tv_login:
                String phone = etPhone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(getActivity(), "手机号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                String nickName = etNickname.getText().toString().trim();
                if (TextUtils.isEmpty(nickName)) {
                    Toast.makeText(getActivity(), "昵称不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                //注册的网络请求
                register(phone, nickName);
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

    @Override
    public void onResume() {
        super.onResume();

        ivNameDelete.setVisibility(View.GONE);
        ivPhoneDelete.setVisibility(View.GONE);
    }

    /**
     * 注册
     *
     * @param phone    手机号
     * @param nickName 昵称
     */
    private void register(String phone, String nickName) {
        etNickname.setText("");
        etPhone.setText("");
        Toast.makeText(getContext(), "注册成功", Toast.LENGTH_SHORT).show();
        RxBus.getInstance().post(new UserInfo(phone,nickName));
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

}
