package com.christianwilcox.widget_test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class CarOfDayXmlParser {
	private static final String ns = null;

	public List<Car> parse(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readCars(parser);
		} finally {
			in.close();
		}
	}

	private List<Car> readCars(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<Car> cars = new ArrayList<Car>();

		parser.require(XmlPullParser.START_TAG, ns, "cars");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("car")) {
				cars.add(readCar(parser));
			} else {
				skip(parser);
			}
		}
		return cars;
	}

	private Car readCar(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.require(XmlPullParser.START_TAG, null, "car");
		String imageLink = null;
		String link = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("imageLink")) {
				imageLink = readImageLink(parser);
			} else if (name.equals("link")) {
				link = readLink(parser);
			} else {
				skip(parser);
			}
		}
		parser.require(XmlPullParser.END_TAG, null, "car");
		return new Car(imageLink, link);
	}

	private String readImageLink(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, "imageLink");
		String imageLink = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "imageLink");
		return imageLink;
	}

	private String readLink(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, "link");
		String link = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "link");
		return link;
	}

	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}
