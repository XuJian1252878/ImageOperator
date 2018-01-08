package com.example.imageoperator.photopicker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.example.imageoperator.R;

import io.fabric.sdk.android.Fabric;

public class PhotoPickerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 引入 Crashlytics 检测工具
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.photo_picker_activity);


        findViewById(R.id.button_crashlytics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forceCrash(v);
            }
        });
    }

    public void forceCrash(View view) {

        // TODO: Move this to where you establish a user session

        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier("12345");
        Crashlytics.setUserEmail("user@fabric.io");
        Crashlytics.setUserName("Test User");

        throw new RuntimeException("This is a crash");
    }
}
