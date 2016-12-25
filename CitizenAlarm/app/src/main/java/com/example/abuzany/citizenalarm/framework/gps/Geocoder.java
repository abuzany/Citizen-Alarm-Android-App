package com.example.abuzany.citizenalarm.framework.gps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.example.abuzany.citizenalarm.R;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by abuza on 05/06/2016.
 */
public class Geocoder {
    public static String reverseGeocode(LatLng loc) {
        //http://maps.google.com/maps/geo?q=40.714224,-73.961452&output=json&oe=utf8&sensor=true_or_false&key=your_api_key
        String localityName = "";
        HttpURLConnection connection = null;
        URL serverAddress = null;

        try {
            // build the URL using the latitude & longitude you want to lookup
            // NOTE: I chose XML return format here but you can choose something else
            serverAddress = new URL("http://maps.google.com/maps/geo?q=" + Double.toString(loc.latitude) + "," + Double.toString(loc.longitude) +
                    "&output=xml&oe=utf8&sensor=true&key=" + R.string.google_maps_key);
            //set up out communications stuff
            connection = null;

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);

            connection.connect();

            try {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                InputSource source = new InputSource(isr);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLReader xr = parser.getXMLReader();
                GoogleReverseGeocodeXmlHandler handler = new GoogleReverseGeocodeXmlHandler();

                xr.setContentHandler(handler);
                xr.parse(source);

                localityName = handler.getLocalityName();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return localityName;
    }
}
