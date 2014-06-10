package com.specialcarstore.car_of_day;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

public class CarOfDayConfigure extends Activity {
	private int mAppWidgetId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_activity);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {

			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		if(mAppWidgetId == 0)
		{
			finish();
		}
	}
}
