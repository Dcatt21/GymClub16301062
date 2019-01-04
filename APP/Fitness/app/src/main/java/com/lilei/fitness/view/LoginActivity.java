package com.lilei.fitness.view;

import java.util.Map;

import okhttp3.Call;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lilei.fitness.QQ.LoginListener;
import com.lilei.fitness.R;
import com.lilei.fitness.entity.User;
import com.lilei.fitness.utils.Constants;
import com.lilei.fitness.utils.MyDialogHandler;
import com.lilei.fitness.utils.SharedPreferencesUtils;
import com.lilei.fitness.view.base.BaseActivity;
import com.tencent.tauth.Tencent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

public class LoginActivity extends BaseActivity implements OnClickListener {
    private EditText et_username;
    private EditText et_password;

    private Button bt_login,login_bt_qq;
    private Button bt_register;
    private Context mContext;

    private MyDialogHandler uiFlusHandler;
    private LoginListener loginListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        findViewById();
        initView();
    }

    private void login() {
        String username = et_username.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        //d.判断用户名密码是否为空，不为空请求服务器（省略，默认请求成功）
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(mContext, "不可留空", Toast.LENGTH_SHORT).show();
            return;
        }
        // 服务端验证
        checkUser();
        // openActivity(MainActivity.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_bt_login:
                login();
                break;
                case R.id.login_bt_qq:
                    loginQQ();
                    loginListener.setmLoginInterface(new LoginListener.LoginInterface() {
                        @Override
                        public void afterGetUserInfo() {
                            String nickName = loginListener.getNickName();
                            String profileURL = loginListener.getProfileURL();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("login", true);
                            intent.putExtra("nickname", nickName);
                            intent.putExtra("figureurl", profileURL);
                            startActivity(intent);
                        }
                    });
                break;
            case R.id.login_bt_register:
                openActivity(RegisterActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openActivity(ConfigActivity.class);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void findViewById() {
        et_username = $(R.id.login_et_username);
        et_password = $(R.id.login_et_password);

        bt_login = $(R.id.login_bt_login);
        bt_register = $(R.id.login_bt_register);
        login_bt_qq=  $(R.id.login_bt_qq);

    }

    @Override
    protected void initView() {
        bt_login.setOnClickListener(this);
        bt_register.setOnClickListener(this);
        login_bt_qq.setOnClickListener(this);
        echo();
        uiFlusHandler = new MyDialogHandler(mContext, "登录中...");
    }

/**
 * 回显
 */
private void echo() {
    Map<String, String> map = SharedPreferencesUtils.getUserInfo(mContext);//获取用户名密码
    if (map != null) {
        String username = map.get("username");
        String password = map.get("password");
        et_username.setText(username);
        et_password.setText(password);
    }
}

    private void checkUser() {
        uiFlusHandler.sendEmptyMessage(SHOW_LOADING_DIALOG);

        String url = Constants.BASE_URL + "User?method=login";
        OkHttpUtils
                .post()
                .url(url)
                .id(1)
                .addParams("username", et_username.getText().toString().trim())
                .addParams("password", et_password.getText().toString().trim())
                .build()
                .execute(new MyStringCallback());
    }

    public class MyStringCallback extends StringCallback {
        @Override
        public void onResponse(String response, int id) {
            Gson gson = new Gson();
            switch (id) {
                case 1:
                    uiFlusHandler.sendEmptyMessage(DISMISS_LOADING_DIALOG);
                    User user = gson.fromJson(response, User.class);
                    if (user.getUserId() == 0) {
                        DisplayToast("用户名或者密码错误");
                        return;
                    } else {
                        // 存储用户
                        Constants.USER = user;
                        boolean result = SharedPreferencesUtils.saveUserInfo(mContext, user);
                        if (result) {
                            Toast.makeText(mContext, "登录成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "用户存储失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    openActivity(MainActivity.class);
                    finish();
                    break;

                default:
                    DisplayToast("what?");
                    break;
            }
        }

        @Override
        public void onError(Call arg0, Exception arg1, int arg2) {
            DisplayToast("网络链接出错！");
        }
    }
    //点击按钮之后QQ登录
    public void loginQQ() {
        //初始化，用APP ID 获取到一个Tencent实例
        Tencent mTencent = Tencent.createInstance(Constants.TENCENT_APP_ID, this);
        loginListener = new LoginListener(this, mTencent);
        if (mTencent==null){
            Toast.makeText(mContext, "Appid错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mTencent.isSessionValid()) {
            /*  实现QQ的登录，这个方法有三个参数，第一个参数是context上下文，
                 第二个参数SCOPE 是一个String类型的字符串，表示应用需要获得哪些API的权限，由“，”分隔。
                 例如：SCOPE = “get_user_info,add_t”；所有权限用“all”
                  第三个参数，是一个事件监听器，IUiListener接口的实例，*/
            mTencent.login(this, "all", loginListener);
        }
    }

    //QQ登录后的回调，必须要有
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN ||
                requestCode == com.tencent.connect.common.Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
