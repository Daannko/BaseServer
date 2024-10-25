package dev.dankoz.BaseServer.weather.providers;

import com.fasterxml.jackson.databind.JsonNode;
import dev.dankoz.BaseServer.weather.Weather;
import java.util.Optional;

public interface WeatherProvider {
    public abstract Weather getWeather(float lat, float lon);

}
