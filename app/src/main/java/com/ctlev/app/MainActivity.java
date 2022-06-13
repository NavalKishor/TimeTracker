package com.ctlev.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btnCompare;
    EditText etAppId,etApkName;
    TextView tvError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCompare=findViewById(R.id.btnCompare);
        tvError=findViewById(R.id.tvError);
        etAppId=findViewById(R.id.etAppId);
        etApkName=findViewById(R.id.etApkName);
        Intent unKnownSourceIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                startActivityForResult(unKnownSourceIntent,  ApkCheck.UNKNOWN_RESOURCE_INTENT_REQUEST_CODE);
            }
        }
        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ApkCheck.compareAndUpgrade(MainActivity.this,etApkName.getText().toString(),etAppId.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    tvError.setText(e.getMessage()+"\n please check the package name and apkname and it should be in downLoadApp folder of app");
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ApkCheck.UNKNOWN_RESOURCE_INTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult: success UNKNOWN_RESOURCE_INTENT_REQUEST_CODE");
        }if (requestCode == ApkCheck.INSTALL_RESOURCE_INTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult: success INSTALL_RESOURCE_INTENT_REQUEST_CODE");
//            delete the file if both apk and install app version is same then del the downloaded file
            
        } else {
            //give the error
            Log.i(TAG, "onActivityResult: error");
        }
    }
    public void compareApp(){
        String apkName = "app-debug.apk";
        String fullPath = getFilesDir().getAbsolutePath()+"/downLoadApp" + "/" + apkName;
        int versionCode=BuildConfig.VERSION_CODE;
        final PackageManager pm = getPackageManager();

        PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
        String details="from apk PackageName: "+info.packageName+", VersionCode : " + info.versionCode + ", VersionName : " + info.versionName;
        details+="\nfrom installed PackageName: "+BuildConfig.APPLICATION_ID+", VersionCode : " +  versionCode + ", VersionName : " + BuildConfig.VERSION_NAME;

        Log.i(TAG, "onCreate:details:"+details);
        if (BuildConfig.APPLICATION_ID.equals(info.packageName)) {
            if (versionCode == info.versionCode) {
                details += " downloaded apk and installed app are same version. No Action needed";
            }
            else
            {
                details += " downloaded apk and installed app are different version. Action ";
                if (versionCode<info.versionCode){
                    details+=" it will upgrade the app";
                    //install the app and del it.
                }
                else
                {
                    details+=" it will downgrade the app, don't do that";
                }
            }
        }
        else {
            details += " different app then the installed one.";
        }
        Toast.makeText(this, details , Toast.LENGTH_LONG).show();
    }
}