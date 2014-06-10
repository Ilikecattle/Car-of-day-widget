package com.specialcarstore.car_of_day;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Browser;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String URL = "http://specialcarstore.com/Calendar/xml_gen.php";

	private static final String LEFT_CLICKED = "leftButtonClick";
	private static final String RIGHT_CLICKED = "rightButtonClick";
	private static final String CAR_CLICKED = "carClick";
	private static final String DATE_CLICKED = "dateClick";

	private Date date = new Date();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		if (isOnline(context)) {
			loadCar(context, appWidgetManager, appWidgetIds);
		}

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		ComponentName widget = new ComponentName(context,
				MyWidgetProvider.class);

		updateDate(views);

		/*
		 * views.setOnClickPendingIntent(R.id.left_button,
		 * getPendingSelfIntent(context, LEFT_CLICKED));
		 * 
		 * views.setOnClickPendingIntent(R.id.right_button,
		 * getPendingSelfIntent(context, RIGHT_CLICKED));
		 */

		views.setOnClickPendingIntent(R.id.date,
				getPendingSelfIntent(context, DATE_CLICKED));

		/*
		 * views.setOnClickPendingIntent(R.id.date,
		 * getPendingIntentCalendarActivity(context));
		 */

		appWidgetManager.updateAppWidget(widget, views);
	}

	@Override
	public void onEnabled(Context context) {

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		ComponentName widget = new ComponentName(context,
				MyWidgetProvider.class);

		if (LEFT_CLICKED.equals(intent.getAction())) {

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);

			Calendar c = Calendar.getInstance();

			c.setTime(date);

			Log.e("TEST", "DATE" + c.get(Calendar.DAY_OF_MONTH));

			c.roll(Calendar.DATE, -1);
			date = c.getTime();
			updateDate(views);

			appWidgetManager.updateAppWidget(widget, views);
		} else if (RIGHT_CLICKED.equals(intent.getAction())) {
			Log.e("Test", "RIGHT");
		} else if (CAR_CLICKED.equals(intent.getAction())) {
			Log.e("TEST", "CAR");
		} else if (DATE_CLICKED.equals(intent.getAction())) {
			
			// A date-time specified in milliseconds since the epoch.
			long startMillis = 1000;
			
			Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
			builder.appendPath("time");
			ContentUris.appendId(builder, startMillis);
			Intent intent2 = new Intent(Intent.ACTION_VIEW)
			    .setData(builder.build());
			intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent2);
			
			/*Intent mailClient = new Intent(Intent.ACTION_MAIN);
			final ComponentName cn = new ComponentName("com.google.android.calendar",
					"com.google.android.calendar.LaunchActivity");
			intent.setComponent(cn);
			mailClient.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mailClient);*/
		}

	}

	protected PendingIntent getPendingIntentOpenArticle(Context context, Car car) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(car.link));
		intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		return pendingIntent;

	}

	protected PendingIntent getPendingIntentCalendarActivity(Context context) {

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Calendar tempCal = Calendar.getInstance();

		intent.putExtra("beginTime", tempCal.getTimeInMillis());
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.setClassName("com.android.calendar",
				"com.google.android.calendar.AgendaActivity");

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		return pendingIntent;
	}

	protected PendingIntent getPendingSelfIntent(Context context, String action) {
		Intent intent = new Intent(context, getClass());
		intent.setAction(action);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	public void loadCar(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		new DownloadXmlTask(context, appWidgetManager, appWidgetIds)
				.execute(URL);
	}

	private List<Car> loadXmlFromNetwork(String urlString)
			throws XmlPullParserException, IOException {
		InputStream stream = null;
		CarOfDayXmlParser carOfDayXmlParser = new CarOfDayXmlParser();
		List<Car> cars = null;

		try {
			stream = downloadUrl(URL);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (stream != null) {
			try {
				cars = carOfDayXmlParser.parse(stream);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return cars;
	}

	private class DownloadXmlTask extends AsyncTask<String, Void, List<Car>> {

		private AppWidgetManager appWidgetManager;
		private Context context;
		private int[] appWidgetIds;

		public DownloadXmlTask(Context context,
				AppWidgetManager appWidgetManager, int[] appWidgetIds) {
			this.context = context;
			this.appWidgetManager = appWidgetManager;
			this.appWidgetIds = appWidgetIds;
		}

		@Override
		protected List<Car> doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				return null;
			} catch (XmlPullParserException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Car> cars) {
			Car newestCar = cars.get(0);

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);

			ComponentName widget = new ComponentName(context,
					MyWidgetProvider.class);

			views.setOnClickPendingIntent(R.id.car,
					getPendingIntentOpenArticle(context, newestCar));

			new ImageDownloader(context, appWidgetManager, appWidgetIds,
					R.id.car).execute(newestCar.imageLink);

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);

			appWidgetManager.updateAppWidget(widget, views);
		}
	}

	// Given a string representation of a URL, sets up a connection and gets
	// an input stream.
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		return conn.getInputStream();
	}

	private boolean isOnline(Context context) {

		if (context != null) {
			ConnectivityManager connMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			return (networkInfo != null && networkInfo.isConnected());
		} else {
			return false;
		}

	}

	private void updateDate(RemoteViews views) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
		String currentDate = sdf.format(date);

		views.setTextViewText(R.id.date, currentDate);
	}
}
