package com.rupert.recipelist;

import android.content.Context;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PDKClient _pdkClient;
    private Button _loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PDKClient.configureInstance(this, Globals.APP_ID);
        PDKClient.getInstance().onConnect(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _loginButton = (Button) findViewById(R.id.login);
        _loginButton.setOnClickListener(this);
        _pdkClient = PDKClient.configureInstance(this, Globals.APP_ID);
        _pdkClient.onConnect(this);
        //pdkClient.setDebugMode(true);

        String accessToken = checkStoredAccessToken();
        if(!accessToken.isEmpty()) {
            _pdkClient.setAccessToken(accessToken);
            onLoginSuccess();
        }
    }

    private String checkStoredAccessToken() {
        FileInputStream fis = null;
        try {
            String accessToken = "";
            int content;

            fis = openFileInput(Globals.ACCESS_TOKEN_FILE_NAME);
            while ((content = fis.read()) != -1) {
                accessToken += (char)content;
            }
            return accessToken;
        } catch (IOException e) {
            return "";
        }
        finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onLogin() {
        final List scopes = new ArrayList<String>();
        scopes.add(PDKClient.PDKCLIENT_PERMISSION_READ_PUBLIC);

        _pdkClient.login(this, scopes, new PDKCallback() {
            @Override
            public void onSuccess(PDKResponse response) {
                Log.d(getClass().getName(), response.getData().toString());
                saveAccessToken();
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

    private void saveAccessToken() {
        FileOutputStream fos = null;
        String accessToken = _pdkClient.getAccessToken();
        try {
            fos = openFileOutput(Globals.ACCESS_TOKEN_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(accessToken.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onLoginSuccess() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}
