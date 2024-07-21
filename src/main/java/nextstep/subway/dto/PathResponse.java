package nextstep.subway.dto;

import nextstep.subway.entity.Station;

import java.util.ArrayList;
import java.util.List;

public class PathResponse {
    private List<Station> stations = new ArrayList<>();
    private Long distance;

    public PathResponse(List<Station> stations, Long distance) {
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
