package dev.dankoz.BaseServer.weather;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import dev.dankoz.BaseServer.config.properties.ApiKeysProperties;
import dev.dankoz.BaseServer.service.HttpService;
import dev.dankoz.BaseServer.weather.providers.WeatherProviderTommorowIo;
import org.hibernate.Hibernate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {
    private final WeatherRepository weatherRepository;

    private final ApiKeysProperties apiKeysProperties;

    private final HttpService httpService;


    public WeatherService(WeatherRepository weatherRepository, ApiKeysProperties apiKeysProperties, HttpService httpService) {
        this.weatherRepository = weatherRepository;
        this.apiKeysProperties = apiKeysProperties;
        this.httpService = httpService;
    }

    @SuppressWarnings("all")
    public ResponseEntity<?> getWeather(float lat, float lon,  Optional<String> location) {
        String loc = location.orElse(String.format("Lat:%f Lon:%f",lat,lon));

        Weather weather = null;
        if(location.isPresent()){
            weather = loadWeatherFromLocation(location.get());
        }
        if(weather == null){
            weather = loadWeatherFromCords(lat,lon);
        }
        if(weather != null){
            return ResponseEntity.ok().body(weather);
        }

        weather = new WeatherProviderTommorowIo(apiKeysProperties,httpService).getWeather(lat,lon);

        weather.setLocationName(loc);
        weatherRepository.save(weather);
        return ResponseEntity.ok().body(weather);
    }

    private Weather loadWeatherFromLocation(String location){
        Optional<Weather> optionalLastWeather = weatherRepository.findByLocationName(location);
        if(optionalLastWeather.isEmpty()){
            return null;
        }
        Weather weather = optionalLastWeather.get();
        return weather.getDate().before(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) ? null : weather;
    }

    private Weather loadWeatherFromCords(float lat,float lon){
        // Check if there is a weather with exact cords
        Optional<Weather> optionalLastWeather = weatherRepository.findByLatAndLon(lat,lon);
        Weather weather = null;
        if(optionalLastWeather.isPresent()){
            weather = optionalLastWeather.get();
            // Check if the weather is at least from last hour
            return weather.getDate().before(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) ? null : weather;
        }

        List<Weather> allWeathers = weatherRepository
                .getAllRecent(new Date(System.currentTimeMillis() - 1000 * 60 * 60));


        // Check if there is a location within DISTANCE LIMIT kilometers
        double lat1Rad = Math.toRadians(lat);
        double lon1Rad = Math.toRadians(lon);
        int EARTH_RADIUS = 6371;
        int DISTANCE_LIMIT = 60;

        for (Weather allWeather : allWeathers) {
            double lat2Rad = Math.toRadians(allWeather.getLat());
            double lon2Rad = Math.toRadians(allWeather.getLon());

            double x = (lon2Rad - lon1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
            double y = (lat2Rad - lat1Rad);

            double distance = Math.sqrt(x * x + y * y) * EARTH_RADIUS;
            if (distance < DISTANCE_LIMIT) {
                return (Weather) Hibernate.unproxy(weatherRepository.getById(allWeather.getId()));
            }
        }
        return null;
    }

}
