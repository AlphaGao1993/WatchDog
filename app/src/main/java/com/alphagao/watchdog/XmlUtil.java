package com.alphagao.watchdog;

import android.graphics.Point;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by AlphaGao on 2019-05-03 21:55
 */

class XmlUtil {

    static Point[] readBoundFromXml(String path, String targetText) throws XmlPullParserException, IOException {
        Point[] points = null;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        InputStream inputStream = new FileInputStream(path);
        parser.setInput(inputStream, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (targetText.equals(parser.getAttributeValue(null, "text"))) {
                    String bounds = parser.getAttributeValue(null, "bounds");
                    int startX = Integer.parseInt(bounds.substring(1, bounds.indexOf(",")));
                    int secondBacer = bounds.indexOf("]", bounds.indexOf(",") + 1);
                    int startY = Integer.parseInt(bounds.substring(bounds.indexOf(",") + 1, secondBacer));
                    int secondDou = bounds.indexOf(",", secondBacer);
                    int endX = Integer.parseInt(bounds.substring(secondBacer + 2, secondDou));
                    int endY = Integer.parseInt(bounds.substring(secondDou + 1, bounds.length() - 1));
                    points = new Point[]{new Point(startX, startY), new Point(endX, endY)};
                }
            }
            eventType = parser.next();
        }
        return points;
    }
}
