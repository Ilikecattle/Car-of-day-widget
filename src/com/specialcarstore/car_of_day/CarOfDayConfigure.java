package com.specialcarstore.car_of_day;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import java.text.SimpleDateFormat;
import java.util.Date;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RemoteViews;

public class CarOfDayConfigure extends Activity implements OnClickListener {

	int mAppWidgetId;
	private Button mDoneButton;
	private CheckBox mShowDateCheckbox;
	private CheckBox mShowRefreshButtonCheckbox;
	private Button mSelectBGColorButton;
	private Button mSelectDateFontColorButton;

	public static String colorToUse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_activity);
		setResult(RESULT_CANCELED);

		init();
	}

	public void init() {
		mDoneButton = (Button) findViewById(R.id.doneButton);
		mShowDateCheckbox = (CheckBox) findViewById(R.id.dateCheckBox);
		mShowRefreshButtonCheckbox = (CheckBox) findViewById(R.id.refreshButtonCheckBox);
		mSelectBGColorButton = (Button) findViewById(R.id.dateBGColorSelect);
		mSelectDateFontColorButton = (Button) findViewById(R.id.dateFontColor);

		mDoneButton.setOnClickListener(this);
		mSelectBGColorButton.setOnClickListener(this);
		mSelectDateFontColorButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.doneButton:
			doneButtonClicked();
			break;
		case R.id.dateBGColorSelect:
			selectBGColorClicked();
			break;
		case R.id.dateFontColor:
			dateFontColorClicked();
			break;
		}

	}

	public void openColorPicker(String colorToUse) {
		CarOfDayConfigure.colorToUse = colorToUse;

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int initialValue = prefs.getInt(CarOfDayConfigure.colorToUse,
				0xFF000000);

		final ColorPickerDialog colorDialog = new ColorPickerDialog(this,
				initialValue);

		colorDialog.setAlphaSliderVisible(true);
		colorDialog.setTitle("Pick a Color!");

		colorDialog.setButton(DialogInterface.BUTTON_POSITIVE,
				getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt(CarOfDayConfigure.colorToUse,
								colorDialog.getColor());
						editor.commit();
					}
				});

		colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Nothing to do here.
					}
				});

		colorDialog.show();
	}

	private void selectBGColorClicked() {
		openColorPicker("dateBGColor");
	}

	private void doneButtonClicked() {
		showAppWidget();
	}

	private void dateFontColorClicked() {
		openColorPicker("dateFontColor");
	}

	private void showAppWidget() {

		mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
					INVALID_APPWIDGET_ID);

			AppWidgetProviderInfo providerInfo = AppWidgetManager.getInstance(
					getBaseContext()).getAppWidgetInfo(mAppWidgetId);

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this);
			RemoteViews views = new RemoteViews(getPackageName(),
					R.layout.widget_layout);
			ComponentName widget = new ComponentName(this,
					MyWidgetProvider.class);

			setResult(RESULT_OK);

			final SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			SharedPreferences.Editor editor = prefs.edit();

			editor.putBoolean("showDate", mShowDateCheckbox.isChecked());
			editor.commit();

			editor.putBoolean("showRefresh",
					mShowRefreshButtonCheckbox.isChecked());
			editor.commit();

			views.setTextColor(R.id.date,
					prefs.getInt("dateFontColor", 0xAAAAAAAA));
			SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
			String currentDate = sdf.format(new Date());

			views.setTextViewText(R.id.date, currentDate);
			views.setInt(R.id.date, "setBackgroundColor",
					prefs.getInt("dateBGColor", 0x0));

			if (mShowRefreshButtonCheckbox.isChecked()) {
				views.setViewVisibility(R.id.refreshButton, View.VISIBLE);
			} else {
				views.setViewVisibility(R.id.refreshButton, View.GONE);
			}

			if (mShowDateCheckbox.isChecked()) {
				views.setViewVisibility(R.id.date, View.VISIBLE);
			} else {
				views.setViewVisibility(R.id.date, View.GONE);
			}

			appWidgetManager.updateAppWidget(widget, views);

			finish();
		}
		if (mAppWidgetId == INVALID_APPWIDGET_ID) {
			finish();
		}

	}
}
