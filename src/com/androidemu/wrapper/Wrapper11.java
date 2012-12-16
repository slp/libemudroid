package com.androidemu.wrapper;

import android.annotation.TargetApi;

import android.os.Build;

import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class Wrapper11
{
	static void View_setSystemUiVisibility(View view, int visibility)
	{
		view.setSystemUiVisibility(visibility);
	}
}
