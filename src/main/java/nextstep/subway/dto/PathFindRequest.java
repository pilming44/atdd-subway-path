package nextstep.subway.dto;

import nextstep.subway.entity.Station;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

public class PathFindRequest {
    private WeightedMultigraph<Station, DefaultWeightedEdge> routeMap;
    private Station source;
    private Station target;

    public PathFindRequest(WeightedMultigraph<Station, DefaultWeightedEdge> routeMap, Station source, Station target) {
        this.routeMap = routeMap;
        this.source = source;
        this.target = target;
    }

    public WeightedMultigraph<Station, DefaultWeightedEdge> getRouteMap() {
        return routeMap;
    }

    public Station getSource() {
        return source;
    }

    public Station getTarget() {
        return target;
    }
}
