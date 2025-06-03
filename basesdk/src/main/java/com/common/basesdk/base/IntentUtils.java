package com.common.basesdk.base;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;

import androidx.core.content.FileProvider;

/**
 * author:davidinchina on 2019/8/3 00:54
 * email:davicdinchina@gmail.com
 * version:1.0.0
 * des:
 */
public class IntentUtils {
    public static final String TYPE_VIDEO = "video/*";
    public static final String TYPE_IMAGE = "image/*";

    public static void shareImage(Activity context, File file, String title) {
        shareFile(context, file, TYPE_IMAGE, title);
    }

    public static void shareVideo(Activity context, File file, String title) {
        shareFile(context, file, TYPE_VIDEO, title);
    }

    public static void shareFile(Activity context, File file, String shareType, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (TYPE_VIDEO.equals(shareType)) {
                fileUri = getVideoContentUri(context, file);
            } else {
                fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            fileUri = Uri.fromFile(file);
        }
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setType(shareType);
        Intent chooser = Intent.createChooser(intent, title);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
    }

    private static Uri getVideoContentUri(Context context, File file) {
        String filePath = file.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=?",
                new String[]{filePath}, null);
        Uri uri = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                Uri baseUri = Uri.parse("content://media/external/video/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }
            cursor.close();
        }
        //如果使用fileprovider获取失败，则使用此方法
        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
        if (uri == null) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        }
        return uri;
    }
}
