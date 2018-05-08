package com.littonishir.appupdate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private UpdataAppManger updatamanger;
    private EditText et;
    private String url = "http://imtt.dd.qq.com/16891/4B61342F28FFAA79A313FCBC03AD238E.apk?fsname=com.huaban.android_4.0.8_83.apk&csr=1bbd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        updatamanger = new UpdataAppManger(this);
        et = (EditText) findViewById(R.id.et);
        et.setText(url);
        updateApp();
    }

    public void download(View view) {
        updateApp();
    }

    private void updateApp() {
        String mUrl = et.getText().toString().trim();
        if (!mUrl.isEmpty()) {
            updatamanger.downloadAPK(mUrl, "花瓣");
        }
    }
}
