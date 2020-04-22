package demo.weather.services;

import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class ZipToGeoPointService {
	private final static Logger log = Logger.getLogger("demo.weather");

	// e.g.
	// https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&facet=state&facet=timezone&facet=dst&q=78613
	private static final String ZIP_TO_GEO_SERVICE = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&facet=state&facet=timezone&facet=dst&q=";

	private String zipcode;
	private String city;
	private String geopoint = "0,0";

	public ZipToGeoPointService(String zipcode) {
		this.zipcode = zipcode;
	}

	/**
	 * Resolves the geopoint associated with the zip code.
	 * 
	 * @return true is the resolution was successful
	 */
	public boolean resolve() {
		String targetUrl = ZIP_TO_GEO_SERVICE + zipcode;
		log.fine("Resolve zip to geo URL: " + targetUrl);

		try {
			JsonObject urlResponse = getJsonObjectFromURL(targetUrl);
			findGeopoint(urlResponse);
			return true;
		} catch (Exception e) {
			log.severe("Unexpected Exception while resolving zip to geopoint: " + e.getMessage());
		}

		return false;
	}

	public String getGeopoint() {
		return geopoint;
	}

	public String getCity() {
		return city;
	}

	private JsonObject getJsonObjectFromURL(String targetUrl) {
		Client client = RestClientFactory.getRestClient();

		Response response = client.target(targetUrl).request().get();
		JsonObject jObj = response.readEntity(JsonObject.class);
		log.fine("Response object from zip to geo: " + jObj);

		response.close();
		client.close();

		return jObj;
	}

	/**
	 * Find the geopoint for this zip within the json object.
	 * 
	 * @param jObj
	 * @return
	 */
	private void findGeopoint(JsonObject jObj) {
		JsonArray records = jObj.getJsonArray("records");
		for (int i = 0; i < records.size(); i++) {
			JsonObject zipObj = records.getJsonObject(i);
			JsonObject fields = zipObj.getJsonObject("fields");
			if (zipcode.equals(fields.getString("zip"))) {
				city = getCityState(fields);
				geopoint = getGeopoint(fields);
				return;
			}
		}

		log.severe("Could not resolve zipcode " + zipcode + " to a geopoint, returning 0,0");
		return;
	}

	/**
	 * @param fields
	 * @return String of "city, state"
	 */
	private String getCityState(JsonObject fields) {
		String city = fields.getString("city");
		String state = fields.getString("state");
		log.info("fields.city:" + city);
		log.info("fields.state:" + state);
		return city + ", " + state;
	}

	/**
	 * @param fields
	 * @return String of "latitude,longitude"
	 */
	private String getGeopoint(JsonObject fields) {
		JsonNumber latitude = fields.getJsonNumber("latitude");
		JsonNumber longitude = fields.getJsonNumber("longitude");
		log.info("fields.latitude:" + latitude);
		log.info("fields.longitude:" + longitude);
		return latitude + "," + longitude;
	}

}
