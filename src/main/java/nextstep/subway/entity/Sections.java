package nextstep.subway.entity;

import nextstep.subway.exception.IllegalSectionException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Embeddable
public class Sections {
    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sectionList = new ArrayList<>();

    public List<Section> getSectionList() {
        return Collections.unmodifiableList(sectionList);
    }

    public int getSectionListSize() {
        return sectionList.size();
    }

    public List<Station> getStations() {
        List<Station> stations = new ArrayList<>();

        if (sectionList.isEmpty()) {
            return stations;
        }

        Section currentSection = sectionList.get(0);
        stations.add(currentSection.getUpStation());
        stations.add(currentSection.getDownStation());

        Optional<Section> nextSection = findNextSection(currentSection);

        while (nextSection.isPresent()) {
            currentSection = nextSection.get();
            stations.add(currentSection.getDownStation());
            nextSection = findNextSection(currentSection);
        }

        return stations;
    }

    public void addSection(Section section) {
        if (sectionList.isEmpty()) {
            sectionList.add(section);
            return;
        }

        Station newUpStation = section.getUpStation();
        Station newDownStation = section.getDownStation();

        Station firstUpStation = sectionList.get(0).getUpStation();
        Station lastDownStation = sectionList.get(sectionList.size() - 1).getDownStation();

        if (isFirstSection(newDownStation, firstUpStation)) {
            addSectionToFront(section);
            return;
        }

        if (isLastSection(newUpStation, lastDownStation)) {
            addSectionToEnd(section);
            return;
        }

        if (isMiddleSection(newUpStation)) {
            addSectionToMiddle(section);
            return;
        }

        throw new IllegalSectionException("노선의 구간과 연결되지 않습니다.");
    }

    private boolean isFirstSection(Station newDownStation, Station firstUpStation) {
        return firstUpStation.getId() == newDownStation.getId();
    }

    private boolean isLastSection(Station newUpStation, Station lastDownStation) {
        return lastDownStation.getId() == newUpStation.getId();
    }

    private void addSectionToFront(Section section) {
        validateStationDuplication(section.getUpStation());
        List<Section> sections = new ArrayList<>(sectionList);
        sectionList.clear();
        sectionList.add(section);
        for (Section s : sections) {
            sectionList.add(new Section(s.getLine(), s.getUpStation(), s.getDownStation(), s.getDistance()));
        }
    }

    private void addSectionToEnd(Section section) {
        validateStationDuplication(section.getDownStation());
        sectionList.add(section);
    }

    private void addSectionToMiddle(Section section) {
        Station newUpStation = section.getUpStation();
        Station newDownStation = section.getDownStation();
        Long newDistance = section.getDistance();

        int oldIndex = 0;
        Section oldSection = new Section();
        for (int index = 0; index < sectionList.size(); index++) {
            Section currentSection = sectionList.get(index);
            if (currentSection.getUpStation().getId() == newUpStation.getId()) {
                validateStationDuplication(newDownStation);
                validateDistance(currentSection, newDistance);
                oldIndex = index;
                oldSection = currentSection;
                break;
            }
        }
        Section rightSection = new Section(section.getLine(), newDownStation, oldSection.getDownStation(),
                oldSection.getDistance() - newDistance);
        sectionList.set(oldIndex, rightSection);

        Section leftSection = new Section(section.getLine(), newUpStation, newDownStation, newDistance);
        sectionList.add(oldIndex, leftSection);
    }

    private void validateDistance(Section currentSection, Long newDistance) {
        if (currentSection.getDistance() <= newDistance || newDistance <= 0) {
            throw new IllegalSectionException("신규 구간 거리가 올바르지 않습니다.");
        }
    }

    private boolean isMiddleSection(Station newSectionUpStation) {
        return sectionList.stream()
                .anyMatch(sec -> sec.getUpStation().getId() == newSectionUpStation.getId());
    }

    public void removeSection(Station downStation) {

        validateDeleteEmpty();

        validateDeleteOnlyOne();

        validateDeletableSection(downStation);

        sectionList.remove(sectionList.size() - 1);
    }

    private Optional<Section> findNextSection(Section tempSection) {
        return sectionList.stream()
                .filter(section -> section.getUpStation().equals(tempSection.getDownStation()))
                .findFirst();
    }

    private void validateStationDuplication(Station station) {
        for (Section s : sectionList) {
            if (s.getUpStation().getId() == station.getId()
                    || s.getDownStation().getId() == station.getId()) {
                throw new IllegalSectionException("이미 노선에 등록되어있는 역입니다.");
            }
        }
    }

    private void validateDeletableSection(Station downStation) {
        if (sectionList.isEmpty()) {
            return;
        }

        if (sectionList.get(sectionList.size() - 1).getDownStation().getId() != downStation.getId()) {
            throw new IllegalSectionException("노선의 마지막 구간이 아닙니다.");
        }
    }

    private void validateDeleteOnlyOne() {
        if (sectionList.size() == 1) {
            throw new IllegalSectionException("노선에 구간이 하나뿐이면 삭제할수없습니다.");
        }
    }

    private void validateDeleteEmpty() {
        if (sectionList.isEmpty()) {
            throw new IllegalSectionException("노선에 삭제 할 구간이 없습니다.");
        }
    }
}
