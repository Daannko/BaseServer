package dev.dankoz.BaseServer.weather.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import dev.dankoz.BaseServer.config.properties.ApiKeysProperties;
import dev.dankoz.BaseServer.service.HttpService;
import dev.dankoz.BaseServer.weather.Weather;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.Date;
import java.util.HashMap;


public class WeatherProviderTommorowIo implements WeatherProvider{
    private final ApiKeysProperties apiKeysProperties;
    private final HttpService httpService;

   public WeatherProviderTommorowIo(ApiKeysProperties apiKeysProperties, HttpService httpService) {
       this.apiKeysProperties = apiKeysProperties;
       this.httpService = httpService;
   }

    public Weather getWeather(float lat, float  lon) {
        String apiURL = String.format("https://api.tomorrow.io/v4/weather/realtime?location=%f,%f&apikey=%s",lat,lon,apiKeysProperties.tommorowApiKey());
        JsonNode node = httpService.get(apiURL);
        node = node.get("data");
        if(node == null) return null;
        try {
            return Weather.builder()
                    .date(new Date(System.currentTimeMillis()))
                    .weatherCode(node.get("values").get("weatherCode").intValue())
                    .description(mapWeatherCode(node.get("values").get("weatherCode").asText()))
                    .temperature(node.get("values").get("temperature").floatValue())
                    .windSpeed(node.get("values").get("windSpeed").floatValue())
                    .pressure(node.get("values").get("pressureSurfaceLevel").floatValue())
                    .lat(lat)
                    .lon(lon)
                    .build();
        }catch (Exception e){
            return null;
        }

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
