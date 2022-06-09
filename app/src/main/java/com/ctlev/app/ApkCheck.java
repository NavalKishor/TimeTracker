package com.ctlev.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

public class ApkCheck {
    private static final String TAG = "ApkCheck";
    public static AppDetails getApkDetailNotInstalled(Context context, String fullPath) throws Exception {
        if(fullPath==null || fullPath.isEmpty()) throw new Exception("Invalid Path,it should be not null and non empty string");
        if (context==null) throw new Exception("Invalid context,it should not be null");
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
        if (info==null)throw new Exception("Invalid Path not accessible");
        return new AppDetails( info.packageName,  info.versionName, info.versionCode );
    }
    public static AppDetails getInstalledAppDetail(Context context,String packageName) throws Exception {
        if(packageName==null || packageName.isEmpty()) throw new Exception("Invalid packageName,it should be not null and non empty string");
        if (context==null) throw new Exception("Invalid context,it should not be null");
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        if (info==null)throw new Exception("No app found with the packageName provided :"+packageName);
        return new AppDetails( info.packageName,  info.versionName, info.versionCode );
    }
    public static AppDetails getCurrentAppDetail(){
        return new AppDetails(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE);
    }



    public static void compareAndUpgrade(Context context,String apkName,String packageName) throws Exception {
        AppDetails currentAppDetailInstance=ApkCheck.getCurrentAppDetail();
        String currentAppDetail=currentAppDetailInstance.toString();
        Log.i(TAG, "onCreate: getCurrentAppDetail:"+" ,"+currentAppDetail);
        //String apkName = "app-debug.apk";
        if (apkName==null || apkName.isEmpty()||packageName==null||packageName.isEmpty()) {
            if(BuildConfig.DEBUG)
                apkName = "app-debug.apk";
            else
                throw new Exception("apkName:["+apkName+"] and packageName:["+packageName+"] should not be null or empty");

        }
        String extension="";
        if (!apkName.endsWith(".apk")){
            extension=".apk";
        }
        String fullPath =(context.getFilesDir().getAbsolutePath()+"/downLoadApp/" + apkName+extension).trim();

        // try {
        AppDetails getApkDetailNotInstalled = ApkCheck.getApkDetailNotInstalled(context,fullPath);
        String apkDetail=getApkDetailNotInstalled.toString();
        Log.i(TAG, "onCreate: apk at:"+fullPath+" ,"+apkDetail);

        //fullPath="com.ctlev.app";
        //fullPath=getApkDetailNotInstalled.packageName;
        AppDetails getInstalledAppDetail = ApkCheck.getInstalledAppDetail(context,packageName);
        apkDetail=getInstalledAppDetail.toString();
        Log.i(TAG, "onCreate: package at:"+packageName+" ,"+apkDetail);

        String details="";
        if (getApkDetailNotInstalled.isSameAppID(getInstalledAppDetail)) {
            if (getApkDetailNotInstalled.isSameApp(getInstalledAppDetail)) {
                details += " downloaded apk and installed app are same version. No Action needed";
            }
            else
            {
                details += " downloaded apk and installed app are different version. Action ";
                if( getApkDetailNotInstalled.isUpGradable(getInstalledAppDetail)){
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
            details += " different app then the installed one."+" getApkDetailNotInstalled:";
        }
        details+= getApkDetailNotInstalled.toString()+" ,getInstalledAppDetail"+getInstalledAppDetail.toString();
        Log.i(TAG, "onCreate: Details:"+details );
        Toast.makeText(context.getApplicationContext(), details , Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            AppDetails apkDetailObj = ApkCheck.getInstalledAppDetail(context,"com.ctlev.app");
            apkDetail=apkDetailObj.toString();
            Log.i(TAG, "onCreate: Details:"+apkDetail );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
class AppDetails{
    String  packageName, versionName;
    int versionCode ;

    @Override
    public String toString() {
        return "AppDetails{" +
                "packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                '}';
    }

    public AppDetails (String  packageName, String versionName, int versionCode ){
        this.packageName=packageName;
        this.versionName=versionName;
        this.versionCode=versionCode ;
    }

    public int versionForDev(){
        return versionCode;
    }
    public String versionForUser(){
        return versionName;
    }
    public String getPackageName(){
        return packageName;
    }
    public boolean isSameAppID(AppDetails appDetails){
        return packageName.equals(appDetails.packageName);
    }
    public boolean isSameApp(AppDetails appDetails){
        return isSameAppID(appDetails)&& appDetails.versionCode==versionCode;
    }
    public boolean isUpGradable(AppDetails appDetails){
        return isSameAppID(appDetails) && appDetails.versionCode>versionCode;
    }
    public boolean isDownGradable(AppDetails appDetails){
        return isSameAppID(appDetails) && appDetails.versionCode<versionCode;
    }

}