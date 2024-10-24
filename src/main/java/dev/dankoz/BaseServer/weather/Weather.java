package dev.dankoz.BaseServer.weather;

import jakarta.persistence.Entity;
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

    @Id
    private Integer id;
    @Setter
    private String locationName;
    @Setter
    private float lat,lon;
    @Setter
    private Date date;
    private String description;
    private float temperature;
    private float windSpeed;
    private float pressure;

}
