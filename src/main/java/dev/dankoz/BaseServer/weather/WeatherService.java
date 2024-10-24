package dev.dankoz.BaseServer.weather;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import dev.dankoz.BaseServer.config.ApiKeysProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {
    private final WeatherRepository weatherRepository;

    private final ApiKeysProperties apiKeysProperties;



    public WeatherService(WeatherRepository weatherRepository, ApiKeysProperties apiKeysProperties) {
        this.weatherRepository = weatherRepository;
        this.apiKeysProperties = apiKeysProperties;
    }

    public ResponseEntity<?> getWeather(float lat, float lon, String location) {

        Weather weather;
        weather = loadWeatherFromLocation(location);
        if(weather == null) weather = loadWeatherFromCords(lat,lon);

        if(weather != null){
            return ResponseEntity.ok().body(weather);
        }

        getWeatherFromTommorowIo(lat,lon);



        return ResponseEntity.ok().body("");
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
        // Check if there is a location within DISTANCE LIMIT kilometers
        List<Weather> allWeathers = weatherRepository.getAll();
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
                weather = weatherRepository.getById(allWeather.getId());
                if (weather.getDate().before(new Date(System.currentTimeMillis() - 1000 * 60 * 60))) {
                    break;
                }
            }
        }
        return weather;
    }

    public Weather getWeatherFromTommorowIo(float lat,float lon) throws JsonProcessingException {











        RestClient customClient = RestClient.builder().build();
        String response = customClient.get()
                .uri(String.format(apiURL,lat,lon,apiKEY))
                .retrieve()
                .body(String.class);
        JsonNode jsonNode = new ObjectMapper().reader().readTree(response).get("data").get("values");


        return null;
    }

    private String mapDescriptionCode(String code){

    }

}
