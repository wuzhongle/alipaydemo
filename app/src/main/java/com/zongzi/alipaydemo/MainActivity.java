package com.zongzi.alipaydemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zongzi.alipaydemo.auto.BaseAccessibilityService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AliPayAccessibilityService.getInstance().setStop(false);
        BaseAccessibilityService.startLaunch(MainActivity.this, AliPayAccessibilityService.getInstance(), "com.eg.android.AlipayGphone");
    }
}
