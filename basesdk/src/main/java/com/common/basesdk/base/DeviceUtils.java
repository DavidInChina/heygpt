package com.common.basesdk.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.List;

public class DeviceUtils {
    public static final String DEFAULT_VALUE = "NONE";

    public static String getAndroidID(Context context) {
        String androidID = DEFAULT_VALUE;
        Class<?> cls = Settings.Secure.class;
        try {
            Field fld = cls.getDeclaredField("ANDROID_ID");
            if (fld == null) {
                return androidID;
            }
        } catch (NoSuchFieldException e) {
            return androidID;
        }
        return getAndroidIDNormal(context);
    }

    private static String getAndroidIDNormal(Context context) {
        try {
            String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return defaultValue(id);
        } catch (Throwable t) {
            return DEFAULT_VALUE;
        }
    }

    private static String defaultValue(String val) {
        return TextUtils.isEmpty(val) ? DEFAULT_VALUE : val;
    }

    public static boolean hasInstalled(Context context, String pkg) {
        if (context == null)
            return false;

        PackageInfo packageInfo;
        PackageManager packageManager;
        try {
            packageManager = context.getPackageManager();
            packageInfo = packageManager.getPackageInfo(pkg, 0);
            if (null != packageInfo) {
                return packageManager.getApplicationEnabledSetting(pkg) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            }
        } catch (Exception e) {}
        return false;
    }

    public static void startAppWithPackageName(Context context, String packagename) {
        if (context == null || TextUtils.isEmpty(packagename))
            return;

        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }
}
