package dev.dankoz.BaseServer.weather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "weathers")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Weather {

    public Weather(Integer id,float lat,float lon){
        this.id= id;
        this.lat = lat;
        this.lon = lon;
    }

    @Id
    @GeneratedValue
    @JsonIgnore

    private Integer id;
    @Setter
    private String locationName;
    private float lat,lon;
    private Date date;
    private String description;
    private float temperature;
    private float windSpeed;
    private float pressure;
    private int weatherCode;

}
