package demo.weather.services;

import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class ForecastService {
	private final static Logger log = Logger.getLogger("demo.weather");

	// e.g. https://api.weather.gov/points/30.501272,-97.83087
	// good : "https://api.weather.gov/points/30.5013,-97.8309";
	private static final String FORECAST_SERVICE = "http://api.weather.gov/points/";

	private String geopoint;
	private String cwa;
	private JsonArray periods;

	public ForecastService(String geopoint) {
		this.geopoint = geopoint;
	}

	/**
	 * Resolves the forecast data associated with the geopoint.
	 * 
	 * @return true is the resolution was successful
	 */
	public boolean resolve() {
		String targetUrl = FORECAST_SERVICE + geopoint;
		log.fine("Resolving geopoint to forecast URL: " + targetUrl);

		try {
			Client client = RestClientFactory.getRestClient();

			Response response = client.target(targetUrl).request().get();
			JsonObject jObj = response.readEntity(JsonObject.class);

			response.close();
			client.close();

			cwa = jObj.getJsonObject("properties").getString("cwa");
			String forecastURL = getForecastURL(jObj);
			log.info("resolving forecast URL: " + forecastURL);
			getForecastFromURL(forecastURL);
			return true;
		} catch (Exception e) {
			log.severe("Unexpected Exception while resolving forecast: " + e.getMessage());
		}

		return false;
	}

	public String getGeopoint() {
		return geopoint;
	}

	public String getCWA() {
		return cwa;
	}

	public JsonArray getPeriods() {
		return periods;
	}

	private String getForecastURL(JsonObject jObj) {
		return jObj.getJsonObject("properties").getString("forecast");
	}

	private void getForecastFromURL(String forecastURL) {
		Client client = RestClientFactory.getRestClient();

		Response response = client.target(forecastURL).request().get();
		JsonObject jObj = response.readEntity(JsonObject.class);

		response.close();
		client.close();

		periods = jObj.getJsonObject("properties").getJsonArray("periods");
	}
}
