package nextstep.subway.dto;

import nextstep.subway.entity.Line;
import nextstep.subway.entity.Station;

import java.util.ArrayList;
import java.util.List;

public class LineResponse {
    private Long id;
    private String name;
    private String color;
    private List<Station> stations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public List<Station> getStations() {
        return stations;
    }

    private LineResponse(Long id, String name, String color, List<Station> stations) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.stations = stations;
    }

    public static LineResponse from(Line line) {
        LineResponse lineResponse =  new LineResponse(
                line.getId(),
                line.getName(),
                line.getColor(),
                line.getStations()
        );
        return lineResponse;
    }
}
