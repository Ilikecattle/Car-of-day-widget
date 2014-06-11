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
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String URL = "http://specialcarstore.com/Calendar/xml_gen.php";

	private static final String DATE_CLICKED = "dateClick";
	private static final String REFRESH_CLICKED = "refreshClick";

	private Date date = new Date();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		doUpdate(context);

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if (REFRESH_CLICKED.equals(intent.getAction())) {
			Log.e("TEST", "REFRESH");
			doUpdate(context);
		} else if (DATE_CLICKED.equals(intent.getAction())) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			long startMillis = c.get(Calendar.MILLISECOND);

			Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
			builder.appendPath("time");
			ContentUris.appendId(builder, startMillis);
			Intent intent2 = new Intent(Intent.ACTION_VIEW).setData(builder
					.build());
			intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent2);
		}

	}

	public void doUpdate(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);

		if (isOnline(context)) {
			loadCar(context);
		}

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		ComponentName widget = new ComponentName(context,
				MyWidgetProvider.class);

		updateDate(views);
		
		views.setOnClickPendingIntent(R.id.date,
				getPendingSelfIntent(context, DATE_CLICKED));

		views.setOnClickPendingIntent(R.id.refreshButton,
				getPendingSelfIntent(context, REFRESH_CLICKED));

		appWidgetManager.updateAppWidget(widget, views);
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

	public void loadCar(Context context) {
		new DownloadXmlTask(context).execute(URL);
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

		private Context context;

		public DownloadXmlTask(Context context) {
			this.context = context;
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

			new ImageDownloader(context, R.id.car).execute(newestCar.imageLink);

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
