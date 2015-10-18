package hu.csany_zeg.one.csanydroid1;

import android.app.Application;
import android.content.Context;

public class App extends Application {

	private static Context mContext = null;

	public static Context getContext() {
		return mContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
	}


}
