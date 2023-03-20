package com.server.util;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    private WeatherService() {
        // Static utility class
    }

    /**
     * Fetches the weather for a given location from a local weather service running on <a href="http://localhost:4001">localhost:4001</a>.
     * <p>
     * For a given XML request:
     *
     * <pre>
     * {@code
     * <coordinates>
     *     <latitude>28.23333</latitude>
     *     <longitude>10.23344</longitude>
     * </coordinates>
     * }
     * </pre>
     * <p>
     * the service will respond with an XML response containing the weather information:
     *
     * <pre>
     * {@code
     * <weather>
     *     <latitude>28.23333</latitude>
     *     <longitude>10.23344</longitude>
     *     <temperature>4</temperature>
     *     <Unit>Celcius</Unit>
     * </weather>
     * }
     * </pre>
     *
     * @param latitude  The latitude of the location
     * @param longitude The longitude of the location
     * @return The weather information
     * @throws WeatherServiceException If the weather service is not available or the response is invalid
     */
    public static String getWeatherInfo(double latitude, double longitude) throws WeatherServiceException {
        final String coordinateXml = "<coordinates><latitude>" + latitude + "</latitude><longitude>" + longitude + "</longitude></coordinates>";
        final String address = "http://localhost:4001/weather";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(address))
                    .header("Content-Type", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(coordinateXml))
                    .build();
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            Document doc = loadXMLFromString(response);
            String temperature = doc.getElementsByTagName("temperature").item(0).getTextContent();
            String unit = doc.getElementsByTagName("Unit").item(0).getTextContent();
            return temperature + " " + unit;
        } catch (Exception e) {
            throw new WeatherServiceException(("Failed to fetch weather information: " + e.getMessage()));
        }
    }

    private static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    /**
     * An exception thrown when the weather service is not available or the response is invalid.
     */
    public static class WeatherServiceException extends Exception {

        public WeatherServiceException(String message) {
            super(message);
        }

    }
}
