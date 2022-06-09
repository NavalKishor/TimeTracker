package com.ctlev.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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