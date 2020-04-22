package demo.weather.data;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class MoonWeatherData {

	public static JsonObject simulatedRealData() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("zip", "00000");
		builder.add("city", "The Moon, SPACE!");
		builder.add("geopoint", "-1,-1");
		builder.add("cwa", "MOONZ");

		JsonArrayBuilder periods = Json.createArrayBuilder();
		JsonObjectBuilder moonperiod = Json.createObjectBuilder();
		moonperiod.add("number", 1);
		moonperiod.add("name", "Space Afternoon");
		moonperiod.add("startTime", "now");
		moonperiod.add("endTime", "then");
		moonperiod.add("temperature", "9001");
		moonperiod.add("temperatureUnit", "K");
		moonperiod.add("icon", "https://api.weather.gov/icons/land/night/few?size=medium");
		moonperiod.add("detailedForecast", "Space weather!");

		periods.add(moonperiod.build());
		builder.add("periods", periods.build());
		return builder.build();
	}

}
