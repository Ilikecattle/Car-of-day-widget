package com.christianwilcox.widget_test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	public static final String WIFI = "Wi-Fi";
	public static final String ANY = "Any";
	private static final String URL = "http://specialcarstore.com/Calendar/xml_gen.php";

	private static boolean wifiConnected = true;
	private static boolean mobileConnected = false;
	public static boolean refreshDisplay = true;
	public static String sPref = WIFI;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int widgetId = appWidgetIds[i];

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);

			appWidgetManager.updateAppWidget(widgetId, views);
		}
		
		loadCar(context, appWidgetManager, appWidgetIds);
	}

	public void loadCar(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		if ((sPref.equals(ANY)) && (wifiConnected || mobileConnected)) {
			new DownloadXmlTask(context, appWidgetManager, appWidgetIds).execute(URL);
		} else if ((sPref.equals(WIFI)) && (wifiConnected)) {
			new DownloadXmlTask(context, appWidgetManager, appWidgetIds).execute(URL);
		} else {
			// show error
		}
	}

	private List<Car> loadXmlFromNetwork(String urlString)
			throws XmlPullParserException, IOException {
		InputStream stream = null;
		CarOfDayXmlParser carOfDayXmlParser = new CarOfDayXmlParser();
		List<Car> cars = null;

		try {
			stream = downloadUrl(URL);
		} catch (IOException e) {

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

		public DownloadXmlTask(Context context, AppWidgetManager appWidgetManager,
				int[] appWidgetIds) {
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
			new ImageDownloader(context, appWidgetManager, appWidgetIds, R.id.imageView1).execute(cars.get(0).imageLink);
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
}
