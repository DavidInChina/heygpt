package com.common.basesdk.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2017/12/27.
 */

public class Utils {

    private static final String TAG = "Utils";
    private static char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String PREF_THEME_MD5 = "pref_timestamp";

    /**
     * 判断通知使用权
     *
     * @param context
     * @return
     */
    public static boolean notificationListenersEnable(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return true;
        }
        String str = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(str)) {
            String splitArray[] = str.split(":");
            if (splitArray != null && splitArray.length > 0) {
                ComponentName cmp = null;
                for (int index = 0; index < splitArray.length; index++) {
                    cmp = ComponentName.unflattenFromString(splitArray[index]);
                    if ((cmp != null) && (TextUtils.equals(context.getPackageName(), cmp.getPackageName()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 进入通知栏使用权设置界面
     *
     * @param context
     */
    public static void enterNotificationSettings(Context context) {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$NotificationAccessSettingsActivity");
                intent.setComponent(cn);
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings");
                context.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String getScreenSize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels + "," + displayMetrics.heightPixels;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 此md5计算函数，只能计算纯英文字符串，对带有中文的
     * 字符串计算有误
     *
     * @param str
     * @return
     */
    public static String string2MD5(String str) {
        MessageDigest md5 = null;
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            Log.d(TAG, "error : " + e);
            return "";
        }
        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = md5Bytes[i] & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }


    public static void save(String filePath, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(content.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "error : " + e);
        }
    }

    public static String restore(String filePath) {
        try {
            StringBuilder builder = new StringBuilder();
            FileInputStream fis = new FileInputStream(filePath);
            byte[] buf = new byte[1024];
            int read = 0;
            while ((read = fis.read(buf)) > 0) {
                builder.append(new String(buf, 0, read));
            }
            fis.close();
            return builder.toString();
        } catch (Exception e) {
            Log.e(TAG, "error : " + e);
        }
        return null;
    }

    public static boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getProcessName(context));
    }

    /**
     * 获取进程名称
     *
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

    public static void putInt(Context context, String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key) {
        return getInt(context, key, 0);
    }

    public static void putLong(Context context, String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).apply();
    }

    public static long getLong(Context context, String key) {
        return getLong(context, key, 0L);
    }

    public static long getLong(Context context, String key, Long defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
    }

    public static int getInt(Context context, String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue);
    }

    public static void putString(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key) {
        return getString(context, key, "");
    }

    public static String getString(Context context, String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static void registerOnSharedPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
    }

    public static File getCacheeDir(Context context) {
        File fileDir = context.getFilesDir();
        return new File(fileDir, "wallpaper_cache");
    }

    public static String getDefaultWallpaperFile(Context context, String fileName) {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WallPaper/", fileName).getAbsolutePath();
    }

    //将文件复制到指定文件夹下
    public static void copyAssets(Context context, String fileName) {
        String dstPath = getDefaultWallpaperFile(context, fileName);
        try {
            if (TextUtils.equals(md5sumAssetsFile(context, fileName), md5sum(dstPath))) {
                return;
            }
            InputStream is = context.getAssets().open(fileName);
            File dstFile = new File(dstPath);
            if (dstFile.exists()) {
                dstFile.delete();
            }
            dstFile.getParentFile().mkdirs();
            dstFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(dstPath);
            byte[] buf = new byte[4096];
            int read = 0;
            while ((read = is.read(buf)) > 0) {
                fos.write(buf, 0, read);
            }
            is.close();
            fos.close();
        } catch (Exception e) {
            Log.d(TAG, "error : " + e);
        }
    }

    public static void copyFile(String fileName, String dstPath) {
        try {
            InputStream is = new FileInputStream(fileName);
            File dstFile = new File(dstPath);
            if (dstFile.exists()) {
                dstFile.delete();
            }
            dstFile.getParentFile().mkdirs();
            dstFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(dstPath);
            byte[] buf = new byte[4096];
            int read = 0;
            while ((read = is.read(buf)) > 0) {
                fos.write(buf, 0, read);
            }
            is.close();
            fos.close();
        } catch (Exception e) {
            Log.d(TAG, "error : " + e);
        }
    }

    public static String md5sum(String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5 = null;
        try {
            fis = new FileInputStream(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            Log.d(TAG, "error : " + e);
        }
        return null;
    }

    public static String md5sumAssetsFile(Context context, String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5 = null;
        try {
            fis = context.getAssets().open(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            Log.d(TAG, "error : " + e);
        }
        return null;
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static boolean canDrawOverlays(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(context);
            }
        } catch (Exception e) {
        } catch (Error e) {
        }
        return true;
    }


    public static void openOverlaySettings(Context context, int requestCode) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
        }
    }

    public static void setLastThemeMd5(Context context, String md5) {
        putString(context, PREF_THEME_MD5, md5);
    }

    public static String getLastThemeMd5(Context context) {
        return getString(context, PREF_THEME_MD5, "");
    }

    public static boolean isInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
            if (info != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Log.d(Log.TAG, "error : " + e);
        }
        return false;
    }

    public static String getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
        }
        return null;
    }

    public static String getCurrentDay() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            ///
        }
        return "";
    }

    public static String getBeforeDay() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);// HH:mm:ss
            Date date = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            ///
        }
        return "";
    }
}