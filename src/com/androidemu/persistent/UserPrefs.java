package com.androidemu.persistent;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.androidemu.R;

public class UserPrefs implements OnSharedPreferenceChangeListener
{
	private static UserPrefs instance = null;

	private boolean dirty = false;
	
	protected SharedPreferences prefsData;
	protected Resources res;
	
	public final boolean fullScreen;
	public final boolean hideNav;
	public final boolean hintShown_fullScreen;
	
	public static UserPrefs getInstance(Application application)
	{
		return (instance == null || instance.dirty) ?
				createInstance(application.getApplicationContext()) : instance;
	}
	
	private static UserPrefs createInstance(Context ctx)
	{
		if (instance != null)
		{
			instance.unregisterCallback();
		}
		instance = new UserPrefs(ctx);
		instance.registerCallback();
		return instance;
	}

	protected UserPrefs(Context context)
	{
		prefsData = PreferenceManager.getDefaultSharedPreferences(context);
		
		res = context.getResources();
		
		fullScreen = prefsData.getBoolean("fullScreen", res.getBoolean(R.bool.def_fullScreen));
		hideNav = prefsData.getBoolean("hideNav", res.getBoolean(R.bool.def_hideNav));

		hintShown_fullScreen = prefsData.getBoolean("fullscreen_hint_shown", false);
	}

	private void registerCallback()
	{
		prefsData.registerOnSharedPreferenceChangeListener(this);
	}
	
	private void unregisterCallback()
	{
		prefsData.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	public void setHintShown()
	{
		prefsData.edit().putBoolean("fullscreen_hint_shown", true).commit();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		dirty = true;
	}
}
