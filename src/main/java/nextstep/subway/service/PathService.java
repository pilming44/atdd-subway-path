package nextstep.subway.service;

import nextstep.subway.dto.PathResponse;
import nextstep.subway.entity.Line;
import nextstep.subway.entity.PathFinder;
import nextstep.subway.entity.Station;
import nextstep.subway.repository.LineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PathService {
    private final LineRepository lineRepository;

    public PathService(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    public PathResponse getPath(Station source, Station target) {
        List<Line> allLines = lineRepository.findAll();
        PathFinder pathFinder = new PathFinder();
        allLines.stream().forEach(pathFinder::addLine);
        return pathFinder.getPath(source, target);
    }
}
