package nextstep.subway.entity;

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
                .forEach(s->this.routeMap.setEdgeWeight(
                        this.routeMap.addEdge(s.getUpStation(), s.getDownStation())
                        , s.getDistance()
                        ));
    }

    public Path getPath(Station source, Station target) {
        DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(routeMap);
        List<Station> shortestPath = dijkstraShortestPath.getPath(source, target).getVertexList();
        long shortestDistance = (long) dijkstraShortestPath.getPathWeight(source, target);
        return new Path(shortestPath, shortestDistance);
    }
}
