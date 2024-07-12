package nextstep.subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nextstep.subway.dto.LineRequest;
import nextstep.subway.dto.LineResponse;
import nextstep.subway.dto.SectionRequest;
import nextstep.subway.entity.Line;
import nextstep.subway.entity.Section;
import nextstep.subway.entity.Station;
import nextstep.subway.exception.NoSuchLineException;
import nextstep.subway.exception.NoSuchStationException;
import nextstep.subway.repository.LineRepository;
import nextstep.subway.repository.SectionRepository;
import nextstep.subway.repository.StationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final SectionRepository sectionRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository, SectionRepository sectionRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Station upStation = null;
        Station downStation = null;

        if (lineRequest.getUpStationId() != null) {
            upStation = getStation(lineRequest.getUpStationId());
        }
        if (lineRequest.getDownStationId() != null) {
            downStation = getStation(lineRequest.getDownStationId());
        }
        Line line = lineRepository.save(new Line(lineRequest.getName(), lineRequest.getColor()));

        sectionRepository.save(new Section(line, upStation, downStation, lineRequest.getDistance()));

        return LineResponse.from(line);
    }

    public List<LineResponse> findAllLines() {
        List<Line> allLines = lineRepository.findAll();
        return allLines.stream()
                .map(line -> LineResponse.from(line))
                .collect(Collectors.toList());
    }

    public LineResponse findLine(Long id) {
        return LineResponse.from(getLine(id));
    }

    @Transactional
    public void updateLine(Long id, LineRequest lineRequest) {
        Line line = getLine(id);
        Optional.ofNullable(lineRequest.getName()).ifPresent(line::setName);
        Optional.ofNullable(lineRequest.getColor()).ifPresent(line::setColor);
    }

    @Transactional
    public void removeLine(Long id) {
        lineRepository.deleteById(id);
    }

    @Transactional
    public LineResponse addSection(Long id, SectionRequest sectionRequest) {
        Line line = getLine(id);
        Station upStation = getStation(sectionRequest.getUpStationId());

        Station downStation = getStation(sectionRequest.getDownStationId());

        Section section = new Section(line, upStation, downStation, sectionRequest.getDistance());

        sectionRepository.save(section);

        return LineResponse.from(line);
    }

    @Transactional
    public void removeSection(Long id, Long stationId) {
        Line line = getLine(id);
        Station downStation = getStation(stationId);

        Section removedSection = line.removedSection(downStation);

        sectionRepository.delete(removedSection);
    }

    private Line getLine(Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new NoSuchLineException("존재하지 않는 노선입니다."));
    }

    private Station getStation(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new NoSuchStationException("존재하지 않는 역입니다."));
    }
}
