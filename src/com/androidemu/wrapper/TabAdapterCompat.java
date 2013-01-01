package com.androidemu.wrapper;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;

public abstract class TabAdapterCompat
{
	protected final WebView webView;
	
	public static TabAdapterCompat newInstance(Activity a, WebView wv, String[][] tabs)
	{
		if (Wrapper.SDK_INT < 11)
		{
			return new TabsAdapterBase(a, wv, tabs);
		}
		else
		{
			return new TabsAdapterHoneycomb(a, wv, tabs);
		}
	}
	
	protected TabAdapterCompat(WebView webView)
	{
		this.webView = webView;
	}
	
	public View getView()
	{
		return webView;
	}
	
	public abstract void selectTab(int i);
	public abstract int getSelected();

	private static class TabsAdapterBase extends TabAdapterCompat
	{
		private final TabHost th;
		
		@SuppressWarnings("deprecation")
		public TabsAdapterBase(Context a, WebView wv, String[][] tabs)
		{
			super(wv);
			
			TabContentFactory cf = new TabContentFactory()
			{
				@Override
				public View createTabContent(String tag)
				{
					return webView;
				}
			};
			
			TabHost.OnTabChangeListener tl = new TabHost.OnTabChangeListener()
			{
				@Override
				public void onTabChanged(String tabId)
				{
					webView.loadUrl(tabId);
				}
			};
			
			th = new TabHost(a);
			
			LinearLayout ll = new LinearLayout(a);
			ll.setOrientation(LinearLayout.VERTICAL);
			
			TabWidget widget = new TabWidget(a);
			widget.setId(android.R.id.tabs);
			ll.addView(widget, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			FrameLayout frame = new FrameLayout(a);
			frame.setId(android.R.id.tabcontent);
			ll.addView(frame, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			
			th.addView(ll);
			th.setup();
			
			th.setOnTabChangedListener(tl);
			
			TabHost.TabSpec spec1 = th.newTabSpec(tabs[0][0]).setIndicator(tabs[1][0]).setContent(cf);
			TabHost.TabSpec spec2 = th.newTabSpec(tabs[0][1]).setIndicator(tabs[1][1]).setContent(cf);
			th.addTab(spec1);
			th.addTab(spec2);
		}
		
		@Override
		public View getView()
		{
			return th;
		}

		@Override
		public void selectTab(int i)
		{
			th.setCurrentTab(i);
		}

		@Override
		public int getSelected()
		{
			return th.getCurrentTab();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class TabsAdapterHoneycomb extends TabAdapterCompat
	{
		private ActionBar ab;
		
		public TabsAdapterHoneycomb(Activity a, WebView wv, String[][] tabs)
		{
			super(wv);
			
			ActionBar.TabListener tl = new ActionBar.TabListener()
			{
				@Override
				public void onTabSelected(Tab tab, FragmentTransaction ft)
				{
					webView.loadUrl((String)tab.getTag());
				}

				@Override
				public void onTabUnselected(Tab tab, FragmentTransaction ft)
				{
				}

				@Override
				public void onTabReselected(Tab tab, FragmentTransaction ft)
				{
				}
			};
			
			ab = a.getActionBar();
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			Tab tab1 = ab.newTab().setTag(tabs[0][0]).setText(tabs[1][0]).setTabListener(tl);
			Tab tab2 = ab.newTab().setTag(tabs[0][1]).setText(tabs[1][1]).setTabListener(tl);
			ab.addTab(tab1, false);
			ab.addTab(tab2, false);
		}

		@Override
		public void selectTab(int i)
		{
			ab.setSelectedNavigationItem(i);
		}

		@Override
		public int getSelected()
		{
			return ab.getSelectedNavigationIndex();
		}
	}
}
