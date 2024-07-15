package nextstep.subway.entity;

import nextstep.subway.exception.IllegalSectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static nextstep.subway.entity.EntityTestUtil.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("노선관리 단위테스트")
class LineTest {
    private Line 신분당선;

    @BeforeEach
    void setUp() {
        신분당선 = new Line("신분당선", "#77777");
    }

    @Test
    @DisplayName("구간없는 노선 생성")
    void 구간_없는_노선_생성() {
        Line line = new Line();
        List<Station> stations = line.getStations();
        assertThat(stations.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("노선에 구간 추가")
    void 노선에_구간_추가() {
        // when
        new Section(신분당선, 신사역, 강남역, 10L);

        // then
        Sections sections = 신분당선.getSections();
        List<Section> sectionList = sections.getSectionList();
        assertThat(sections.getSectionListSize()).isEqualTo(1);
        assertThat(sectionList.get(0).getDistance()).isEqualTo(10L);
    }

    @Test
    @DisplayName("노선 역 조회")
    void 노선_역_조회() {
        //given
        new Section(신분당선, 신사역, 강남역, 10L);

        // when
        List<Station> stations = 신분당선.getStations();

        // then
        assertThat(stations.size()).isEqualTo(2);
        assertThat(stations.get(0)).isEqualTo(신사역);
        assertThat(stations.get(1)).isEqualTo(강남역);
    }

    @Test
    @DisplayName("노선 구간 삭제")
    void 노선_구간_삭제() {
        // given
        new Section(신분당선, 신사역, 강남역, 10L);
        new Section(신분당선, 강남역, 판교역, 20L);

        // when
        신분당선.removedSection(판교역);

        // then
        Sections sections = 신분당선.getSections();
        List<Station> stations = sections.getStations();
        assertThat(sections.getSectionListSize()).isEqualTo(1);
        assertThat(stations.get(0)).isEqualTo(신사역);
        assertThat(stations.get(1)).isEqualTo(강남역);
    }
    
    @Test
    @DisplayName("노선의 하행 종점역과 새로운 구간의 상행역이 불일치 시 예외발생")
    void 노선의_하행_종점역과_새로운_구간의_상행역이_불일치_시_예외발생() {
        // given
        new Section(신분당선, 신사역, 강남역, 10L);

        // when, then
        assertThatThrownBy(() -> new Section(신분당선, 신사역, 강남역, 20L))
                .isInstanceOf(IllegalSectionException.class)
                .hasMessage("구간의 상행역이 노선 마지막 하행종점역이 아닙니다.");
    }

    @Test
    @DisplayName("노선의 마지막 구간이 아닌 구간을 삭제 시 예외발생")
    void 노선의_마지막_구간이_아닌_구간을_삭제_시_예외발생() {
        // given
        new Section(신분당선, 신사역, 강남역, 10L);
        new Section(신분당선, 강남역, 판교역, 20L);

        // when, then
        assertThatThrownBy(() -> 신분당선.removedSection(강남역))
                .isInstanceOf(IllegalSectionException.class)
                .hasMessage("노선의 마지막 구간이 아닙니다.");
    }
    
    @Test
    @DisplayName("노선에 구간이 하나뿐이라면 삭제 시 예외발생")
    void 노선에_구간이_하나뿐이라면_삭제_시_예외발생() {
        // given
        new Section(신분당선, 신사역, 강남역, 10L);

        // when, then
        assertThatThrownBy(() -> 신분당선.removedSection(강남역))
                .isInstanceOf(IllegalSectionException.class)
                .hasMessage("노선에 구간이 하나뿐이면 삭제할수없습니다.");
    }

    @Test
    @DisplayName("노선에 구간이 없는데 삭제 시 예외발생")
    void 노선에_구간이_없는데_삭제_시_예외발생() {
        // given
        // when, then
        assertThatThrownBy(() -> 신분당선.removedSection(강남역))
                .isInstanceOf(IllegalSectionException.class)
                .hasMessage("노선에 삭제 할 구간이 없습니다.");
    }

    @Test
    @DisplayName("노선에 이미 등록된 역을 구간으로 추가 시 예외발생")
    void 노선에_이미_등록_된_역을_구간으로_추가_시_예외발생() {
        // given
        new Section(신분당선, 신사역, 강남역, 10L);

        // when, then
        assertThatThrownBy(() -> new Section(신분당선, 강남역, 신사역, 10L))
                .isInstanceOf(IllegalSectionException.class)
                .hasMessage("구간의 하행역이 이미 노선에 등록되어있는 역입니다.");
    }
}