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
import android.view.LayoutInflater;
import com.ctlev.app.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;  //defining the binding class

    Button btnCompare;
    EditText etAppId,etApkName;
    TextView tvError;
//    boolean sentForInstall=false;
    ApkCheck apkCheck=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); //initializing the binding class
        setContentView(binding.getRoot()); // we now set the contentview as the binding.root
        //setContentView(R.layout.activity_main);
        //btnCompare=findViewById(R.id.btnCompare);
//        tvError=findViewById(R.id.tvError);
//        etAppId=findViewById(R.id.etAppId);
//        etApkName=findViewById(R.id.etApkName);
        apkCheck=ApkCheck.getInstance();
        apkCheck.checkUnknownSourceInstallation(this);
        binding.tvError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                   AppDetails details= apkCheck.getInstalledAppDetail(MainActivity.this,"com.example.logsupload");
                    Log.i(TAG, "tvError onClick: details"+details);
                    details= apkCheck.getInstalledAppDetail(MainActivity.this,"com.example.httpstest");
                    Log.i(TAG, "tvError onClick: details"+details);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "tvError onClick: fail due to exception:"+e.getMessage());
                }
            }
        });
        binding.btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!apkCheck.checkUnknownSourceInstallation(MainActivity.this)) {
                    binding.tvError.setText("allow this to use this app functionality ");
                    return;
                }
                try {
                    String response="";
                    //option1
                    response=apkCheck.compareAndUpgrade(MainActivity.this,binding.etApkName.getText().toString(),binding.etAppId.getText().toString());
                    //option2
//                    response=apkCheck.compareAndUpgrade(MainActivity.this,binding.etApkName.getText().toString(),
//                            apkCheck.getCurrentAppDetail().packageName);
                    //option3
//                    String fullPath =( getDataDir().getAbsolutePath()+"/downLoadApp/" + binding.etApkName.getText().toString()+".apk").trim();
//                    response=apkCheck.compareAndUpgrade(MainActivity.this,binding.etApkName.getText().toString(),
//                            apkCheck.getApkDetailNotInstalled(MainActivity.this,fullPath).packageName);
                    binding.tvError.setText(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    binding.tvError.setText(e.getMessage()+"\n please check the package name and apkname and it should be in downLoadApp folder of app");
                }
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        binding.btnCompare.postDelayed(new Runnable() {
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
                binding.tvError.setText("Good Job. You are ready to use the feature");
            }
            else {
                Log.i(TAG, "onActivityResult: fail UNKNOWN_RESOURCE_INTENT_REQUEST_CODE");
                binding.tvError.setText("allow this to use this app functionality ");
                //sentForInstall=false;
            }
        }
        else if (requestCode == ApkCheck.INSTALL_RESOURCE_INTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "onActivityResult: success INSTALL_RESOURCE_INTENT_REQUEST_CODE");
                //  delete the file if both apk and install app version is same then del the downloaded file
                binding.tvError.setText("Installation is successful");
            }
            else {
                Log.i(TAG, "onActivityResult: fail INSTALL_RESOURCE_INTENT_REQUEST_CODE");
                binding.tvError.setText("User Cancel the installation ");
            }
        } else {
            //give the error
            Log.i(TAG, "onActivityResult: error");
        }
    }
}