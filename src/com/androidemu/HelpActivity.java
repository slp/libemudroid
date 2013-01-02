package com.androidemu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.androidemu.wrapper.Wrapper;
import com.androidemu.wrapper.TabAdapterCompat;

public class HelpActivity extends Activity
{
	TabAdapterCompat tac;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Wrapper.displayHomeAsUp(this);
		
		WebView view = new WebView(this);

		// avoid unwanted link selection, when switching tabs
		view.getSettings().setNeedInitialFocus(false);

		Intent params = getIntent();
		String[][] tabContent = new String[][] {
				new String[] { params.getData().toString(),
						"file:///android_asset/gpl.html" },
				new String[] { params.getStringExtra(""), "License" } };
		tac = TabAdapterCompat.newInstance(this, view, tabContent);

		setContentView(tac.getView());
		
		tac.selectTab(savedInstanceState == null? 0 : savedInstanceState.getInt("currentTab"));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("currentTab", tac.getSelected());

		super.onSaveInstanceState(outState);
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
