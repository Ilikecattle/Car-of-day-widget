package com.specialcarstore.car_of_day;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RemoteViews;

public class CarOfDayConfigure extends Activity implements OnClickListener {

	private Button mDoneButton;
	private CheckBox mShowDateCheckbox;
	private CheckBox mShowRefreshButtonCheckbox;
	private Button mSelectBGColorButton;
	private Button mSelectDateFontColorButton;

	public static String colorToUse;

	private final String SHOW_DATE = "showDate";
	private final String SHOW_REFRESH = "showRefresh";
	private final String DATE_FONT_COLOR = "dateFontColor";
	private final String DATE_BG_COLOR = "dateBgColor";

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

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		mShowDateCheckbox.setChecked(prefs.getBoolean(SHOW_DATE, true));
		mShowRefreshButtonCheckbox.setChecked(prefs.getBoolean(SHOW_REFRESH,
				true));
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

		colorDialog.show();
	}

	private void selectBGColorClicked() {
		openColorPicker(DATE_BG_COLOR);
	}

	private void doneButtonClicked() {
		showAppWidget();
	}

	private void dateFontColorClicked() {
		openColorPicker(DATE_FONT_COLOR);
	}

	private void showAppWidget() {

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		RemoteViews views = new RemoteViews(getPackageName(),
				R.layout.widget_layout);
		ComponentName widget = new ComponentName(this, MyWidgetProvider.class);

		setResult(RESULT_OK);

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = prefs.edit();

		editor.putBoolean(SHOW_DATE, mShowDateCheckbox.isChecked());
		editor.commit();

		editor.putBoolean(SHOW_REFRESH, mShowRefreshButtonCheckbox.isChecked());
		editor.commit();

		views.setTextColor(R.id.date, prefs.getInt(DATE_FONT_COLOR, 0xFFF));
		views.setInt(R.id.date, "setBackgroundColor",
				prefs.getInt(DATE_BG_COLOR, 0x0));

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
}
