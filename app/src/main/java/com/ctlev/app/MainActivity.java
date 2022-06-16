package com.ctlev.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btnCompare;
    EditText etAppId,etApkName;
    TextView tvError;
//    boolean sentForInstall=false;
    ApkCheck apkCheck=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCompare=findViewById(R.id.btnCompare);
        tvError=findViewById(R.id.tvError);
        etAppId=findViewById(R.id.etAppId);
        etApkName=findViewById(R.id.etApkName);
        apkCheck=ApkCheck.getInstance();
        apkCheck.checkUnknownSourceInstallation(this);

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!apkCheck.checkUnknownSourceInstallation(MainActivity.this)) {
                    tvError.setText("allow this to use this app functionality ");
                    return;
                }
                try {
                    String response="";
                    //option1
                    response=apkCheck.compareAndUpgrade(MainActivity.this,etApkName.getText().toString(),etAppId.getText().toString());
                    //option2
//                    response=apkCheck.compareAndUpgrade(MainActivity.this,etApkName.getText().toString(),
//                            apkCheck.getCurrentAppDetail().packageName);
                    //option3
//                    String fullPath =( getDataDir().getAbsolutePath()+"/downLoadApp/" + etApkName.getText().toString()+".apk").trim();
//                    response=apkCheck.compareAndUpgrade(MainActivity.this,etApkName.getText().toString(),
//                            apkCheck.getApkDetailNotInstalled(MainActivity.this,fullPath).packageName);
                    tvError.setText(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    tvError.setText(e.getMessage()+"\n please check the package name and apkname and it should be in downLoadApp folder of app");
                }
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        btnCompare.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!MainActivity.this.isFinishing())
                    apkCheck.deleteApk(MainActivity.this);
            }
        },10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ApkCheck.UNKNOWN_RESOURCE_INTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "onActivityResult: success UNKNOWN_RESOURCE_INTENT_REQUEST_CODE");
                tvError.setText("Good Job. You are ready to use the feature");
            }
            else {
                Log.i(TAG, "onActivityResult: fail UNKNOWN_RESOURCE_INTENT_REQUEST_CODE");
                tvError.setText("allow this to use this app functionality ");
                //sentForInstall=false;
            }
        }
        else if (requestCode == ApkCheck.INSTALL_RESOURCE_INTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "onActivityResult: success INSTALL_RESOURCE_INTENT_REQUEST_CODE");
                //  delete the file if both apk and install app version is same then del the downloaded file
                tvError.setText("Installation is successful");
            }
            else {
                Log.i(TAG, "onActivityResult: fail INSTALL_RESOURCE_INTENT_REQUEST_CODE");
                tvError.setText("User Cancel the installation ");
            }
        } else {
            //give the error
            Log.i(TAG, "onActivityResult: error");
        }
    }
}