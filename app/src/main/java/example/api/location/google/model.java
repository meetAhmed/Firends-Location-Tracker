package example.api.location.google;

public class model {
    Double lat;
    Double lon;
    Long time;

    public model(Double lat, Double lon, Long time) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
    }

    public model() {
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public Long getTime() {
        return time;
    }
}
