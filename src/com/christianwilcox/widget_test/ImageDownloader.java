package com.christianwilcox.widget_test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
	private int viewId;
	private AppWidgetManager appWidgetManager;
	private Context context;
	private int[] appWidgetIds;

	public ImageDownloader(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds, int viewId) {
		this.context = context;
		this.appWidgetManager = appWidgetManager;
		this.appWidgetIds = appWidgetIds;
		this.viewId = viewId;
	}

	protected Bitmap doInBackground(String... urls) {
		String url = urls[0];
		Bitmap mIcon = null;

		try {
			InputStream in;
			url = url.replace(" ", "%20");
			in = new java.net.URL(url).openStream();
			mIcon = BitmapFactory.decodeStream(in);
		} catch (MalformedURLException e) {
		
		} catch (IOException e) {

		}

		return mIcon;
	}

	protected void onPostExecute(Bitmap result) {
		
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int widgetId = appWidgetIds[i];

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);

			views.setImageViewBitmap(viewId, result);
			appWidgetManager.updateAppWidget(widgetId, views);
		}
		
		
	}
}