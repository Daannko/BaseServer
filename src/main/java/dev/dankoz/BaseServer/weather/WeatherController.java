package dev.dankoz.BaseServer.weather;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController()
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("")
    public ResponseEntity<?> getWeather(@RequestParam(value = "lat") float lat,
                                        @RequestParam(value = "lon")  float lon,
                                        @RequestParam(value = "location",required = false)  Optional<String> location){
        return weatherService.getWeather(lat,lon,location);
    }

}
