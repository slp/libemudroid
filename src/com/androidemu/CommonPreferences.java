package com.androidemu;

import com.androidemu.wrapper.Wrapper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class CommonPreferences extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.common);
		
		Wrapper.displayHomeAsUp(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				if (Wrapper.SDK_INT < 16)
				{
					// Game activity is system-global, so
					// FLAG_ACTIVITY_CLEAR_TOP is not needed
					PackageManager pm = getPackageManager();
				    Intent intent = pm.getLaunchIntentForPackage(getPackageName()).cloneFilter();
				    startActivity(intent);
					return true;
				}
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
