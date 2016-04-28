package com.example.ddvoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Created by ASUS on 2016/4/16.
 */
public class LoadingActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                //等待10000毫秒后销毁此页面，并提示登陆成功
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                LoadingActivity.this.finish();
                Log.v("123", "123");
            }
        }, 1000);
    }

}
