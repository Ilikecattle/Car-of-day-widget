package com.specialcarstore.car_of_day;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.RemoteViews;

class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
	private int viewId;
	private Context context;

	public ImageDownloader(Context context, int viewId) {
		this.context = context;
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
		ComponentName widget = new ComponentName(context,
				MyWidgetProvider.class);

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);

		views.setImageViewBitmap(viewId, result);
		appWidgetManager.updateAppWidget(widget, views);

	}
}