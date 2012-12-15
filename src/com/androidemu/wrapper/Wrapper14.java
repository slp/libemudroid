package com.androidemu.wrapper;

import android.annotation.TargetApi;

import android.content.Context;

import android.os.Build;

import android.view.ViewConfiguration;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Wrapper14
{
	public static boolean isHwMenuBtnAvailable(Context ctx)
	{
		return ViewConfiguration.get(ctx).hasPermanentMenuKey();
	}
}
