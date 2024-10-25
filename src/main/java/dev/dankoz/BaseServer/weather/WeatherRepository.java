package dev.dankoz.BaseServer.weather;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherRepository extends CrudRepository<Weather,Integer> {

    Optional<Weather> findByLatAndLon(float lat,float lon);
    Optional<Weather> findByLocationName(String location);

    @Query("SELECT new dev.dankoz.BaseServer.weather.Weather(w.id, w.lat,w.lon) from Weather w where w.date >= :date")
    List<Weather> getAllRecent(@Param("date") Date date);
    Weather getById(Integer id);

}

