package com.sdk.wj.paysdk;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    public void init(View v) {
        BMapManager.getInstance().SDKInitializer(this, "2000", 1, "确定要购买吗?", "测试专用", "10001", "", new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        }, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        });
    }

    public void pay(View v) {
        BMapManager.getInstance().BaiduMap(this, "2000", 1, "确定要购买吗?", "测试专用", "10001", "", new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        });
    }

    public void paypoint(View v) {
        Toast.makeText(this, BMapManager.getInstance().g().toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        BMapManager.getInstance().s(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        BMapManager.getInstance().close(this);
        super.onDestroy();
    }
}
