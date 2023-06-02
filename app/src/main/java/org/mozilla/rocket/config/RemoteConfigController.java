package org.mozilla.rocket.config;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anysitebrowser.base.core.settings.Settings;
import com.anysitebrowser.base.core.utils.lang.ObjectStore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.mozilla.focus.R;

public class RemoteConfigController {

    private static final String mSPName = "my_anysite_browser_sp";
    private static final String TAG = "RemoteConfigController";
    public static FirebaseRemoteConfig mFirebaseRemoteConfig;
    public static String versionName;

    public static FirebaseRemoteConfig getRemoteConfig() {
        if (mFirebaseRemoteConfig == null) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        }
        return mFirebaseRemoteConfig;
    }

    public static void acquireRemoteConfig() {

        versionName = getVersionName(ObjectStore.getContext()
                , ObjectStore.getContext().getPackageName());
        Log.d(TAG, "--------versionName-----" + versionName);
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60).build();
        getRemoteConfig().setConfigSettingsAsync(configSettings);
        getRemoteConfig().setDefaultsAsync(R.xml.remote_config_defaults);
        getRemoteConfig().fetchAndActivate().addOnFailureListener(e ->
                //失败重新拉取一次
                getRemoteConfig().fetchAndActivate()
        );

        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.fetchAndActivate();
                        } else {
                            mFirebaseRemoteConfig.fetchAndActivate();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String aa = "";
            }
        });
    }

    public static boolean getFirebaseBoolean(String key) {
        boolean fireBooleanValue = RemoteConfigController.getRemoteConfig().getBoolean(key);
        Log.d(TAG, "--------fireBooleanValue-----" + fireBooleanValue + "---key--" + key);
        String firevalue = new Settings(ObjectStore.getContext(), mSPName).get(key);
        if (TextUtils.isEmpty(firevalue)) {
            new Settings(ObjectStore.getContext(), mSPName).setBoolean(key, fireBooleanValue);
        }
        if (!TextUtils.isEmpty(versionName) && versionName.contains("_test")) {
            return new Settings(ObjectStore.getContext(), mSPName).getBoolean(key);
        }
        return fireBooleanValue;
    }

    public static String getFirebaseString(String key) {
        String fireStringValue = RemoteConfigController.getRemoteConfig().getString(key);
        Log.d(TAG, "--------fireStringValue-----" + fireStringValue + "---key--" + key);
        String firevalue = new Settings(ObjectStore.getContext(), mSPName).get(key);
        if (TextUtils.isEmpty(firevalue)) {
            new Settings(ObjectStore.getContext(), mSPName).set(key, fireStringValue);
        }
        if (!TextUtils.isEmpty(versionName) && versionName.contains("_test")) {
            return new Settings(ObjectStore.getContext(), mSPName).get(key);
        }
        return fireStringValue;
    }

    public static Long getFirebaseLong(String key) {
        long fireLongValue = RemoteConfigController.getRemoteConfig().getLong(key);
        String firevalue = new Settings(ObjectStore.getContext(), mSPName).get(key);
        if (TextUtils.isEmpty(firevalue)) {
            new Settings(ObjectStore.getContext(), mSPName).setLong(key, fireLongValue);
        }
        Log.d(TAG, "--------fireLongValue-----" + fireLongValue + "---key--" + key);
        if (!TextUtils.isEmpty(versionName) && versionName.contains("_test")) {
            return new Settings(ObjectStore.getContext(), mSPName).getLong(key);
        }
        return fireLongValue;
    }

    public static int getFirebaseInt(String key) {
        int fireIntValue = (int) RemoteConfigController.getRemoteConfig().getLong(key);
        Log.d(TAG, "--------fireIntValue-----" + fireIntValue + "---key--" + key);
        String firevalue = new Settings(ObjectStore.getContext(), mSPName).get(key);
        if (TextUtils.isEmpty(firevalue)) {
            new Settings(ObjectStore.getContext(), mSPName).setInt(key, fireIntValue);
        }
        if (!TextUtils.isEmpty(versionName) && versionName.contains("_test")) {
            return new Settings(ObjectStore.getContext(), mSPName).getInt(key);
        }
        return fireIntValue;
    }

    public static String getVersionName(Context context, String packageName) {
            PackageManager pm = context.getPackageManager();
            String sAppVersionName = "unknown";
            try {
                sAppVersionName = pm.getPackageInfo(packageName, 0).versionName;
            } catch (PackageManager.NameNotFoundException var4) {
                return "unknown";
            }
        return sAppVersionName;
    }
}
