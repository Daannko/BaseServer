package dev.dankoz.BaseServer.weather;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherRepository extends CrudRepository<Weather,Integer> {

    Optional<Weather> findByLatAndLon(float lat,float lon);
    Optional<Weather> findByLocationName(String location);

    @Query("SELECT id,lat,lon from Weather")
    List<Weather> getAll();
    Weather getById(Integer id);

}

