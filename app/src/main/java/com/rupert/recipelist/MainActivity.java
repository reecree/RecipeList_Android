package com.rupert.recipelist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.pinterest.android.pdk.PDKCallback;
import com.pinterest.android.pdk.PDKClient;
import com.pinterest.android.pdk.PDKException;
import com.pinterest.android.pdk.PDKResponse;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
        private PDKClient pdkClient;
        private Button loginButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        PDKClient.configureInstance(this, Globals.APP_ID);
        PDKClient.getInstance().onConnect(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(this);
        pdkClient = PDKClient.configureInstance(this, Globals.APP_ID);
        pdkClient.onConnect(this);
        //pdkClient.setDebugMode(true);
    }

    private void onLogin() {
        final List scopes = new ArrayList<String>();
        scopes.add(PDKClient.PDKCLIENT_PERMISSION_READ_PUBLIC);

        pdkClient.login(this, scopes, new PDKCallback() {
            @Override
            public void onSuccess(PDKResponse response) {
                Log.d(getClass().getName(), response.getData().toString());
                onLoginSuccess();
            }

            @Override
            public void onFailure(PDKException exception) {
                Log.e(getClass().getName(), exception.getDetailMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        switch (vid) {
            case R.id.login:
                onLogin();
                break;
        }
    }

    private void onLoginSuccess() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}
