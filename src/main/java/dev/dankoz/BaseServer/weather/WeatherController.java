package dev.dankoz.BaseServer.weather;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("")
    public ResponseEntity<?> getWeather(@RequestParam("lat") float lat, @RequestParam("lon") float lon, @RequestParam("location") String location){
        return weatherService.getWeather(lat,lon,location);
    }

}
