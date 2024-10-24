package dev.dankoz.BaseServer.weather.providers;

import com.fasterxml.jackson.databind.JsonNode;
import dev.dankoz.BaseServer.config.ApiKeysProperties;
import dev.dankoz.BaseServer.weather.Weather;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
public class WeatherProviderTommorowIo implements WeatherProvider{
    private final ApiKeysProperties apiKeysProperties;
    private final String apiURL = "https://api.tomorrow.io/v4/weather/realtime?location=%f,%f&apikey=%s";

   public WeatherProviderTommorowIo( ApiKeysProperties apiKeysProperties) {
       this.apiKeysProperties = apiKeysProperties;
   }

    public Weather getWeather(){
        return Weather.builder()
                .date(new Date(System.currentTimeMillis()))
                .description(mapWeatherCode(jsonNode.get("weatherCode").asText()))
                .temperature(jsonNode.get("temperature").floatValue())
                .windSpeed(jsonNode.get("windSpeed").floatValue())
                .pressure(jsonNode.get("pressureSurfaceLevel").floatValue())
                .build();
    }

    private String mapWeatherCode(String code){
        HashMap<String,String> weatherCodes = new HashMap<>();
        weatherCodes.put("0", "Unknown");
        weatherCodes.put("1000", "Clear, Sunny");
        weatherCodes.put("1100", "Mostly Clear");
        weatherCodes.put("1101", "Partly Cloudy");
        weatherCodes.put("1102", "Mostly Cloudy");
        weatherCodes.put("1001", "Cloudy");
        weatherCodes.put("2000", "Fog");
        weatherCodes.put("2100", "Light Fog");
        weatherCodes.put("4000", "Drizzle");
        weatherCodes.put("4001", "Rain");
        weatherCodes.put("4200", "Light Rain");
        weatherCodes.put("4201", "Heavy Rain");
        weatherCodes.put("5000", "Snow");
        weatherCodes.put("5001", "Flurries");
        weatherCodes.put("5100", "Light Snow");
        weatherCodes.put("5101", "Heavy Snow");
        weatherCodes.put("6000", "Freezing Drizzle");
        weatherCodes.put("6001", "Freezing Rain");
        weatherCodes.put("6200", "Light Freezing Rain");
        weatherCodes.put("6201", "Heavy Freezing Rain");
        weatherCodes.put("7000", "Ice Pellets");
        weatherCodes.put("7101", "Heavy Ice Pellets");
        weatherCodes.put("7102", "Light Ice Pellets");
        weatherCodes.put("8000", "Thunderstorm");

        return weatherCodes.get(code);
    }

}
