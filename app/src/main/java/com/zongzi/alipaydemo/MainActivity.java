package com.zongzi.alipaydemo;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.zongzi.alipaydemo.auto.AutoAccessibilityService;
import com.zongzi.alipaydemo.auto.BaseAccessibilityService;
import com.zongzi.alipaydemo.utils.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button startSettingButton;
    Button endSettingButton;
    Button getEnergy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startSettingButton = findViewById(R.id.btn_setting_start);
        endSettingButton = findViewById(R.id.btn_setting_end);
        getEnergy = findViewById(R.id.btn_get_energy);

        startSettingButton.setOnClickListener(this);
        endSettingButton.setOnClickListener(this);
        getEnergy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_setting_start) {
            if (!AutoAccessibilityService.isStart()) {
                try {
                    this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } catch (Exception e) {
                    this.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    e.printStackTrace();
                }
            } else {
                Utils.toast("已经打开辅助设置");
            }
        } else if (v.getId() == R.id.btn_setting_end) {
            if (!AutoAccessibilityService.stopService()) {
                try {
                    this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } catch (Exception e) {
                    this.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    e.printStackTrace();
                }
            } else {
                Utils.toast("已经关闭辅助设置");
            }
        } else if (v.getId() == R.id.btn_get_energy) {
            AliPayAccessibilityService.getInstance().setStop(false);
            BaseAccessibilityService.startLaunch(MainActivity.this, AliPayAccessibilityService.getInstance(), "com.eg.android.AlipayGphone");
        }
    }
}
