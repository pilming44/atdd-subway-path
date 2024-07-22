package nextstep.subway.service;

import nextstep.subway.dto.PathRequest;
import nextstep.subway.dto.PathResponse;
import nextstep.subway.entity.Line;
import nextstep.subway.entity.PathFinder;
import nextstep.subway.entity.Station;
import nextstep.subway.exception.NoSuchStationException;
import nextstep.subway.repository.LineRepository;
import nextstep.subway.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PathService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public PathService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public PathResponse getPath(PathRequest pathRequest) {
        Station sourceStation = null;
        Station targetStation = null;

        if (pathRequest.getSource() != null) {
            sourceStation = getStation(pathRequest.getSource());
        }
        if (pathRequest.getTarget() != null) {
            targetStation = getStation(pathRequest.getTarget());
        }
        List<Line> allLines = lineRepository.findAll();
        PathFinder pathFinder = new PathFinder();
        pathFinder.addAllLines(allLines);
        return pathFinder.getPath(sourceStation, targetStation);
    }

    private Station getStation(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new NoSuchStationException("존재하지 않는 역입니다."));
    }
}
