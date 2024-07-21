package nextstep.subway.entity;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private List<Station> stations = new ArrayList<>();
    private Long distance;

    public Path(List<Station> stations, Long distance) {
        this.stations = stations;
        this.distance = distance;
    }

    public List<Station> getStations() {
        return stations;
    }

    public Long getDistance() {
        return distance;
    }
}
