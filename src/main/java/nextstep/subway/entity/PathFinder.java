package nextstep.subway.entity;

import nextstep.subway.dto.PathResponse;
import nextstep.subway.exception.IllegalPathException;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.List;

public class PathFinder {
    private WeightedMultigraph<Station, DefaultWeightedEdge> routeMap = new WeightedMultigraph(DefaultWeightedEdge.class);

    public void addLine(Line line) {
        List<Station> stations = line.getStations();
        stations.stream().forEach(this.routeMap::addVertex);

        Sections sections = line.getSections();
        List<Section> sectionList = sections.getSectionList();
        sectionList.stream()
                .forEach(s -> this.routeMap.setEdgeWeight(
                        this.routeMap.addEdge(s.getUpStation(), s.getDownStation())
                        , s.getDistance()
                ));
    }

    public void addAllLines(List<Line> lines) {
        lines.stream().forEach(this::addLine);
    }

    public PathResponse getPath(Station source, Station target) {
        validateEqualStation(source, target);
        validateStationExist(source, target);
        DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(routeMap);
        GraphPath path = dijkstraShortestPath.getPath(source, target);
        validateLinkedPath(path);
        List<Station> shortestPath = path.getVertexList();
        long shortestDistance = (long) dijkstraShortestPath.getPathWeight(source, target);

        return PathResponse.from(shortestPath, shortestDistance);
    }

    private void validateEqualStation(Station source, Station target) {
        if (source.equals(target)) {
            throw new IllegalPathException("출발역과 도착역이 같은 경우 경로를 조회할수 없습니다.");
        }
    }

    private void validateLinkedPath(GraphPath path) {
        if (path == null) {
            throw new IllegalPathException("출발역과 도착역이 연결되어있지 않습니다.");
        }
    }

    private void validateStationExist(Station source, Station target) {
        if (!routeMap.containsVertex(source)) {
            throw new IllegalPathException("출발역이 경로에 존재하지 않습니다.");
        }
        if (!routeMap.containsVertex(target)) {
            throw new IllegalPathException("도착역이 경로에 존재하지 않습니다.");
        }
    }
}
