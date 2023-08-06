package com.ctlev.app.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public class SharedPreferenceStorage {

    private SharedPreferences sharedPreferences;
    boolean isCommitted=false;
    private SharedPreferences.Editor editor;

    private static class SharedPreferenceStorageHelper{
        public static SharedPreferenceStorage sharedPreferenceStorage=new SharedPreferenceStorage();
    }
    private SharedPreferenceStorage(){ }

    private final String preferenceName="secret_shared_prefs";

    public static SharedPreferenceStorage getSharedPreferences(Context context) {
        return SharedPreferenceStorageHelper.sharedPreferenceStorage
                .initSharedPreferences(context);
    }

    private SharedPreferenceStorage initSharedPreferences(Context context) {
        sharedPreferences=context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        return this;
    }

    private SharedPreferenceStorage initEncryptedSharedPreferences(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            sharedPreferences = EncryptedSharedPreferences.create(
                    preferenceName,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            sharedPreferences=context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        }
        editor = sharedPreferences.edit();
        return this;
    }

    // use the shared preferences and editor as you normally would

    public boolean setData(String key, String value){
        editor.putString(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, Set<String> value){
        editor.putStringSet(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, int value){
        editor.putInt(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, long value){
        editor.putLong(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, float value){
        editor.putFloat(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, boolean value){
        editor.putBoolean(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, Integer value){
        editor.putInt(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, Long value){
        editor.putLong(key, value);
        return save(isCommitted);
    }
    public boolean setData(String key, Float value){
        editor.putFloat(key, value);
        return save(isCommitted);
    }

    public boolean setData(String key, Boolean value){
        editor.putBoolean(key, value);
        return save(isCommitted);
    }

    public String getData(String key, String value){
        return sharedPreferences.getString(key, value);
    }
    public boolean getData(String key, boolean value){
        return sharedPreferences.getBoolean(key, value);
    }
    public float getData(String key, float value){
        return sharedPreferences.getFloat(key, value);
    }
    public long getData(String key, long value){
        return sharedPreferences.getLong(key, value);
    }
    public int getData(String key, int value){
        return sharedPreferences.getInt(key, value);
    }

    public void remove(String key){
        editor.remove(key);
         save(isCommitted);
    }
    public void deleteAllData(String key){
        editor.clear();
        save(isCommitted);
    }

    /**
     * use the return value for commit only to return
     * not for apply
     */
    private boolean save(boolean isCommitted){
        if (isCommitted){
            return editor.commit();
        }else {
            editor.apply();
            return false;
        }
    }



}
