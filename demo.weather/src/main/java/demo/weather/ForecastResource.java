package demo.weather;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import demo.weather.data.MoonWeatherData;
import demo.weather.services.ForecastService;
import demo.weather.services.ZipToGeoPointService;

@Path("forecast")
public class ForecastResource {
	private final static Logger log = Logger.getLogger("demo.weather");
	private final static Map<String, JsonObject> cache = new HashMap<String, JsonObject>();

	static {
		cache.put("00000", MoonWeatherData.simulatedRealData());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getForecast(@QueryParam("zip") String zip) {
		// Turn on all logging for development
		log.setLevel(Level.ALL);

		JsonObject response = null;
		if (cache.containsKey(zip)) {
			log.info("Successful cache hit for zip " + zip);
			response = cache.get(zip);
		} else {
			response = getUncachedForecast(zip);
		}

		return response;
	}

	/**
	 * The forecast for the zip was not cached, go out on the network and resolve it
	 * @param zip
	 * @return
	 */
	private JsonObject getUncachedForecast(String zip) {
		JsonObject response;
		boolean shouldCache = false;
		JsonObjectBuilder builder = Json.createObjectBuilder();
		if (checkZipValueIsSetOrSetResponseError(zip, builder)) {
			ZipToGeoPointService zipData = new ZipToGeoPointService(zip);
			if (zipResolveIsGoodOrSetResposneError(zipData, builder)) {
				builder.add("zip", zip);
				builder.add("city", zipData.getCity());
				builder.add("geopoint", zipData.getGeopoint());
				ForecastService forecast = new ForecastService(zipData.getGeopoint());
				if (forecastResolvesOrSetResponseError(forecast, builder)) {
					builder.add("cwa", forecast.getCWA());
					builder.add("periods", forecast.getPeriods());
					shouldCache = true;
				}
			}
		}
		response = builder.build();
		if (shouldCache) {
			cache.put(zip, response);
			log.info("Cached response for zip " + zip);
		}
		return response;
	}

	private boolean checkZipValueIsSetOrSetResponseError(String zip, JsonObjectBuilder builder) {
		if (zip == null || zip.isEmpty()) {
			builder.add("error", "zip value is null. Specify with query param ?zip=00000");
			return false;
		} else {
			return true;
		}
	}

	private boolean zipResolveIsGoodOrSetResposneError(ZipToGeoPointService zipData, JsonObjectBuilder builder) {
		if (!zipData.resolve()) {
			builder.add("error", "Unexpected error encountered while trying to resolve zip to geopoint");
			return false;
		}
		return true;
	}

	public boolean forecastResolvesOrSetResponseError(ForecastService forecast, JsonObjectBuilder builder) {
		if (!forecast.resolve()) {
			builder.add("error", "Unexpected error encountered while trying to resolve forecast");
			return false;
		}
		return true;
	}

}