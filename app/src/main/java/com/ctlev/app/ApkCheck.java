package com.ctlev.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

public class ApkCheck {
    private ApkCheck(){ }
    private static class ApkCheckHelper{
        public static final ApkCheck Instance=new ApkCheck();
    }
    public static ApkCheck getInstance(){
        return ApkCheckHelper.Instance;
    }
    private  final String TAG = "ApkCheck";
    private  final String DownLoadDir = "/downLoadApp/";

    public  AppDetails getApkDetailNotInstalled(Context context, String fullPath) throws Exception {
        if(fullPath==null || fullPath.isEmpty()) throw new Exception("Invalid Path,it should be not null and non empty string");
        if (context==null) throw new Exception("Invalid context,it should not be null");
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
        if (info==null)throw new Exception("Invalid Path not accessible");
        return new AppDetails( info.packageName,  info.versionName, info.versionCode );
    }
    public  AppDetails getInstalledAppDetail(Context context,String packageName) throws Exception {
        if(packageName==null || packageName.isEmpty()) throw new Exception("Invalid packageName,it should be not null and non empty string");
        if (context==null) throw new Exception("Invalid context,it should not be null");
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        if (info==null)throw new Exception("No app found with the packageName provided :"+packageName);
        return new AppDetails( info.packageName,  info.versionName, info.versionCode );
    }
    public  AppDetails getCurrentAppDetail(){
        return new AppDetails(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE);
    }
    public boolean checkUnknownSourceInstallation(Activity context){
        boolean chk=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent unKnownSourceIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", context.getPackageName())));
            chk=context.getPackageManager().canRequestPackageInstalls();
            if (!chk) {
                context.startActivityForResult(unKnownSourceIntent,  ApkCheck.UNKNOWN_RESOURCE_INTENT_REQUEST_CODE);
                //sentForInstall=true;
            }
        }
        else{
            try {
                chk = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if(!chk){
                context.startActivityForResult(new Intent(Settings.ACTION_SECURITY_SETTINGS),  ApkCheck.UNKNOWN_RESOURCE_INTENT_REQUEST_CODE);
                //sentForInstall=true;
            }
        }
        return chk;
    }
    public  void deleteApk(Context context){
        String fullPath =context.getDataDir().getAbsolutePath()+DownLoadDir;
        File downloadDir=new File(fullPath);
        if (!downloadDir.exists()) return;
        File[] fileList=downloadDir.listFiles();
        if (fileList==null || fileList.length==0) return;
        for (File file : fileList) {
            if (file.getName().endsWith(".apk")){
                try {
                    AppDetails getApkDetailNotInstalled = getApkDetailNotInstalled(context,file.getAbsolutePath());
                    AppDetails getInstalledAppDetail =getInstalledAppDetail(context,getApkDetailNotInstalled.packageName);
                    if (getApkDetailNotInstalled.isSameApp(getInstalledAppDetail)) {
                        file.delete();
                        Log.i(TAG, "deleteApk: isSameApp done for "+file.getAbsolutePath());
                    }
                    if (getApkDetailNotInstalled.isDownGradable(getInstalledAppDetail)) {
                        file.delete();
                        Log.i(TAG, "deleteApk: isDownGradable done for "+file.getAbsolutePath());
                    }
                    if (getApkDetailNotInstalled.isUpGradable(getInstalledAppDetail)) {
                        try {
                            if(checkUnknownSourceInstallation((Activity) context)) {
                                install((Activity) context, file);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public  String compareAndUpgrade(@NonNull Context context,@NonNull String apkName,@NonNull String packageName) throws Exception {
        AppDetails currentAppDetailInstance=getCurrentAppDetail();
        String currentAppDetail=currentAppDetailInstance.toString();
        Log.i(TAG, "compareAndUpgrade: getCurrentAppDetail:"+" ,"+currentAppDetail);
        //String apkName = "app-debug.apk";
        if (apkName==null || apkName.isEmpty()||packageName==null||packageName.isEmpty()) {
            if(BuildConfig.DEBUG) {
                apkName = "app-debug.apk";
                packageName = "com.ctlev.app";
            }
            else
                throw new Exception("apkName:["+apkName+"] and packageName:["+packageName+"] should not be null or empty");

        }

        String fullPath =getAbosulatePath(context,apkName);

        // try {
        AppDetails getApkDetailNotInstalled = getApkDetailNotInstalled(context,fullPath);
        String apkDetail=getApkDetailNotInstalled.toString();
        Log.i(TAG, "compareAndUpgrade: apk at:"+fullPath+" ,"+apkDetail);

        //fullPath="com.ctlev.app";
        //fullPath=getApkDetailNotInstalled.packageName;
        AppDetails getInstalledAppDetail = null;
        try {
            getInstalledAppDetail = getInstalledAppDetail(context,packageName);
            apkDetail=getInstalledAppDetail.toString();
            Log.i(TAG, "compareAndUpgrade: package at:"+packageName+" ,"+apkDetail);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String details="";
        //        handle not insall yet case first time installation
        if(getApkDetailNotInstalled!=null && getInstalledAppDetail==null){
            install((Activity) context,new File(fullPath));
            details=packageName+" this app is not installed so requesting the installation ";

        }
        else if (getApkDetailNotInstalled.isSameAppID(getInstalledAppDetail)) {
            if (getApkDetailNotInstalled.isSameApp(getInstalledAppDetail)) {
                details += " downloaded apk and installed app are same version. No Action needed";
            }
            else
            {
                details += " downloaded apk and installed app are different version. Action ";
                if( getApkDetailNotInstalled.isUpGradable(getInstalledAppDetail)){
                    details+=" it will upgrade the app";
                    //install the app and del it.
                    install((Activity) context,new File(fullPath));
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
        details+= getApkDetailNotInstalled+" , from getInstalledAppDetail"+getInstalledAppDetail;
        Log.i(TAG, "compareAndUpgrade: full flow Details:"+details );
        Toast.makeText(context.getApplicationContext(), details , Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            AppDetails apkDetailObj = getInstalledAppDetail(context,"com.ctlev.app");
            apkDetail=apkDetailObj.toString();
            Log.i(TAG, "compareAndUpgrade: current app Details:"+apkDetail );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return details;
    }

    public  void compareAndInstall(@NonNull Context context, @NonNull String apkName) throws Exception {
        String fullPath = getAbosulatePath(context, apkName);
        AppDetails getApkDetailNotInstalled = getApkDetailNotInstalled(context,fullPath);
        String apkDetail = getApkDetailNotInstalled.toString();
        Log.i(TAG, "compareAndInstall: apk at:"+fullPath+" ,"+apkDetail);
        String packageName=getApkDetailNotInstalled.packageName;
        if (packageName==null||packageName.isEmpty()) {
            if(BuildConfig.DEBUG)
                packageName = "com.ctlev.app";
            else
                throw new Exception("compareAndInstall packageName:["+packageName+"] should not be null or empty");

        }
        compareAndUpgrade(context,apkName,packageName);
    }
    public  void compareAndInstall(@NonNull Context context, @NonNull File apk) throws Exception {

        if (!apk.getAbsolutePath().contains(getDownLoadDir(context))) {
           throw new Exception("File is not accessible,it is not in our downLoadApp folder");
        }
        String fullPath = apk.getAbsolutePath();
        AppDetails getApkDetailNotInstalled = getApkDetailNotInstalled(context,fullPath);
        String apkDetail = getApkDetailNotInstalled.toString();
        Log.i(TAG, "compareAndInstall: apk at:"+fullPath+" ,"+apkDetail);
        String packageName=getApkDetailNotInstalled.packageName;
        if (packageName==null||packageName.isEmpty()) {
            if(BuildConfig.DEBUG)
                packageName = "com.ctlev.app";
            else
                throw new Exception("compareAndInstall packageName:["+packageName+"] should not be null or empty");

        }
        compareAndUpgrade(context,apk.getName(),packageName);
    }
    public  String getDownLoadDir(@NonNull Context context){
        String fullPath =(context.getDataDir().getAbsolutePath()+DownLoadDir).trim();
        return fullPath;
    }
    public  String getAbosulatePath(@NonNull Context context, @NonNull String apkName){
        String extension="";
        if (!apkName.endsWith(".apk")){
            extension=".apk";
        }
        String fullPath =(getDownLoadDir(context)+ apkName+extension).trim();
        return fullPath;
    }

    public final static  int UNKNOWN_RESOURCE_INTENT_REQUEST_CODE=1111;
    public final static  int INSTALL_RESOURCE_INTENT_REQUEST_CODE=1112;
    public  void install(Activity context, File file){
        if (!file.exists()) return;
        Uri uri =/*Uri.fromFile(file)*/ FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        file.setReadable(true, false);
        Intent install = new Intent(Intent.ACTION_VIEW);
         install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivityForResult(install,INSTALL_RESOURCE_INTENT_REQUEST_CODE);
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
        return isSameAppID(appDetails) && appDetails.versionCode<versionCode;
    }
    public boolean isDownGradable(AppDetails appDetails){
        return isSameAppID(appDetails) && appDetails.versionCode>versionCode;
    }

}