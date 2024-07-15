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
    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
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

        Line newSectionLine = section.getLine();
        Station newSectionUpStation = section.getUpStation();
        Station newSectionDownStation = section.getDownStation();
        Long newSectionDistance = section.getDistance();

        //새로운 구간의 하행역이 기존구간의 상행종점역과 일치할떄 = 맨앞에 추가
        Station oldSectionUpEndStation = sectionList.get(0).getUpStation();
        Station oldSectionDownEndStation = sectionList.get(sectionList.size()-1).getDownStation();

        if (oldSectionUpEndStation.getId() == newSectionDownStation.getId()) {
            validateStationDuplication(newSectionUpStation);
            sectionList.add(0,section);
        }else if (oldSectionDownEndStation.getId() == newSectionUpStation.getId()) {
            //새로운 구간의 상행역과 일치하는 기존구간이 맨 마지막 구간일때 = 맨 끝에 추가
            validateStationDuplication(newSectionDownStation);
            sectionList.add(section);
        } else if (isMiddleSection(newSectionUpStation)) {
            //새로운 구간의 상행역이 기존구간의 하행역과 일치할때 = 중간에 추가
            for (int index = 0; index < sectionList.size(); index++) {
                Station oldSectionUpStation = sectionList.get(index).getUpStation();

                if (oldSectionUpStation.getId() == newSectionUpStation.getId()) {
                    validateStationDuplication(newSectionDownStation);
                    Station oldSectionDownStation = sectionList.get(index).getDownStation();
                    Long oldDistance = sectionList.get(index).getDistance();
                    if (oldDistance <= newSectionDistance || newSectionDistance <= 0) {
                        throw new IllegalSectionException("신규 구간 거리가 올바르지않습니다.");
                    }

                    sectionList.remove(index);
                    Section rightSection = new Section(newSectionLine, newSectionDownStation, oldSectionDownStation, oldDistance - newSectionDistance);
                    sectionList.add(index, rightSection);
                    Section leftSection = new Section(newSectionLine, oldSectionUpStation, newSectionDownStation, newSectionDistance);
                    sectionList.add(index, leftSection);
                }
            }
        } else {
            throw new IllegalSectionException("노선의 구간과 연결되지 않습니다.");
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
