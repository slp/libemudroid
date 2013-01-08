package com.androidemu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import com.androidemu.persistent.UserPrefs;
import com.androidemu.wrapper.Wrapper;
import com.androidemu.wrapper.SystemUiHider;
import com.androidemu.wrapper.SystemUiHider.OnVisibilityChangeListener;

public class GameActivity extends Activity
{
	private static final Logger logger;
	static
	{
		if (BuildConfig.DEBUG)
		{
			logger = Logger.getLogger("Emudroid");
			logger.setLevel(Level.INFO);
		}
	}

	protected static final int DIALOG_EXIT_PROMPT = 0;
	protected static final int DIALOG_FULLSCREEN_HINT = 100;

	private int fullScreenCfg;

	private Handler hideHandler;
	private Runnable hideRunnable;

	protected SystemUiHider uiHider;
	protected UserPrefs cfg;
	protected Resources res;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		debug("onCreate");

		super.onCreate(savedInstanceState);
		
		initResources();

		Wrapper.disableHomeButton(this);

		Intent serviceIntent = new Intent(this, EmulatorService.class).setAction(
				EmulatorService.ACTION_FOREGROUND).putExtra("target",
				getClass().getName());
		startService(serviceIntent);
		
		if (Wrapper.SDK_INT < 11)
		{
			debug("Pre-Honeycomb - hiding title separately");

			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		}
		else
			debug("API >= 11 => title = ActionBar");

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	private void initResources()
	{
		res = getResources();
		cfg = UserPrefs.getInstance(getApplication());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		debug("onConfigurationChanged");
		
		super.onConfigurationChanged(newConfig);
		
		initResources();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		debug("onPostCreate");
		
		super.onPostCreate(savedInstanceState);

		if (!Wrapper.isHwMenuBtnAvailable(this) && !cfg.hintShown_fullScreen)
		{
			if (Wrapper.SDK_INT >= 11) showDialog(DIALOG_FULLSCREEN_HINT);
		}
	}

	@Override
	protected void onResume()
	{
		debug("onResume");
		
		super.onResume();
		
		/*
		 * On Honeycomb and newer:
		 * 1) No fullscreen -> fullScreenCfg = 0
		 * 2) Fullscreen with hidden controls -> fullScreenCfg has HIDE_NAVIGATION
		 * 3) Fullscreen with dimmed controls -> fullScreenCfg != 0
		 * Otherwise:
		 * 1) No fullscreen -> fullScreenCfg = 0
		 * 2) Fullscreen -> fullScreenCfg has FULLSCREEN (HIDE_NAVIGATION contains FULLSCREEN)
		 */
		if (cfg.fullScreen)
		{
			fullScreenCfg = cfg.hideNav ? SystemUiHider.FLAG_HIDE_NAVIGATION
					: Wrapper.SDK_INT < 11 ? SystemUiHider.FLAG_FULLSCREEN | SystemUiHider.FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES
							: SystemUiHider.FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES;
		}
		else
		{
			fullScreenCfg = 0;
		}

		if (fullScreenCfg != 0)
		{
			debug("Initializing UI hiding");

			hideHandler = new Handler();
			hideRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					uiHider.hide();
				}
			};
			uiHider = SystemUiHider.getInstance(this, findViewById(android.R.id.content), fullScreenCfg);
			uiHider.setup();

			uiHider.setOnVisibilityChangeListener(new OnVisibilityChangeListener()
			{
				@Override
				public void onVisibilityChange(boolean visible)
				{
					if (visible)
					{
						hideUiDelayed();
					}
				}
			});
		}

		// show stuff after setting change
		if (uiHider != null && fullScreenCfg == 0)
		{
			uiHider.show();
		}
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu)
	{
		if (Wrapper.SDK_INT < 11 && fullScreenCfg != 0)
		{
			uiHider.show();
			hideHandler.removeCallbacks(hideRunnable);
		}
		
		return super.onMenuOpened(featureId, menu);
	}
	
	@Override
	public void onOptionsMenuClosed(Menu menu)
	{
		if (Wrapper.SDK_INT < 11 && fullScreenCfg != 0)
		{
			uiHider.hide();
		}

		super.onOptionsMenuClosed(menu);
	}
	
	@Override
	protected void onStart()
	{
		debug("onStart");

		super.onStart();
	}

	@Override
	public void onBackPressed()
	{
		// nothing
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
		{
			if (fullScreenCfg != 0)
			{
				hideUiDelayed();
			}
			else if (uiHider != null)
			{
				uiHider.show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		debug("onCreateOptionsMenu");

		getMenuInflater().inflate(R.menu.base, menu);
		menu.findItem(R.id.menu_help).setIntent(
				new Intent(this, HelpActivity.class).setData(
						Uri.parse("file:///android_asset/faq.html")).putExtra("", "FAQ"));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPause()
	{
		debug("onPause");

		if (uiHider != null)
		{
			uiHider.setOnVisibilityChangeListener(null);
		}

		if (hideHandler != null)
		{
			hideHandler.removeCallbacks(hideRunnable);
		}

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		debug("onDestroy");

		stopService(new Intent(this, EmulatorService.class));

		super.onDestroy();
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				if (Wrapper.SDK_INT <= 5 && event.getRepeatCount() == 0) onBackPressed();
			case KeyEvent.KEYCODE_CAMERA:
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onSearchRequested()
	{
		return false;
	}

	/*
	 * @Override public boolean dispatchKeyEvent(KeyEvent event) {
	 * 
	 * 
	 * return super.dispatchKeyEvent(event); }
	 */

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
			case DIALOG_FULLSCREEN_HINT:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				final View dialogView = getLayoutInflater().inflate(R.layout.dialog_hint,
						null);

				return builder.setView(dialogView).setCancelable(false)
						.setPositiveButton(android.R.string.ok, new OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								if (((CheckBox) dialogView.findViewById(R.id.shown))
										.isChecked())
								{
									cfg.setHintShown();
								}
							}
						}).create();
		}

		return super.onCreateDialog(id);
	}

	protected void hideUiDelayed()
	{
		hideHandler.removeCallbacks(hideRunnable);

		if (fullScreenCfg != 0)
		{
			debug("Sheduling UI hiding");

			hideHandler.postDelayed(hideRunnable, 3000);
		}
	}

	protected boolean extractAsset(File file)
	{
		if (!file.exists())
		{
			debug(file.getPath() + " not found - extracting");

			InputStream in = null;
			OutputStream out = null;

			try
			{
				in = getAssets().open(file.getName());
				out = new FileOutputStream(file);

				byte[] buf = new byte[8192];
				int len;
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);

			}
			catch (Exception e)
			{
				log(e, "Failed to extract " + file.getPath());

				return false;
			}
			finally
			{
				try
				{
					if (out != null) out.close();
					if (in != null) in.close();
				}
				catch (IOException e)
				{
					log(e, "Failed to close stream after extraction");
				}
			}
		}
		else
			debug(file.getPath() + " already exist - skipped");

		return true;
	}

	protected static void debug(String logMessage)
	{
		if (BuildConfig.DEBUG)
		{
			logger.info(logMessage);
		}
	}

	protected static void log(Exception e, String logMessage)
	{
		logger.severe(logMessage);
		e.printStackTrace();
	}
}
