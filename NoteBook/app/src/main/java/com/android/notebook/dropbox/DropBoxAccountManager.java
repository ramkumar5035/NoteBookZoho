package com.android.notebook.dropbox;

import android.content.Context;

import com.dropbox.sync.android.DbxAccountManager;

public final class DropBoxAccountManager {
    private DropBoxAccountManager() {
    }

    public static final String appKey = "3672vr0mw4vv4j5";
    public static final String appSecret = "44xh0rnna9o4kmk";

    public static DbxAccountManager getAccountManager(Context context) {
        return DbxAccountManager.getInstance(context.getApplicationContext(), appKey, appSecret);
    }


}
