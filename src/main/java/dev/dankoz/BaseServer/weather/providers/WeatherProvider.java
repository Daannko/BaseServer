package dev.dankoz.BaseServer.weather.providers;

import com.fasterxml.jackson.databind.JsonNode;
import dev.dankoz.BaseServer.weather.Weather;

public interface WeatherProvider {
    public abstract Weather getWeather();

}
