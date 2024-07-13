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

        validateStationLink(section);

        validateStationDuplication(section);

        sectionList.add(section);
    }

    public Section removedSection(Station downStation) {

        validateDeleteEmpty();

        validateDeleteOnlyOne();

        validateDeletableSection(downStation);

        Section removedSection = sectionList.get(sectionList.size() - 1);

        sectionList.remove(sectionList.size() - 1);

        return removedSection;
    }

    private Optional<Section> findNextSection(Section tempSection) {
        return sectionList.stream()
                .filter(section -> section.getUpStation().equals(tempSection.getDownStation()))
                .findFirst();
    }

    private void validateStationDuplication(Section section) {
        for (Section s : sectionList) {
            if (s.getUpStation().getId() == section.getDownStation().getId()
                    || s.getDownStation().getId() == section.getDownStation().getId()) {
                throw new IllegalSectionException("구간의 하행역이 이미 노선에 등록되어있는 역입니다.");
            }
        }
    }

    private void validateStationLink(Section section) {
        if (sectionList.isEmpty()) {
            return;
        }
        if (sectionList.get(sectionList.size() - 1).getDownStation().getId() != section.getUpStation().getId()) {
            throw new IllegalSectionException("구간의 상행역이 노선 마지막 하행종점역이 아닙니다.");
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
