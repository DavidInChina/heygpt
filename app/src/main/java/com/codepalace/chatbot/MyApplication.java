package com.codepalace.chatbot;

import android.app.Application;
import android.content.Context;



public class MyApplication extends Application {
    public static Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
