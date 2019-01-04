package com.lilei.fitness.QQ;

import android.content.Context;
import android.widget.Toast;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

//实现回调
public abstract class BaseUiListener implements IUiListener {

    private static final String TAG = "BaseUiListener";

    protected Context context;

    BaseUiListener(Context context) {
        this.context = context;
    }

    @Override
    public void onComplete(Object response) {
        if (null == response) {
            return;
        }
        JSONObject jsonResponse = (JSONObject) response;
        if (jsonResponse.length() == 0) {

            return;
        }

        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
        doComplete((JSONObject) response);
    }

    //数据处理在子类中实现
    abstract void doComplete(JSONObject values);

    @Override
    public void onError(UiError e) {
        Toast.makeText(context, "登录出错", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel() {
        Toast.makeText(context, "取消登录", Toast.LENGTH_SHORT).show();
    }
}
